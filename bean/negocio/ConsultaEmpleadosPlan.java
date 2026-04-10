package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.general.utilidades.web.ValidaDatos;
import com.coop1.banksys.login.entidades.Segsesion;
import com.coop1.banksys.login.entidades.Segusuario;
import com.coop1.banksys.rhu.entidades.Rhuempleado;
import com.coop1.banksys.rhu.negocio.BusquedasRhuLocal;
import com.coop1.soficoop.pln.dto.*;
import com.coop1.soficoop.pln.entidades.*;
import com.icesoft.faces.context.effects.JavascriptContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

/**
 * Managed Bean para la Consulta de Planes Operativos por Empleados.
 *
 * Permite a los empleados visualizar: - El PAO de su jefe inmediato o
 * departamento - Búsqueda de PAOs por PEP (Plan Estratégico) - Jerarquía de
 * Perspectivas -> Iniciativas -> Acciones - Cronograma trimestral y
 * evaluaciones - Matriz Estratégica completa del PEP
 *
 * @author ENVY360
 */
@ManagedBean(name = "consultaEmpleadosPlan")
@ViewScoped
public class ConsultaEmpleadosPlan implements Serializable {

    //<editor-fold defaultstate="collapsed" desc="EJBs y Sesión">
    @EJB
    private BusquedasPepLocal busqPep;
    @EJB
    private BusquedasRhuLocal busqRhu;
    @ManagedProperty(value = "#{login.sesion}")
    private Segsesion sesion;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Contexto Usuario">
    private Rhuempleado miEmpleado;
    private String nombreArea;
    private boolean tienePaoAsignado = false;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Selección en Cascada">
    private BigInteger selIdPep;
    private BigInteger selIdPao;
    private List<SelectItem> listaPeps;
    private List<SelectItem> listaPaos;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Visualización">
    private Plnpao paoVisualizado;
    private List<ReportePerspectivaDTO> listaResumen = new ArrayList<ReportePerspectivaDTO>();
    private List<ResumenPepDTO> lstArbolCompleto = new ArrayList<ResumenPepDTO>();
    private int indiceTab = 0;
    private ValidaDatos validar = new ValidaDatos(FacesContext.getCurrentInstance());
    private List<Plnpao> listaPaosEncontrados = new ArrayList<Plnpao>();
    private List<Plnaccionsinplan> listaNoPlanificadasEvaluadas = new ArrayList<Plnaccionsinplan>();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables KPIs Globales">
    private int totalGlobalPerspectivas;
    private int totalGlobalObjetivos;
    private int totalGlobalIndicadores;
    private int totalGlobalEstrategias;
    //</editor-fold>

    @PostConstruct
    public void init() {
        cargarContextoYBuscarPao();
        cargarListaPeps();
        if (!tienePaoAsignado) {
            this.showMsg("Aun no se ha aprobado el PAO de tu Área", ValidaDatos.WARNING);

        }
    }

