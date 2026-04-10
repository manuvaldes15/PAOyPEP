package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.general.entidades.Genagencia;
import com.coop1.banksys.general.negocio.BusquedaGenLocal;
import com.coop1.banksys.general.utilidades.web.ValidaDatos;
import com.coop1.banksys.login.entidades.Segsesion;
import com.coop1.banksys.rhu.entidades.Rhudepto;
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
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.icefaces.ace.model.table.RowStateMap;

/**
 * Managed Bean para la Consulta Gerencial de Planes Operativos (PAO).
 *
 * Permite a la gerencia consultar y visualizar PAOs por múltiples filtros: -
 * Plan Estratégico (PEP) - Año - Agencia - Departamento/Área - Nombre del
 * Coordinador
 *
 * Incluye visualización de: - Jerarquía de Iniciativas y Acciones -
 * Planificación Trimestral - Evaluaciones de Cumplimiento - Matriz Estratégica
 * del PEP
 *
 * @author ENVY360
 */
@ManagedBean(name = "consultaGerenciaPlan")
@ViewScoped
public class ConsultaGerenciaPlan implements Serializable {

    //<editor-fold defaultstate="collapsed" desc="EJBs y Sesión">
    @EJB
    private BusquedasPepLocal busqPep;
    @EJB
    private BusquedasRhuLocal busqRhu;
    @EJB
    private BusquedaGenLocal busqGen;
    @ManagedProperty(value = "#{login.sesion}")
    private Segsesion sesion;
    private ValidaDatos validar = new ValidaDatos(FacesContext.getCurrentInstance());
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Control de Vista">
    private int indiceTab = 0;
    private RowStateMap stateMapResumen = new RowStateMap();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Filtros de Búsqueda">
    private BigDecimal filtroIdPep;
    private Integer filtroAnio;
    private BigInteger filtroCodAgen;
    private BigDecimal filtroCodDepto;
    private String filtroNombreCoord;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Listas para Combos">
    private List<SelectItem> listaPeps = new ArrayList<SelectItem>();
    private List<SelectItem> listaAnios = new ArrayList<SelectItem>();
    private List<SelectItem> listaAgencias = new ArrayList<SelectItem>();
    private List<SelectItem> listaAreas = new ArrayList<SelectItem>();
    
    private List<Plnaccionsinplan> listaNoPlanificadasEvaluadas = new ArrayList<Plnaccionsinplan>();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Resultados y Visualización">
    private List<Plnpao> listaPaosEncontrados = new ArrayList<Plnpao>();
    private Plnpao paoVisualizado;
    private String nombreAreaPao;
    private List<ReportePerspectivaDTO> listaResumen = new ArrayList<ReportePerspectivaDTO>();
    private List<ResumenPepDTO> lstArbolCompleto = new ArrayList<ResumenPepDTO>();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables KPIs Globales">
    private int totalGlobalPerspectivas;
    private int totalGlobalObjetivos;
    private int totalGlobalIndicadores;
    private int totalGlobalEstrategias;
    private BigDecimal kpiPromedioCumplimiento = BigDecimal.ZERO;
    private BigDecimal kpiCumplimientoEsperado = BigDecimal.ZERO;
    private BigDecimal kpiPromedioNoPlaneado = BigDecimal.ZERO;
    private BigDecimal kpiPromedioTotal = BigDecimal.ZERO;
    private int kpiTotalAcciones = 0;
    private int kpiTotalAccionesEvaluadas = 0;
    private int kpiTotalNoPlanificadas = 0;
    private int kpiNoPlanificadasEvaluadas = 0;
    private int kpiNoPlanificadasSinEval = 0;

    public BigDecimal getKpiPromedioCumplimiento() {
        return kpiPromedioCumplimiento;
    }

    public BigDecimal getKpiCumplimientoEsperado() {
        return kpiCumplimientoEsperado;
    }

    public BigDecimal getKpiPromedioNoPlaneado() {
        return kpiPromedioNoPlaneado;
    }

    public BigDecimal getKpiPromedioTotal() {
        return kpiPromedioTotal;
    }

    public int getKpiTotalAcciones() {
        return kpiTotalAcciones;
    }

    public int getKpiTotalAccionesEvaluadas() {
        return kpiTotalAccionesEvaluadas;
    }

    public int getKpiTotalNoPlanificadas() {
        return kpiTotalNoPlanificadas;
    }

    public int getKpiNoPlanificadasEvaluadas() {
        return kpiNoPlanificadasEvaluadas;
    }

    public int getKpiNoPlanificadasSinEval() {
        return kpiNoPlanificadasSinEval;
    }

    public int getKpiPromedioCumplimientoInt() {
        return kpiPromedioCumplimiento != null
                ? kpiPromedioCumplimiento.intValue() : 0;
    }

    public int getKpiCumplimientoEsperadoInt() {
        return kpiCumplimientoEsperado != null
                ? kpiCumplimientoEsperado.intValue() : 0;
    }

    public int getKpiPromedioNoPlaneadoInt() {
        return kpiPromedioNoPlaneado != null
                ? kpiPromedioNoPlaneado.intValue() : 0;
    }

    public int getKpiPromedioTotalInt() {
        return kpiPromedioTotal != null
                ? kpiPromedioTotal.intValue() : 0;
    }

    /**
     * CALCULA LOS KPIs DE CUMPLIMIENTO DEL PAO VISUALIZADO. - Promedio de
     * cumplimiento (acciones planificadas evaluadas) - Cumplimiento esperado
     * (acciones con evaluación / total acciones * 100) - Promedio no planeado
     * (acciones no planificadas) - Promedio total (todas las acciones)
     */
/**
     * CALCULA LOS KPIs DE CUMPLIMIENTO DEL PAO VISUALIZADO. 
     */
    private void calcularKpis() {
        kpiPromedioCumplimiento = BigDecimal.ZERO;
        kpiCumplimientoEsperado = new BigDecimal(100);
        kpiPromedioNoPlaneado = BigDecimal.ZERO;
        kpiPromedioTotal = BigDecimal.ZERO;
        kpiTotalAcciones = 0;
        kpiTotalAccionesEvaluadas = 0;
        kpiTotalNoPlanificadas = 0;
        kpiNoPlanificadasEvaluadas = 0;
        kpiNoPlanificadasSinEval = 0;
        
        // Limpiamos la lista cada vez que se calcula un nuevo PAO
        this.listaNoPlanificadasEvaluadas = new ArrayList<Plnaccionsinplan>();

        if (paoVisualizado == null || paoVisualizado.getIdpao() == null) {
            return;
        }

        try {
            // ── Acciones planificadas ─────────────────────────────────────────
            List<Plnacciondeta> planificadas = busqPep.buscarAccionesPorPao(paoVisualizado.getIdpao());

            BigDecimal sumaPlanificadas = BigDecimal.ZERO;
            int countEvaluadas = 0;

            if (planificadas != null) {
                kpiTotalAcciones = planificadas.size();

                for (Plnacciondeta ax : planificadas) {
                    List<Plnaccioneval> evals = busqPep.buscarEvaluacionesPorAccion(ax.getIdaccionpao());

                    if (evals != null && !evals.isEmpty()) {
                        Plnaccioneval ultima = evals.get(evals.size() - 1);
                        if (ultima.getCumplipct() != null) {
                            sumaPlanificadas = sumaPlanificadas.add(ultima.getCumplipct());
                            countEvaluadas++;
                        }
                    }
                }
            }

            kpiTotalAccionesEvaluadas = countEvaluadas;
            if (countEvaluadas > 0) {
                kpiPromedioCumplimiento = sumaPlanificadas
                        .divide(new BigDecimal(countEvaluadas), 2, BigDecimal.ROUND_HALF_UP);
            }

            // ── Acciones NO planificadas ──────────────────────────────────────
            List<Plnaccionsinplan> todasNoPlan = busqPep.buscarAccionesNoPlanificadas(paoVisualizado.getIdpao());

            BigDecimal sumaNoPlaneadas = BigDecimal.ZERO;
            int countNoPlanEvaluadas = 0;
            int countNoPlanSinEval = 0;

            if (todasNoPlan != null) {
                kpiTotalNoPlanificadas = todasNoPlan.size();

                for (Plnaccionsinplan np : todasNoPlan) {
                    if (np.getCumplipct() != null && np.getCumplipct().compareTo(BigDecimal.ZERO) > 0) {
                        sumaNoPlaneadas = sumaNoPlaneadas.add(np.getCumplipct());
                        countNoPlanEvaluadas++;
                        
                        // AGREGAMOS A LA LISTA PARA LA VISTA DE GERENCIA
                        this.listaNoPlanificadasEvaluadas.add(np);
                        
                    } else {
                        countNoPlanSinEval++;
                    }
                }
            }

            kpiNoPlanificadasEvaluadas = countNoPlanEvaluadas;
            kpiNoPlanificadasSinEval = countNoPlanSinEval;

            if (countNoPlanEvaluadas > 0) {
                kpiPromedioNoPlaneado = sumaNoPlaneadas
                        .divide(new BigDecimal(countNoPlanEvaluadas), 2, BigDecimal.ROUND_HALF_UP);
            }

            // ── Promedio total ────────────────────────────────────────────────
            int totalCount = countEvaluadas + countNoPlanEvaluadas;
            BigDecimal sumaTotal = sumaPlanificadas.add(sumaNoPlaneadas);

            if (totalCount > 0) {
                kpiPromedioTotal = sumaTotal
                        .divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al calcular KPIs: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructor e Inicialización">
    @PostConstruct
    public void init() {
        cargarListasIniciales();
        //calcularKpis();
    }

    /**
     * CARGA LAS LISTAS INICIALES PARA LOS COMBOS DE FILTROS. Incluye: Años,
     * PEPs, Agencias y opción inicial de Áreas.
     */
    private void cargarListasIniciales() {

        listaAnios = new ArrayList<SelectItem>();
        listaAnios.add(new SelectItem(0, "-- TODOS --"));

        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = anioActual + 1; i >= anioActual - 5; i--) {
            listaAnios.add(new SelectItem(i, String.valueOf(i)));
        }
        this.filtroAnio = anioActual;

        //filtrar oir PEP
        listaPeps = new ArrayList<SelectItem>();
        try {
            Map<String, Object> f = new HashMap<String, Object>();
            List<Plnpep> peps = busqPep.buscarPeps(f);
            if (peps != null) {
                for (Plnpep p : peps) {
                    String label = (p.getDescrip() != null) ? p.getDescrip() : "PEP " + p.getIdpep();
                    listaPeps.add(new SelectItem(p.getIdpep(), label));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos del PEP: " + e.getMessage(), ValidaDatos.ERROR);

        }

        //filtrar Agencias
        listaAgencias = new ArrayList<SelectItem>();
        try {
            List<Genagencia> agencias = busqGen.obtenerAgencias();
            if (agencias != null) {
                for (Genagencia ag : agencias) {
                    listaAgencias.add(new SelectItem(ag.getCodagen(), ag.getNombre()));
                }
            }
        } catch (Exception e) {
            listaAgencias.add(new SelectItem(BigInteger.ZERO, "Error cargando agencias"));
        }

        // carga Áreas por agencia
        listaAreas = new ArrayList<SelectItem>();
        listaAreas.add(new SelectItem(BigDecimal.ZERO, "-- Seleccione Agencia --"));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO FILTROS - Eventos en Cascada">
    /**
     * EVENTO DE CAMBIO DE AGENCIA. Carga las áreas/departamentos de la agencia
     * seleccionada.
     */
    public void cambioAgencia(ValueChangeEvent event) {
        Object valor = event.getNewValue();
        this.filtroCodAgen = null;

        if (valor != null) {
            if (valor instanceof BigInteger) {
                this.filtroCodAgen = (BigInteger) valor;
            } else if (valor instanceof Number) {
                this.filtroCodAgen = BigInteger.valueOf(((Number) valor).longValue());
            } else if (!valor.toString().isEmpty()) {
                this.filtroCodAgen = new BigInteger(valor.toString());
            }
        }

        if (this.filtroCodAgen == null || this.filtroCodAgen.equals(BigInteger.ZERO)) {
            this.filtroCodAgen = BigInteger.ZERO;
            this.filtroCodDepto = null;
            this.listaAreas = new ArrayList<SelectItem>();
            this.listaAreas.add(new SelectItem(0, "-- Todas --"));
        } else {
            this.filtroCodDepto = null;
            cargarAreasPorAgencia(this.filtroCodAgen);
        }
    }

    /**
     * CARGA LAS ÁREAS/DEPARTAMENTOS DE UNA AGENCIA ESPECÍFICA.
     *
     * @param idAgencia Identificador de la agencia seleccionada.
     */
    private void cargarAreasPorAgencia(BigInteger idAgencia) {
        listaAreas = new ArrayList<SelectItem>();
        listaAreas.add(new SelectItem(0, "-- Todas --"));

        try {
            if (idAgencia != null && !idAgencia.equals(BigInteger.ZERO)) {
                Map<String, Object> filtros = new HashMap<String, Object>();
                filtros.put("codagen", idAgencia);

                List<Rhudepto> deptos = busqRhu.buscarDeptoGeneral(filtros);

                if (deptos != null) {
                    for (Rhudepto d : deptos) {
                        BigInteger codDepto = d.getRhudeptoPK().getCoddepto();
                        listaAreas.add(new SelectItem(new BigDecimal(codDepto), d.getDescdepto()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos de las areas por agencia " + e.getMessage(), ValidaDatos.ERROR);

        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO BÚSQUEDA - Consulta de PAOs">
    /**
     * BUSCA PAOs APLICANDO LOS FILTROS SELECCIONADOS. Filtra por: PEP, Año,
     * Agencia, Departamento y Nombre del Coordinador.
     */
    public void buscarPaos() {
        paoVisualizado = null;
        listaPaosEncontrados = new ArrayList<Plnpao>();

        try {
            Map<String, Object> filtros = new HashMap<String, Object>();

            if (filtroIdPep != null && !BigDecimal.ZERO.equals(filtroIdPep)) {
                filtros.put("idPep", filtroIdPep);
            }

            if (filtroAnio != null && filtroAnio > 0) {
                filtros.put("anio", filtroAnio);
            }

            if (filtroCodAgen != null && !BigInteger.ZERO.equals(filtroCodAgen)) {
                filtros.put("codAgen", new BigDecimal(filtroCodAgen));
            }

            if (filtroCodDepto != null && !BigDecimal.ZERO.equals(filtroCodDepto)) {
                filtros.put("codDepto", filtroCodDepto);
            }

            if (filtroNombreCoord != null && !filtroNombreCoord.trim().isEmpty()) {
                filtros.put("nombreCoord", filtroNombreCoord.toUpperCase());
            }

            listaPaosEncontrados = busqPep.buscarPaosDinamico(filtros);

            if (listaPaosEncontrados == null || listaPaosEncontrados.isEmpty()) {
                showMsg("No se encontraron resultados con los filtros aplicados.", ValidaDatos.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al buscar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO SELECCIÓN Y DETALLE - Visualización de PAO">
    /**
     * SELECCIONA UN PAO PARA VISUALIZAR SU DETALLE COMPLETO. Carga la jerarquía
     * de Perspectivas -> Iniciativas -> Acciones.
     *
     * @param paoSelect PAO seleccionado desde la tabla de resultados.
     */
    public void seleccionarPao(Plnpao paoSelect) {
        this.paoVisualizado = paoSelect;

        try {
            if (paoSelect.getIdcoordinador().getPuesto() != null && paoSelect.getIdcoordinador().getPuesto().getRhudepto() != null) {
                this.nombreAreaPao = paoSelect.getIdcoordinador().getPuesto().getRhudepto().getDescdepto();
            } else {
                this.nombreAreaPao = "Área de " + paoSelect.getIdcoordinador().getPersona().getNomcom();
            }
        } catch (Exception e) {
            this.nombreAreaPao = "Sin Datos";
        }

        cargarResumenGeneral();
        calcularKpis();
        this.indiceTab = 1;
    }

    /**
     * EVENTO DE SELECCIÓN DE FILA EN LA TABLA DE PAOs. Patrón idéntico a
     * mttoPao.seleccionarPaoDeTabla y consultaEmpleadosPlan.seleccionarPao
     */
    public void seleccionarPaoDeTabla(org.icefaces.ace.event.SelectEvent event) {
        Plnpao paoSelect = (Plnpao) event.getObject();
        if (paoSelect != null) {
            seleccionarPao(paoSelect);
        }
    }

    /**
     * CARGA EL RESUMEN GENERAL DEL PAO AGRUPADO POR PERSPECTIVA. Estructura:
     * Perspectiva -> Iniciativa -> Acción (con cronograma y evaluación).
     */
/**
     * CARGA EL RESUMEN GENERAL DEL PAO AGRUPADO POR PERSPECTIVA. Estructura:
     * Perspectiva -> Iniciativa -> Acción (con cronograma y evaluación).
     * CLON EXACTO DE LA LÓGICA DE EMPLEADOS PARA EVITAR CRUCE DE DATOS.
     */
    private void cargarResumenGeneral() {
        listaResumen = new ArrayList<ReportePerspectivaDTO>();
        stateMapResumen = new RowStateMap();
        Map<String, ReportePerspectivaDTO> mapaPerspectivas = new LinkedHashMap<String, ReportePerspectivaDTO>();

        if (paoVisualizado == null || paoVisualizado.getIdpao() == null) {
            return;
        }

        try {
            List<Plnpaoinic> iniciativas = busqPep.buscarIniciativasPorPep(paoVisualizado.getIdpep().getIdpep());

            if (iniciativas != null) {
                for (Plnpaoinic inic : iniciativas) {

                    ReporteIniciativaDTO dtoInic = new ReporteIniciativaDTO(inic);
                    boolean tieneAccionesDelPao = false;

                    List<Plnacciondeta> accionesRaw = busqPep.buscarAccionesPorIniciativa(inic.getIdiniciativa());

                    if (accionesRaw != null) {
                        for (Plnacciondeta ax : accionesRaw) {
                            // ✅ CORRECCIÓN: Uso de .equals() idéntico al de Empleados para asegurar que
                            // solo se carguen las acciones exactas de este PAO.
                            if (ax.getIdpao() == null || !ax.getIdpao().getIdpao().equals(paoVisualizado.getIdpao())) {
                                continue;
                            }

                            tieneAccionesDelPao = true;
                            ReporteAccionDTO dtoAcc = new ReporteAccionDTO(ax);

                            cargarDetallesAccion(ax, dtoAcc);

                            dtoInic.getAcciones().add(dtoAcc);
                        }
                    }

                    if (tieneAccionesDelPao) {
                        String nomPersp = dtoInic.getNombrePerspectiva();
                        if (nomPersp == null || nomPersp.isEmpty()) {
                            nomPersp = "GENERAL";
                        }

                        if (!mapaPerspectivas.containsKey(nomPersp)) {
                            mapaPerspectivas.put(nomPersp, new ReportePerspectivaDTO(nomPersp));
                        }
                        mapaPerspectivas.get(nomPersp).getListaIniciativas().add(dtoInic);
                    }
                }
            }

            listaResumen.addAll(mapaPerspectivas.values());

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error cargando detalle de acciones: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * CARGA LOS DETALLES DE UNA ACCIÓN ESPECÍFICA. 
     * Lógica restaurada para coincidir con la vista funcional de Empleados.
     */
    private void cargarDetallesAccion(Plnacciondeta ax, ReporteAccionDTO dtoAcc) {
        try {
            List<Plnaccidetplantrim> trims = busqPep.buscarDetallesPorAccion(ax.getIdaccionpao());
            if (trims != null) {
                for (Plnaccidetplantrim t : trims) {
                    dtoAcc.agregarPorRangoMeses(
                            t.getMesini(),
                            t.getMesfin(),
                            t.getActiviprogra());
                }
            }

            Plnaccioneval eval = busqPep.buscarEvaluacionPorAccion(ax.getIdaccionpao());
            if (eval != null) {
                dtoAcc.setPorcentajeCumplimiento(eval.getCumplipct());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos de la acción:  " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO RESUMEN - Vista General PEP">
    /**
     * CARGA LA MATRIZ ESTRATÉGICA COMPLETA DEL PEP. La query SQL se delega al
     * EJB — sin queries en el bean.
     */
    public void cargarVistaGeneral() {
        calcularKpis();

        this.lstArbolCompleto = new ArrayList<ResumenPepDTO>();
        this.totalGlobalPerspectivas = 0;
        this.totalGlobalObjetivos = 0;
        this.totalGlobalIndicadores = 0;
        this.totalGlobalEstrategias = 0;

        if (paoVisualizado == null || paoVisualizado.getIdpep() == null) {
            showMsg("Hubo un error en la selección de este PAO.", ValidaDatos.WARNING);
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
                            ResumenIndicadorDTO.EstrategiaSimpleDTO est =
                                    new ResumenIndicadorDTO.EstrategiaSimpleDTO(descEst, 0);
                            dtoInd.getPlnpeplinestrList().add(est);
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

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos del PEP: " + e.getMessage(), ValidaDatos.ERROR);

        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO UTILIDADES">
    /**
     * MUESTRA UN MENSAJE AL USUARIO.
     *
     * @param msg Mensaje a mostrar.
     * @param severity Nivel de severidad (INFO, WARNING, ERROR).
     */
    private void showMsg(String msg, int severity) {
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "mensaje.show();");
        this.validar.setMsgValidation(msg, "dialog", severity, null, null, null);
    }

    /**
     * LIMPIA TODOS LOS FILTROS Y RESULTADOS. Reinicia la vista al estado
     * inicial.
     */
    public void limpiarTodo() {
        paoVisualizado = null;
        listaPaosEncontrados = new ArrayList<Plnpao>();
        filtroIdPep = null;
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        filtroAnio = anioActual;
        filtroCodAgen = null;
        filtroCodDepto = null;
        filtroNombreCoord = "";
        this.listaAreas = new ArrayList<SelectItem>();
        this.listaAreas.add(new SelectItem(BigDecimal.ZERO, "-- Seleccione Agencia --"));
        this.indiceTab = 0;
    }

    /**
     * CIERRA LA VISTA DE DETALLE DEL PAO. Limpia el PAO visualizado y sus
     * resúmenes.
     */
    public void cerrarDetalle() {
        this.paoVisualizado = null;
        this.listaResumen.clear();
        this.indiceTab = 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GETTERS Y SETTERS ">
    public Segsesion getSesion() {
        return sesion;
    }

    public void setSesion(Segsesion sesion) {
        this.sesion = sesion;
    }

    public int getIndiceTab() {
        return indiceTab;
    }

    public void setIndiceTab(int indiceTab) {
        this.indiceTab = indiceTab;
    }

    public RowStateMap getStateMapResumen() {
        return stateMapResumen;
    }

    public void setStateMapResumen(RowStateMap stateMapResumen) {
        this.stateMapResumen = stateMapResumen;
    }

    public BigDecimal getFiltroIdPep() {
        return filtroIdPep;
    }

    public void setFiltroIdPep(BigDecimal filtroIdPep) {
        this.filtroIdPep = filtroIdPep;
    }

    public Integer getFiltroAnio() {
        return filtroAnio;
    }

    public void setFiltroAnio(Integer filtroAnio) {
        this.filtroAnio = filtroAnio;
    }

    public BigInteger getFiltroCodAgen() {
        return filtroCodAgen;
    }

    public void setFiltroCodAgen(BigInteger filtroCodAgen) {
        this.filtroCodAgen = filtroCodAgen;
    }

    public BigDecimal getFiltroCodDepto() {
        return filtroCodDepto;
    }

    public void setFiltroCodDepto(BigDecimal filtroCodDepto) {
        this.filtroCodDepto = filtroCodDepto;
    }

    public String getFiltroNombreCoord() {
        return filtroNombreCoord;
    }

    public void setFiltroNombreCoord(String filtroNombreCoord) {
        this.filtroNombreCoord = filtroNombreCoord;
    }

    public List<SelectItem> getListaPeps() {
        return listaPeps;
    }

    public List<SelectItem> getListaAnios() {
        return listaAnios;
    }

    public List<SelectItem> getListaAgencias() {
        return listaAgencias;
    }

    public List<SelectItem> getListaAreas() {
        if (listaAreas == null || listaAreas.isEmpty()) {
            listaAreas = new ArrayList<SelectItem>();
            listaAreas.add(new SelectItem(BigDecimal.ZERO, "-- Seleccione Agencia --"));
        }
        return listaAreas;
    }

    public List<Plnpao> getListaPaosEncontrados() {
        return listaPaosEncontrados;
    }

    public Plnpao getPaoVisualizado() {
        return paoVisualizado;
    }

    public String getNombreAreaPao() {
        return nombreAreaPao;
    }

    public List<ReportePerspectivaDTO> getListaResumen() {
        return listaResumen;
    }

    public List<ResumenPepDTO> getLstArbolCompleto() {
        return lstArbolCompleto;
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
    
    public List<Plnaccionsinplan> getListaNoPlanificadasEvaluadas() {
        return listaNoPlanificadasEvaluadas;
    }

    public void setListaNoPlanificadasEvaluadas(List<Plnaccionsinplan> listaNoPlanificadasEvaluadas) {
        this.listaNoPlanificadasEvaluadas = listaNoPlanificadasEvaluadas;
    }
    //</editor-fold>

    /**
     * Contexto: Capa de Control (Managed Bean) Motor de impresión adaptado para
     * el módulo PLN (Planificación). Inyecta los atributos en sesión y abre el
     * servlet ImpRpts en un popup.
     */
    private void imprimirReportePln(Map parameters, String reporte) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
            String url = request.getContextPath() + "/ImpRpts";

            request.getSession().setAttribute("ds", "jdbc/BankSys");
            // RUTA CORREGIDA PARA EL MÓDULO PAO
            request.getSession().setAttribute("url", "/com/coop1/soficoop/pln/reportes/" + reporte + ".jasper");
            request.getSession().setAttribute("parameters", parameters);
            request.getSession().setAttribute("format", "PDF");

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                    "window.open('" + url + "','Rpt','location=0,menubar=0,resizable=1,status=0,toolbar=0');");

        } catch (Exception ex) {
            this.showMsg("Error en llamar reporte", ValidaDatos.WARNING);
            //this.showMsgLog(ex, ValidaDatos.WARNING); // Usa tu método log si existe
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
            // El parámetro exacto que declaramos en tu archivo .jrxml
            filtro.put("idPaoSeleccionado", this.paoVisualizado.getIdpao().intValue());

            // Llamamos al motor apuntando al archivo RptCumplimientoOE.jasper
            imprimirReportePln(filtro, "rptPlaneacionPAO");

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al preparar la impresión del PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

 

    public void imprimirResumenPep() {
        try {
            if (this.paoVisualizado == null || this.paoVisualizado.getIdpao() == null) {
                this.showMsg("Debe cargar los detalles de un PEP para imprimir el resumen.", ValidaDatos.WARNING);
                return;
            }

            Map<String, Object> filtro = new HashMap<String, Object>();

            // Se asume que getIdpep() devuelve BigDecimal o BigInteger que nunca es nulo por la validación superior
            filtro.put("idPepSeleccionado", this.paoVisualizado.getIdpep().getIdpep().intValue());

            // CORRECCIÓN DEL NPE: Verificación de seguridad (Null-Check) para evitar el error de Unboxing
            Integer anio = this.paoVisualizado.getAnio();
            if (anio != null) {
                filtro.put("lineaAnio", anio);
            } else {
                filtro.put("lineaAnio", null); // Enviar nulo para que Jasper/SQL decida, o usar 0 si tu query requiere un número
            }

            // CORRECCIÓN TIPOGRÁFICA: rptPlaneacionPEP (Asegúrate de que el archivo .jasper se llame exactamente así)
            imprimirReportePln(filtro, "rptPlaneacionPEP");

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al preparar la impresión del PEP: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
}