    /**
     * CARGA EL CONTEXTO DEL USUARIO Y BUSCA EL PAO DE SU JEFE.
     */
    private void cargarContextoYBuscarPao() {
        try {
            if (sesion == null || sesion.getSegusuario() == null) {
                return;
            }

            Segusuario u = sesion.getSegusuario();
            BigInteger codEmp = null;

            if (u.getGenpersona() != null && u.getGenpersona().getCodper() != null) {
                codEmp = new BigInteger(u.getGenpersona().getCodper().toString());
            } else if (u.getCodusr() != null) {
                try {
                    codEmp = new BigInteger(u.getCodusr().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                    showMsg("El codPer parece ser null: " + e.getMessage(), ValidaDatos.ERROR);
                }
            }

            if (codEmp != null) {
                miEmpleado = busqRhu.buscarEmpleado(codEmp);
                if (miEmpleado != null) {
                    Rhuempleado jefe = busqRhu.buscarJefaturaInmediata(codEmp);
                    Rhuempleado coordinador = (jefe != null) ? jefe : miEmpleado;

                    if (coordinador.getPuesto() != null && coordinador.getPuesto().getRhudepto() != null) {
                        this.nombreArea = coordinador.getPuesto().getRhudepto().getDescdepto();
                    } else {
                        this.nombreArea = "Área de " + coordinador.getPersona().getNomcom();
                    }

                    int anioActual = Calendar.getInstance().get(Calendar.YEAR);
                    paoVisualizado = busqPep.buscarPaoPorJefe(coordinador.getCodemp(), anioActual);

                    if (paoVisualizado != null) {
                        tienePaoAsignado = true;

                        if (paoVisualizado.getIdpep() != null) {
                            paoVisualizado.getIdpep().getDescrip();
                        }
                        if (paoVisualizado.getIdcoordinador() != null) {
                            paoVisualizado.getIdcoordinador().getPersona().getNomcom();
                        }

                        construirVistaResumen();
                    } else {
                        this.nombreArea += " (Sin PAO registrado)";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos del usuario dueño del PAO: " + e.getMessage(), ValidaDatos.ERROR);

        }
    }

    //<editor-fold defaultstate="collapsed" desc="MÓDULO BÚSQUEDA - Combos en Cascada">
    /**
     * CARGA LA LISTA DE PEPs ACTIVOS PARA EL COMBO. Filtra solo planes
     * estratégicos en estado activo.
     */
    private void cargarListaPeps() {
        listaPeps = new ArrayList<SelectItem>();
        try {
            Map<String, Object> filtros = new HashMap<String, Object>();
            filtros.put("estado", 1);
            List<Plnpep> peps = busqPep.buscarPeps(filtros);

            for (Plnpep p : peps) {
                listaPeps.add(new SelectItem(p.getIdpep(), p.getDescrip() + " (" + p.getAnioini() + "-" + p.getAniofin() + ")"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los PEPS: " + e.getMessage(), ValidaDatos.ERROR);

        }
    }

    /**
     * EVENTO DE CAMBIO DE PEP. Carga SOLO los PAOs del jefe inmediato asociados
     * al PEP seleccionado.
     */
    public void cambioPep() {
        listaPaos = new ArrayList<SelectItem>();
        selIdPao = null;
        tienePaoAsignado = false;
        paoVisualizado = null;

        if (selIdPep != null && !selIdPep.equals(BigInteger.ZERO)) {
            try {
                // Obtener el jefe inmediato del empleado actual
                BigInteger codEmp = null;
                if (sesion != null && sesion.getSegusuario() != null) {
                    if (sesion.getSegusuario().getGenpersona() != null) {
                        codEmp = new BigInteger(
                                sesion.getSegusuario().getGenpersona().getCodper().toString());
                    } else if (sesion.getSegusuario().getCodusr() != null) {
                        try {
                            codEmp = new BigInteger(sesion.getSegusuario().getCodusr().trim());
                        } catch (Exception ignored) {
                        }
                    }
                }

                Rhuempleado jefe = (codEmp != null) ? busqRhu.buscarJefaturaInmediata(codEmp) : null;
                Rhuempleado coordinador = (jefe != null) ? jefe : miEmpleado;

                if (coordinador == null) {
                    return;
                }

                // Traer todos los PAOs del PEP y filtrar solo los del coordinador
                List<Plnpao> todos = busqPep.buscarPaosPorPep(selIdPep);

                if (todos != null) {
                    for (Plnpao p : todos) {
                        // Solo mostrar PAOs cuyo coordinador sea mi jefe inmediato
                        if (p.getIdcoordinador() != null
                                && p.getIdcoordinador().getCodemp()
                                .equals(coordinador.getCodemp())) {

                            String label = p.getNombrepao()
                                    + " - " + p.getAnio()
                                    + " (" + p.getIdcoordinador().getPersona().getNomcom() + ")";

                            listaPaos.add(new SelectItem(p.getIdpao(), label));
                        }
                    }
                }

                if (listaPaos.isEmpty()) {
                    showMsg("Tu coordinador no tiene PAOs registrados en este PEP.",
                            ValidaDatos.WARNING);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cambiar de PEP: " + e.getMessage(), ValidaDatos.ERROR);

            }
        }
    }

    /**
     * CONSULTA EL PAO SELECCIONADO Y MUESTRA SUS DETALLES. Carga la jerarquía
     * completa de iniciativas y acciones.
     */
    public void consultarPaoSeleccionado() {
        tienePaoAsignado = false;
        paoVisualizado = null;

        if (selIdPao != null && !selIdPao.equals(BigInteger.ZERO)) {
            try {

                List<Plnpao> paosTemp = busqPep.buscarPaosPorPep(selIdPep);
                for (Plnpao p : paosTemp) {
                    if (p.getIdpao().equals(selIdPao)) {
                        paoVisualizado = p;
                        break;
                    }
                }

                if (paoVisualizado != null) {
                    if (paoVisualizado.getIdcoordinador().getPuesto() != null && paoVisualizado.getIdcoordinador().getPuesto().getRhudepto() != null) {
                        this.nombreArea = paoVisualizado.getIdcoordinador().getPuesto().getRhudepto().getDescdepto();
                    } else {
                        this.nombreArea = "Área de " + paoVisualizado.getIdcoordinador().getPersona().getNomcom();
                    }

                    construirVistaResumen();
                    tienePaoAsignado = true;
                    this.indiceTab = 1;
                }

            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar los datos del PAO: " + e.getMessage(), ValidaDatos.ERROR);

            }
        }
    }

    /**
     * LIMPIA LOS FILTROS DE BÚSQUEDA Y OCULTA LOS DETALLES. Reinicia la vista
     * al estado inicial.
     */
    public void limpiarBusqueda() {
        this.selIdPep = BigInteger.ZERO;
        this.selIdPao = BigInteger.ZERO;
        this.listaPaos = new ArrayList<SelectItem>();
        this.listaPaosEncontrados = new ArrayList<Plnpao>();
        this.tienePaoAsignado = false;
        this.paoVisualizado = null;
        this.nombreArea = "";
        this.listaResumen.clear();
        this.lstArbolCompleto.clear();
        this.indiceTab = 0;
    }

    /**
     * BUSCA PAOs DEL PEP SELECCIONADO QUE PERTENEZCAN AL MISMO DEPTO DEL
     * EMPLEADO ACTUAL.
     */
    /**
     * BUSCA PAOs DEL PEP SELECCIONADO QUE PERTENEZCAN AL MISMO DEPTO DEL
     * EMPLEADO ACTUAL.
     */
    public void buscarPaos() {
        listaPaosEncontrados = new ArrayList<Plnpao>();
        tienePaoAsignado = false;
        paoVisualizado = null;

        if (selIdPep == null || selIdPep.equals(BigInteger.ZERO)) {
            showMsg("Debe seleccionar un Plan Estratégico (PEP).", ValidaDatos.WARNING);
            return;
        }

        try {
            // ── 1. Validar empleado actual ─────────────────────────────────────
            if (miEmpleado == null
                    || miEmpleado.getPuesto() == null
                    || miEmpleado.getPuesto().getRhudepto() == null) {
                showMsg("No se pudo identificar tu área/departamento.", ValidaDatos.WARNING);
                return;
            }

            BigInteger miCodDepto = miEmpleado.getPuesto()
                    .getRhudepto()
                    .getRhudeptoPK()
                    .getCoddepto();

            String miNombreDepto = miEmpleado.getPuesto()
                    .getRhudepto()
                    .getDescdepto();

            // ── 2. Traer TODOS los PAOs del PEP Usando buscarPaosDinamico ──────
            // ✅ Solo filtro por PEP — sin coordinador, sin agencia, sin depto
            Map<String, Object> filtros = new HashMap<String, Object>();
            filtros.put("idPep", new BigDecimal(selIdPep));

            List<Plnpao> todos = busqPep.buscarPaosDinamico(filtros);

            if (todos == null || todos.isEmpty()) {
                showMsg("No existen PAOs registrados para este PEP.", ValidaDatos.WARNING);
                return;
            }

            // ── 3. Filtrar en Java: solo los del mismo depto que yo ────────────
            for (Plnpao p : todos) {
                try {
                    BigInteger codDeptoCoord = p.getIdcoordinador()
                            .getPuesto()
                            .getRhudepto()
                            .getRhudeptoPK()
                            .getCoddepto();

                    if (codDeptoCoord.equals(miCodDepto)) {
                        listaPaosEncontrados.add(p);
                    }
                } catch (Exception ignored) {
                    // Coordinador sin depto asignado → se omite
                }
            }

            // ── 4. Resultado ───────────────────────────────────────────────────
            if (listaPaosEncontrados.isEmpty()) {
                showMsg("No hay PAOs en tu área (" + miNombreDepto + ") para este PEP.",
                        ValidaDatos.WARNING);
            } else {
                showMsg("Se encontraron " + listaPaosEncontrados.size()
                        + " PAO(s) en el área: " + miNombreDepto, ValidaDatos.INFO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al buscar PAOs: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * SELECCIONA UN PAO DE LA TABLA Y CARGA SU DETALLE. Navega automáticamente
     * al tab de detalle. Recibe SelectEvent igual que
     * mttoPao.seleccionarPaoDeTabla
     */
    public void seleccionarPao(org.icefaces.ace.event.SelectEvent event) {
        Plnpao paoSelect = (Plnpao) event.getObject();

        if (paoSelect == null) {
            return;
        }

        this.paoVisualizado = paoSelect;

        try {
            if (paoSelect.getIdcoordinador().getPuesto() != null
                    && paoSelect.getIdcoordinador().getPuesto().getRhudepto() != null) {
                this.nombreArea = paoSelect.getIdcoordinador()
                        .getPuesto().getRhudepto().getDescdepto();
            } else {
                this.nombreArea = "Área de "
                        + paoSelect.getIdcoordinador().getPersona().getNomcom();
            }
        } catch (Exception e) {
            this.nombreArea = "Sin Datos";
        }

        construirVistaResumen();
        tienePaoAsignado = true;
        this.indiceTab = 1;
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="MÓDULO CONSTRUCCIÓN DE RESUMEN">
    /**
     * CONSTRUYE LA VISTA DE RESUMEN AGRUPADA POR PERSPECTIVA. Estructura:
     * Perspectiva -> Iniciativa -> Acción (con cronograma y evaluación). Filtra
     * solo las acciones que pertenecen al PAO visualizado.
     */
/**
     * CONSTRUYE LA VISTA DE RESUMEN AGRUPADA POR PERSPECTIVA. Estructura:
     * Perspectiva -> Iniciativa -> Acción (con cronograma y evaluación). Filtra
     * solo las acciones que pertenecen al PAO visualizado.
     */
    private void construirVistaResumen() {
        listaResumen.clear();
        Map<String, ReportePerspectivaDTO> mapa = new LinkedHashMap<String, ReportePerspectivaDTO>();

        // Limpiamos la lista cada vez que se calcula un nuevo PAO
        this.listaNoPlanificadasEvaluadas = new ArrayList<Plnaccionsinplan>();

        try {
            List<Plnpaoinic> iniciativas = busqPep.buscarIniciativasPorPep(
                    paoVisualizado.getIdpep().getIdpep());

            if (iniciativas != null) {
                for (Plnpaoinic inic : iniciativas) {
                    ReporteIniciativaDTO dtoInic = new ReporteIniciativaDTO(inic);
                    boolean tieneAccionesDelPao = false;

                    List<Plnacciondeta> accionesRaw =
                            busqPep.buscarAccionesPorIniciativa(inic.getIdiniciativa());

                    if (accionesRaw != null) {
                        for (Plnacciondeta ax : accionesRaw) {
                            if (ax.getIdpao() == null
                                    || !ax.getIdpao().getIdpao().equals(paoVisualizado.getIdpao())) {
                                continue;
                            }

                            tieneAccionesDelPao = true;
                            ReporteAccionDTO dtoAcc = new ReporteAccionDTO(ax);

                            List<Plnaccidetplantrim> trims =
                                    busqPep.buscarDetallesPorAccion(ax.getIdaccionpao());
                            if (trims != null) {
                                for (Plnaccidetplantrim t : trims) {
                                    dtoAcc.agregarPorRangoMeses(
                                            t.getMesini(),
                                            t.getMesfin(),
                                            t.getActiviprogra());
                                }
                            }

                            Plnaccioneval eval =
                                    busqPep.buscarEvaluacionPorAccion(ax.getIdaccionpao());
                            if (eval != null) {
                                dtoAcc.setPorcentajeCumplimiento(eval.getCumplipct());
                            }

                            dtoInic.getAcciones().add(dtoAcc);
                        }
                    }

                    if (tieneAccionesDelPao) {
                        String persp = dtoInic.getNombrePerspectiva();
                        if (persp == null || persp.isEmpty()) {
                            persp = "GENERAL";
                        }
                        if (!mapa.containsKey(persp)) {
                            mapa.put(persp, new ReportePerspectivaDTO(persp));
                        }
                        mapa.get(persp).getListaIniciativas().add(dtoInic);
                    }
                }
            }

            listaResumen.addAll(mapa.values());

            // ── NUEVO: Buscar Acciones NO planificadas del PAO visualizado ──
            if (paoVisualizado != null && paoVisualizado.getIdpao() != null) {
                List<Plnaccionsinplan> todasNoPlan = busqPep.buscarAccionesNoPlanificadas(paoVisualizado.getIdpao());
                if (todasNoPlan != null) {
                    for (Plnaccionsinplan np : todasNoPlan) {
                        // Solo mostramos las que ya tienen algún porcentaje (evaluadas)
                        if (np.getCumplipct() != null && np.getCumplipct().compareTo(BigDecimal.ZERO) > 0) {
                            this.listaNoPlanificadasEvaluadas.add(np);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar las acciones: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO RESUMEN - Vista General PEP">
    /**
     * CARGA LA MATRIZ ESTRATÉGICA COMPLETA DEL PEP. La query SQL se delega al
     * EJB — sin queries en el bean.
     */
    public void cargarVistaGeneral(javax.faces.event.ActionEvent event) {
        cargarVistaGeneral(); // llama al método real
    }

    /**
     * CARGA LA VISTA GENERAL DEL PEP Y ABRE EL POPUP. ✅ JavascriptContext
     * garantiza que el popup se abra después del render.
     */
    public void cargarVistaGeneral() {
        this.lstArbolCompleto = new ArrayList<ResumenPepDTO>();
        this.totalGlobalPerspectivas = 0;
        this.totalGlobalObjetivos = 0;
        this.totalGlobalIndicadores = 0;
        this.totalGlobalEstrategias = 0;

        if (paoVisualizado == null || paoVisualizado.getIdpep() == null) {
            return;
        }

        try {
            List<Object[]> filas = busqPep.obtenerDetallesPep(
                    paoVisualizado.getIdpep().getIdpep());

            Map<BigDecimal, ResumenPepDTO> mapPersp = new LinkedHashMap<BigDecimal, ResumenPepDTO>();
            Map<BigDecimal, ResumenObjetivoDTO> mapObj = new HashMap<BigDecimal, ResumenObjetivoDTO>();
            Map<BigDecimal, ResumenIndicadorDTO> mapInd = new HashMap<BigDecimal, ResumenIndicadorDTO>();

            for (Object[] row : filas) {
                BigDecimal idPer = (BigDecimal) row[2];
                String nomPer = (String) row[3];
                BigDecimal idObj = (row[4] != null) ? (BigDecimal) row[4] : null;
                String codObj = (String) row[5];
                String descObj = (String) row[6];
                BigDecimal idInd = (row[7] != null) ? (BigDecimal) row[7] : null;
                String codInd = (String) row[8];
                String descInd = (String) row[9];
                String descEst = (row[11] != null) ? (String) row[11] : null;

                ResumenPepDTO dtoPer = mapPersp.get(idPer);
                if (dtoPer == null) {
                    dtoPer = new ResumenPepDTO(nomPer);
                    mapPersp.put(idPer, dtoPer);
                    this.lstArbolCompleto.add(dtoPer);
                }

                if (idObj != null) {
                    ResumenObjetivoDTO dtoObj = mapObj.get(idObj);
                    if (dtoObj == null) {
                        dtoObj = new ResumenObjetivoDTO(codObj, descObj);
                        mapObj.put(idObj, dtoObj);
                        dtoPer.getPlnpepobjetivoList().add(dtoObj);
                    }

                    if (idInd != null) {
                        ResumenIndicadorDTO dtoInd = mapInd.get(idInd);
                        if (dtoInd == null) {
                            dtoInd = new ResumenIndicadorDTO(codInd, descInd);
                            mapInd.put(idInd, dtoInd);
                            dtoObj.getPlnpepindicumplList().add(dtoInd);
                        }

                        if (descEst != null) {
                            dtoInd.getPlnpeplinestrList().add(
                                    new ResumenIndicadorDTO.EstrategiaSimpleDTO(descEst, 0));
                        }
                    }
                }
            }

            this.totalGlobalPerspectivas = this.lstArbolCompleto.size();
            for (ResumenPepDTO p : this.lstArbolCompleto) {
                this.totalGlobalObjetivos += p.getTotalObjetivos();
                this.totalGlobalIndicadores += p.getTotalIndicadores();
                this.totalGlobalEstrategias += p.getTotalEstrategias();
            }

            // ✅ Abrir popup desde el bean — garantizado después del render
            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(),
                    "popupResumenPep.show();");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO UTILIDADES">
    private void showMsg(String msg, int severity) {
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "mensaje.show();");
        this.validar.setMsgValidation(msg, "dialog", severity, null, null, null);
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="METOOS GENERAR REPORTES">
    private void imprimirReportePln(Map parameters, String reporte) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
            String url = request.getContextPath() + "/ImpRpts";

            request.getSession().setAttribute("ds", "jdbc/BankSys");
            request.getSession().setAttribute("url", "/com/coop1/soficoop/pln/reportes/" + reporte + ".jasper");
            request.getSession().setAttribute("parameters", parameters);
            request.getSession().setAttribute("format", "PDF");

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                    "window.open('" + url + "','RptPAO','location=0,menubar=0,resizable=1,status=0,toolbar=0');");

        } catch (Exception ex) {
            this.showMsg("Error al invocar el motor de reportes", ValidaDatos.WARNING);
        }
    }

    /**
     * Contexto: Capa de Control (Managed Bean) Acción vinculada al botón de la
     * vista para imprimir el PAO actual.
     */
    public void imprimirResumenPao() {
        try {
            if (this.paoVisualizado == null || this.paoVisualizado.getIdpao() == null) {
                this.showMsg("Debe cargar los detalles de un PAO para imprimir el resumen.", ValidaDatos.WARNING);
                return;
            }

            Map<String, Object> filtro = new HashMap<String, Object>();
            filtro.put("idPaoSeleccionado", this.paoVisualizado.getIdpao().intValue());

            imprimirReportePln(filtro, "rptPlaneacionPAO");

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al preparar la impresión del PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * Contexto: Capa de Control (Managed Bean) Acción vinculada al botón de la
     * vista para imprimir el PEP actual.
     */
    public void imprimirResumenPep() {
        try {
            if (this.paoVisualizado == null || this.paoVisualizado.getIdpao() == null) {
                this.showMsg("Debe cargar los detalles de un PEP para imprimir el resumen.", ValidaDatos.WARNING);
                return;
            }

            Map<String, Object> filtro = new HashMap<String, Object>();

            filtro.put("idPepSeleccionado", this.paoVisualizado.getIdpep().getIdpep().intValue());

            Integer anio = this.paoVisualizado.getAnio();
            if (anio != null) {
                filtro.put("lineaAnio", anio);
            } else {
                filtro.put("lineaAnio", null);
            }

            imprimirReportePln(filtro, "rptPlaneacionPEP");

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al preparar la impresión del PEP: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GETTERS Y SETTERS">
    public Segsesion getSesion() {
        return sesion;
    }

    public void setSesion(Segsesion sesion) {
        this.sesion = sesion;
    }

    public Rhuempleado getMiEmpleado() {
        return miEmpleado;
    }

    public String getNombreArea() {
        return nombreArea;
    }

    public boolean isTienePaoAsignado() {
        return tienePaoAsignado;
    }

    public BigInteger getSelIdPep() {
        return selIdPep;
    }

    public void setSelIdPep(BigInteger selIdPep) {
        this.selIdPep = selIdPep;
    }

    public BigInteger getSelIdPao() {
        return selIdPao;
    }

    public void setSelIdPao(BigInteger selIdPao) {
        this.selIdPao = selIdPao;
    }

    public List<SelectItem> getListaPeps() {
        return listaPeps;
    }

    public List<SelectItem> getListaPaos() {
        return listaPaos;
    }

    public Plnpao getPaoVisualizado() {
        return paoVisualizado;
    }

    public List<ReportePerspectivaDTO> getListaResumen() {
        return listaResumen;
    }

    public List<ResumenPepDTO> getLstArbolCompleto() {
        return lstArbolCompleto;
    }

    public int getIndiceTab() {
        return indiceTab;
    }

    public void setIndiceTab(int indiceTab) {
        this.indiceTab = indiceTab;
    }

    public int getTotalGlobalPerspectivas() {
        return totalGlobalPerspectivas;
    }

    public int getTotalGlobalObjetivos() {
        return totalGlobalObjetivos;
    }

    public int getTotalGlobalIndicadores() {
        return totalGlobalIndicadores;
    }

    public int getTotalGlobalEstrategias() {
        return totalGlobalEstrategias;
    }

    public List<Plnpao> getListaPaosEncontrados() {
        return listaPaosEncontrados;
    }
    
    public List<Plnaccionsinplan> getListaNoPlanificadasEvaluadas() {
        return listaNoPlanificadasEvaluadas;
    }

    public void setListaNoPlanificadasEvaluadas(List<Plnaccionsinplan> listaNoPlanificadasEvaluadas) {
        this.listaNoPlanificadasEvaluadas = listaNoPlanificadasEvaluadas;
    }
    //</editor-fold>
}