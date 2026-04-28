package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.general.constantes.CorPantallas;
import com.coop1.banksys.general.constantes.Modulo;
import com.coop1.banksys.general.excepciones.ValidacionExcepcion;
import com.coop1.banksys.general.utilidades.web.ValidaDatos;
import com.coop1.banksys.login.entidades.Segsesion;
import com.coop1.banksys.login.entidades.Segusuario;
import com.coop1.banksys.rhu.entidades.Rhuempleado;
import com.coop1.banksys.rhu.negocio.BusquedasRhuLocal;
import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import com.coop1.soficoop.pln.dto.*;
import com.coop1.soficoop.pln.entidades.*;
import com.icesoft.faces.context.effects.JavascriptContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.icefaces.ace.event.RowEditEvent;
import org.icefaces.ace.event.SelectEvent;
import org.icefaces.ace.model.table.RowStateMap;
import com.coop1.banksys.general.entidades.Genconfig;
import com.coop1.banksys.general.negocio.BusquedaGenLocal;
import java.util.logging.Level;

/**
 * Managed Bean para el Mantenimiento del Módulo PAO (Plan Anual Operativo).
 *
 * Gestiona: - PAO (Plan Anual Operativo) - Acciones del PAO - Acciones No
 * Planificadas - Planificación Trimestral - Evaluaciones - Seguimientos -
 * Iniciativas - Dashboard y Resúmenes
 *
 * @author ENVY360
 */
@ManagedBean(name = "mttoPao")
@SessionScoped
public class MttoPao implements Serializable {

    //<editor-fold defaultstate="collapsed" desc="Constantes">
    private static final int DEL_PAO = 1;
    private static final int DEL_ACCION = 2;
    private static final int DEL_TAREA = 3;
    private static final int DEL_INICIATIVA = 4;
    private static final BigDecimal UMBRAL_CUMPLIMIENTO = new BigDecimal(80);//PONER EN GENCONFIG PARA EL UMBRAL DEL CUMPLIMIENTO 
    private static final int MAX_LONGITUD_TEXTO = 1000;
    private static final long serialVersionUID = 1L;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="EJBs y Sesión">
    @EJB
    private BusquedasPepLocal busqPep;
    @EJB
    private AdministracionPepLocal adminPep;
    @EJB
    private BusquedaGenLocal busqGen;
    @EJB
    private BusquedasRhuLocal busqRhu;
    @ManagedProperty(value = "#{login.sesion}")
    private Segsesion sesion;
    private ValidaDatos validar = new ValidaDatos(FacesContext.getCurrentInstance());
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Control General">
    private int cambiarTagPanel = 0;
    private int tipoEliminacion = 0;
    private String mensajeConfirmacion = "";
    private boolean panelAccionesVisible = false;
    private boolean nuevoPao = false;
    private boolean panelEvaluacionVisible = false;
    private boolean editandoAccion = false;
    
    private BigDecimal umbralCumplimiento;
    private String fechaLimiteQ1;
    private String fechaLimiteQ2;
    private String fechaLimiteQ3;
    private String fechaLimiteQ4;

    public String getFechaLimiteQ1() {
        return fechaLimiteQ1;
    }

    public String getFechaLimiteQ2() {
        return fechaLimiteQ2;
    }

    public String getFechaLimiteQ3() {
        return fechaLimiteQ3;
    }

    public String getFechaLimiteQ4() {
        return fechaLimiteQ4;
    }
    
    public java.util.Date getFechaActual() {
        return new java.util.Date();
    }
    
    


    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables RHU y Contexto Usuario">
    private BigInteger miCodemp;
    private Rhuempleado miEmpleado;
    private String txtNombreUsuario = "";
    private String txtAgencia = "";
    private String txtArea = "";
    private String txtPuesto = "";
    private String txtNombreJefe = "";
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables PAO">
    private Plnpao paoActual = new Plnpao();
    private List<Plnpao> listaPaos = new ArrayList<Plnpao>();
    private Plnpao paoAEliminar;
    private BigInteger selIdPepCabecera;
    private RowStateMap smPao = new RowStateMap();
    // Filtros de búsqueda PAO
    private Integer filtroAnioPao;
    private BigInteger filtroIdPepPao;
    private String filtroNombrePao;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Jerarquía PEP (Para Acciones)">
    private BigInteger selIdPep;
    private BigInteger selIdPerspectiva;
    private BigInteger selIdObjetivo;
    private BigInteger selIdIndicador;
    private BigInteger selIdEstrategia;
    // Listas para Combos
    private List<SelectItem> listaPeps = new ArrayList<SelectItem>();
    private List<SelectItem> listaPerspectivas = new ArrayList<SelectItem>();
    private List<SelectItem> listaObjetivos = new ArrayList<SelectItem>();
    private List<SelectItem> listaIndicadores = new ArrayList<SelectItem>();
    private List<SelectItem> listaEstrategias = new ArrayList<SelectItem>();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Acciones del PAO">
    private Plnacciondeta accion = new Plnacciondeta();
    private List<Plnacciondeta> listaAcciones = new ArrayList<Plnacciondeta>();
    private Plnacciondeta accionAEliminar;
    private Plnacciondeta accionPadreSeleccionada;
    private RowStateMap smAccion = new RowStateMap();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Acciones No Planificadas">
    private List<Plnaccionsinplan> listaNoPlanificadas = new ArrayList<Plnaccionsinplan>();
    private Plnaccionsinplan accionNoPlanActual = new Plnaccionsinplan();
    private List<Plnaccionsinplan> listaNoPlanificadasEvaluadas = new ArrayList<Plnaccionsinplan>();
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Variables Evaluaciones">
    private Plnaccioneval evaluacionActual = new Plnaccioneval();
    private List<Plnacciondeta> listaAccionesParaEvaluar = new ArrayList<Plnacciondeta>();
    private List<Plnacciondeta> listaAccionesParaEvaluarMaster;
    private List<Plnaccioneval> listaEvaluacionesRealizadas = new ArrayList<Plnaccioneval>();
    // Filtros jerarquía para evaluaciones
    private BigInteger filtroIdPerspectiva;
    private BigInteger filtroIdObjetivo;
    private BigInteger filtroIdIndicador;
    private BigInteger filtroIdEstrategia;
    private String filtroPeriodoEvaluacion;
    private String filtroPerioEval = "TODOS";

    public String getFiltroPerioEval() {
        return filtroPerioEval;
    }

    public void setFiltroPerioEval(String filtroPerioEval) {
        this.filtroPerioEval = filtroPerioEval;
    }
    // Combos jerárquicos para evaluación
    private List<SelectItem> listaPerspectivasEval = new ArrayList<SelectItem>();
    private List<SelectItem> listaObjetivosEval = new ArrayList<SelectItem>();
    private List<SelectItem> listaIndicadoresEval = new ArrayList<SelectItem>();
    private List<SelectItem> listaEstrategiasEval = new ArrayList<SelectItem>();
    //</editor-fold>
    private List<Plnaccidetplantrim> listaDetallesEvaluar = new ArrayList<>();
    private List<SelectItem> listaPeriodosEvaluacion = new ArrayList<SelectItem>();
    private List<Plnaccioneval> listaEvaluacionesDeAccionActual = new ArrayList<>();

// Getter
    public List<Plnaccioneval> getListaEvaluacionesDeAccionActual() {
        return listaEvaluacionesDeAccionActual;
    }
    //<editor-fold defaultstate="collapsed" desc="Variables Seguimientos">
    private Plnaccionseguimiento seguimientoActual = new Plnaccionseguimiento();
    private List<Plnaccionseguimiento> historialSeguimientos = new ArrayList<Plnaccionseguimiento>();
    private List<Plnacciondeta> listaCriticasPendientes = new ArrayList<Plnacciondeta>();
    private List<Plnaccionseguimiento> listaSeguimientosYaRealizados = new ArrayList<Plnaccionseguimiento>();
    private List<Plnacciondeta> listaAccionesBajoCumplimiento = new ArrayList<Plnacciondeta>();
    public String periodoFiltroReporte;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Planificación Trimestral">
    private List<Plnaccidetplantrim> listaDetallesTrim = new ArrayList<Plnaccidetplantrim>();
    private Plnaccidetplantrim detalleTrimActual = new Plnaccidetplantrim();
    private Plnaccidetplantrim tareaAEliminar;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Iniciativas">
    // Variables para el CRUD de iniciativas
    private List<Plnpaoinic> listaIniciativasPao = new ArrayList<Plnpaoinic>();
    private Plnpaoinic iniciativaActual = new Plnpaoinic();
    private Plnpaoinic iniciativaAEliminar;
    private boolean editandoIniciativa = false;
    // Variables para vinculación de acciones a iniciativas
    private List<Plnacciondeta> listaAccionesVinculadas = new ArrayList<Plnacciondeta>();
    private List<Plnacciondeta> listaAccionesDisponibles = new ArrayList<Plnacciondeta>();
    // Variables de filtro en cascada para iniciativas
    private BigInteger filtroInicIdPerspectiva;
    private BigInteger filtroInicIdObjetivo;
    private BigInteger filtroInicIdIndicador;
    // Listas para los combos de iniciativas
    private List<SelectItem> listaInicPerspectivas = new ArrayList<SelectItem>();
    private List<SelectItem> listaInicObjetivos = new ArrayList<SelectItem>();
    private List<SelectItem> listaInicIndicadores = new ArrayList<SelectItem>();
    //</editor-fold>
    private List<ReporteIniciativaDTO> listaResumenIniciativas = new ArrayList<>();

    public List<ReporteIniciativaDTO> getListaResumenIniciativas() {
        return listaResumenIniciativas;
    }
    //<editor-fold defaultstate="collapsed" desc="Variables Dashboard y DTOs">
    private ResumenJerarquiaDTO resumenJerarquia = new ResumenJerarquiaDTO();
    private List<ResumenCumplimientoDTO> listaDashboardOE = new ArrayList<ResumenCumplimientoDTO>();
    private List<ResumenPaoDTO> listaResumen = new ArrayList<ResumenPaoDTO>();
    private RowStateMap stateMapResumen = new RowStateMap();
    private List<ReporteIniciativaDTO> listaReporteEjecutivo = new ArrayList<>();
    private List<ReportePerspectivaDTO> listaReportePorPerspectiva = new ArrayList<>();
    private List<ResumenPepDTO> lstArbolCompleto = new ArrayList<ResumenPepDTO>();
    // KPIs Globales
    private int totalGlobalPerspectivas;
    private int totalGlobalObjetivos;
    private int totalGlobalIndicadores;
    private int totalGlobalEstrategias;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructor e Inicialización">
    public MttoPao() {
    }

    @PostConstruct
    public void init() {
        inicializarEstructuras();
        cargarConfiguracionesBD();
        cargarContextoUsuario();
        cargarPaosDelUsuario();
        cargarPeps();
        getListaResumenIniciativas();
        listaAcciones.clear();
        listaNoPlanificadas.clear();
    }

    /**
     * INICIALIZA ESTRUCTURAS DE DATOS Y VARIABLES DE CONTROL.
     */
    private void inicializarEstructuras() {
        paoActual = new Plnpao();
        accion = new Plnacciondeta();

        listaPaos = new ArrayList<Plnpao>();
        listaAcciones = new ArrayList<Plnacciondeta>();

        listaPeps = new ArrayList<SelectItem>();
        listaPerspectivas = new ArrayList<SelectItem>();
        listaObjetivos = new ArrayList<SelectItem>();
        listaIndicadores = new ArrayList<SelectItem>();
        listaEstrategias = new ArrayList<SelectItem>();
        
        panelAccionesVisible = false;
        cambiarTagPanel = 0;
        smPao = new RowStateMap();
        
        //    this.umbralCumplimiento = new BigDecimal("80");
        //    this.fechaLimiteQ1 = "15/04";
        //    this.fechaLimiteQ2 = "15/07";
        //    this.fechaLimiteQ3 = "15/10";
        //    this.fechaLimiteQ4 = "15/01";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Carga de Contexto Usuario (RHU)">
    /**
     * CARGA LA INFORMACIÓN DEL USUARIO ACTUAL DESDE RHU. Obtiene datos del
     * empleado, jefe, agencia, área y puesto.
     */
    private void cargarContextoUsuario() {
        cargarPeps();

        try {
            if (sesion != null && sesion.getSegusuario() != null) {
                Segusuario u = sesion.getSegusuario();
                if (u.getGenpersona() != null && u.getGenpersona().getCodper() != null) {
                    miCodemp = new BigInteger(u.getGenpersona().getCodper().toString());
                } else if (u.getCodusr() != null) {
                    try {
                        miCodemp = new BigInteger(u.getCodusr().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showMsg("Error al cargar los datos del usuario: " + e.getMessage(), ValidaDatos.ERROR);
                    }
                }
            }

            miEmpleado = busqRhu.buscarEmpleado(miCodemp);

            if (miEmpleado != null) {
                txtNombreUsuario = miEmpleado.getPersona().getNomcom();
                txtAgencia = (miEmpleado.getAgencia() != null) ? miEmpleado.getAgencia().getNombre() : "No asignado";
                txtPuesto = (miEmpleado.getPuesto() != null) ? miEmpleado.getPuesto().getDescpuesto() : "No asignado";
                txtArea = (miEmpleado.getPuesto() != null && miEmpleado.getPuesto().getRhudepto() != null)
                        ? miEmpleado.getPuesto().getRhudepto().getDescdepto() : "No asignado";
                try {
                    Rhuempleado jefe = busqRhu.buscarJefaturaInmediata(miCodemp);
                    txtNombreJefe = (jefe != null) ? jefe.getPersona().getNomcom() : "No asignado";
                } catch (Exception e) {
                    txtNombreJefe = "No asignado";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos del usuario dueño del PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PAO - Gestión y CRUD">
    /**
     * VALIDA SI EL PAO ACTUAL ESTÁ BLOQUEADO POR ACTA DE APROBACIÓN.
     *
     * @return true si tiene acta asignada, false en caso contrario.
     */
    private boolean esPaoBloqueado() {
        if (this.paoActual != null && this.paoActual.getIdacta() != null) {
            return true;
        }
        return false;
    }

    /**
     * CARGA LOS PAO DEL USUARIO .
     */
    public void cargarPaosDelUsuario() {
        listaPaos.clear();
        try {
            if (miCodemp != null) {
                List<Plnpao> res = busqPep.buscarPaosPorCoordinador(miCodemp);
                if (res != null) {
                    listaPaos.addAll(res);
                    getListaResumenIniciativas(); //preuba 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos del PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * BUSCA PAO APLICANDO FILTROS (AÑO, PEP, NOMBRE).
     */
    
    private void limpiarMensajesJSF() {
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator<FacesMessage> it = context.getMessages();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }
    
    public void buscarPaos() {
        
        limpiarMensajesJSF();
        listaPaos.clear();
        try {
            if (miCodemp != null) {
                List<Plnpao> res = busqPep.buscarPaosPorFiltros(
                        miCodemp,
                        filtroAnioPao,
                        filtroIdPepPao);

                if (res != null) {
                    listaPaos.addAll(res);
                }
                if (listaPaos==null || listaPaos.isEmpty()) {
                    showMsg("No se encontraron PAOS con los filtros aplicados.", ValidaDatos.INFO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al buscar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * LIMPIA LOS FILTROS DE BÚSQUEDA DE PAO.
     */
    public void limpiarBusquedaPao() {
        limpiarMensajesJSF();
        this.filtroAnioPao = null;
        this.filtroIdPepPao = null;
        buscarPaos();
    }

    /**
     * INICIALIZA UN NUEVO PAO.
     */
    public void nuevoPao() {
        cargarPeps();

        Calendar cal = Calendar.getInstance();
        int anioActual = cal.get(Calendar.YEAR);

        paoActual = new Plnpao();
        paoActual.setAnio(anioActual);
        paoActual.setNombrepao("");

        if (miEmpleado != null) {
            paoActual.setIdcoordinador(miEmpleado);
        }

        nuevoPao = true;
        smPao.clear();

        limpiarSeleccionJerarquia();
    }

    /**
     * EVENTO DE SELECCIÓN DE PAO DESDE LA TABLA. Carga el PAO seleccionado y
     * sus datos relacionados.
     */
    public void seleccionarPaoDeTabla(SelectEvent e) {
        paoActual = (Plnpao) e.getObject();
        nuevoPao = false;

        if (paoActual.getIdpep() != null) {
            this.selIdPepCabecera = paoActual.getIdpep().getIdpep();
            this.selIdPep = this.selIdPepCabecera;
            cambioPep(null);
        } else {
            this.selIdPepCabecera = null;
            this.selIdPep = null;
            limpiarDesdePerspectiva();
        }

        cargarAccionesParaEvaluar();
        setCambiarTagPanel(1);
        getListaResumenIniciativas();
    }

    /**
     * EVENTO DE CAMBIO DE PEP EN LA CABECERA DEL PAO.
     */
    public void cambioPepCabecera(AjaxBehaviorEvent event) {
        cargarPeps();

        if (selIdPepCabecera != null) {
            this.selIdPep = this.selIdPepCabecera;
            cambioPep(null);
        } else {
            limpiarDesdePerspectiva();
        }
    }

    /**
     * GUARDA O ACTUALIZA UN PAO. Realiza validaciones y detecta cambio de PEP.
     */
    /**
     * Contexto: Capa de Control (Managed Bean) GUARDA O ACTUALIZA UN PAO.
     * Realiza validaciones y detecta cambio de PEP.
     */
    public void guardarPao() {
        List<String> lstMensaje = new ArrayList<String>();
        int anioSistema = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        cargarPeps();

        if (paoActual.getIdpao() != null && esPaoBloqueado()) {
            lstMensaje.add("El PAO tiene un Acta de aprobación asignada. No se puede modificar.");
        }

        if (selIdPepCabecera == null || (selIdPepCabecera instanceof BigInteger && ((BigInteger) selIdPepCabecera).compareTo(BigInteger.ZERO) <= 0)) {
            lstMensaje.add("Debe seleccionar el Plan Estratégico (PEP) asociado.");
        }

        if (paoActual.getNombrepao() == null || paoActual.getNombrepao().trim().isEmpty()) {
            lstMensaje.add("El nombre del Plan Operativo es obligatorio.");
        }

        if (paoActual.getAnio() == null) {
            lstMensaje.add("Ingrese el año para su PAO.");
        } else if (paoActual.getAnio() < anioSistema) {
            lstMensaje.add("El año del PAO no puede ser menor al año actual (" + anioSistema + ").");
        }

        // 1. RECUPERAR EL PEP COMPLETO PARA VALIDAR
        Plnpep pepCompleto = null;
        if (selIdPepCabecera != null && selIdPepCabecera.compareTo(BigInteger.ZERO) > 0) {
            try {
                List<Plnpep> pepsDisponibles = busqPep.buscarPeps(new HashMap());
                if (pepsDisponibles != null) {
                    for (Plnpep p : pepsDisponibles) {
                        if (p.getIdpep().equals(selIdPepCabecera)) {
                            pepCompleto = p;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. VALIDAR RANGO DE AÑOS CONTRA EL PEP REAL EN EL BEAN
// 2. VALIDAR RANGO DE AÑOS CONTRA EL PEP REAL EN EL BEAN
        if (paoActual.getAnio() != null && pepCompleto != null) {

            // Eliminamos la validación de null porque anioini y aniofin son primitivos (short)
            if (paoActual.getAnio() < pepCompleto.getAnioini() || paoActual.getAnio() > pepCompleto.getAniofin()) {

                lstMensaje.add("El año del PAO debe estar dentro de la vigencia del PEP seleccionado ("
                        + pepCompleto.getAnioini() + " - " + pepCompleto.getAniofin() + ").");
            }
        }

        // 3. MOSTRAR TODAS LAS ADVERTENCIAS RECOPILADAS EN LA VISTA
        if (!lstMensaje.isEmpty()) {
            for (String msg : lstMensaje) {
                this.showMsg(msg, ValidaDatos.WARNING);
            }
            return;
        }

        // 4. PREPARAR EL PAO CON EL PEP COMPLETO PARA QUE EL EJB NO FALLE
        if (pepCompleto != null) {
            paoActual.setIdpep(pepCompleto);
        }

        // 5. VALIDAR SI EL USUARIO CAMBIÓ EL PEP DE UN PAO EXISTENTE (POPUP)
        if (paoActual.getIdpao() != null) {
            BigInteger idPepAnterior = (paoActual.getIdpep() != null && paoActual.getIdpep().getIdpep() != null)
                    ? paoActual.getIdpep().getIdpep() : null;

            if (idPepAnterior != null && !idPepAnterior.equals(selIdPepCabecera)) {
                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmarCambioPep.show();");
                return;
            }
        }

        // 6. PROCEDER AL GUARDADO REAL
        try {
            ejecutarGuardadoReal(false);

        } catch (Exception ex) {
            Throwable causa = ex;
            while (causa != null) {
                if (causa instanceof ValidacionExcepcion) {
                    procesarValidacionExcepcion((ValidacionExcepcion) causa);
                    return;
                }
                causa = causa.getCause();
            }
            ex.printStackTrace();
            this.showMsg("Error técnico: " + ex.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * Contexto: Capa de Control (Managed Bean) EJECUTA EL GUARDADO REAL DEL
     * PAO.
     */
    private void ejecutarGuardadoReal(boolean venimosDeBorradoMasivo) {
        try {
            if (paoActual.getNombrepao() != null) {
                paoActual.setNombrepao(paoActual.getNombrepao().trim());
            }
            if (paoActual.getIdacta() != null) {
                paoActual.setIdacta(paoActual.getIdacta());
            }

            String user = (sesion != null && sesion.getSegusuario() != null) ? sesion.getSegusuario().getCodusr() : "SISTEMA";

            // Ya no construimos un PEP vacío aquí porque guardarPao() ya le asignó el PEP completo real
            paoActual.setIdcoordinador(miEmpleado);

            // El EJB recibe el objeto íntegro y su validación (línea 895) pasará sin lanzar excepciones
            adminPep.guardarPao(paoActual, user);

            if (venimosDeBorradoMasivo) {
                this.showMsg("PAO actualizado. Se eliminaron las acciones anteriores debido al cambio de PEP.", ValidaDatos.INFO);
            } else {
                this.showMsg("Plan Operativo Guardado Exitosamente.", ValidaDatos.INFO);
            }

            this.selIdPep = this.selIdPepCabecera;
            cambioPep(null);
            cargarPaosDelUsuario();

            listaAcciones.clear();
            listaAccionesParaEvaluar.clear();

        } catch (Exception ex) {
            ex.printStackTrace();
            this.showMsgLog(ex, ValidaDatos.ERROR);
            showMsg("Error al procesar el PAO: " + ex.getMessage(), ValidaDatos.ERROR);
        }
    }

//    public void guardarPao() {
//        List<String> lstMensaje = new ArrayList<>();
//        int anioSistema = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
//
//        cargarPeps();
//
//        if (paoActual.getIdpao() != null && esPaoBloqueado()) {
//            lstMensaje.add("El PAO tiene un Acta de aprobación asignada. No se puede modificar.");
//        }
//
//        if (selIdPepCabecera == null || (selIdPepCabecera instanceof BigInteger && ((BigInteger) selIdPepCabecera).compareTo(BigInteger.ZERO) <= 0)) {
//            lstMensaje.add("Debe seleccionar el Plan Estratégico (PEP) asociado.");
//        }
//
//        if (paoActual.getNombrepao() == null || paoActual.getNombrepao().trim().isEmpty()) {
//            lstMensaje.add("El nombre del Plan Operativo es obligatorio.");
//        }
//        if (paoActual.getAnio() == null) {
//            lstMensaje.add("Ingrese el año para su PAO");
//        }
//        if (paoActual.getAnio() != null && paoActual.getAnio() < anioSistema) {
//            lstMensaje.add("El año del PAO no puede ser menor al año actual (" + anioSistema + ").");
//        }
//        if (!lstMensaje.isEmpty()) {
//            for (String msg : lstMensaje) {
//                this.showMsg(msg, ValidaDatos.WARNING);
//            }
//            return;
//        }
//
//        if (paoActual.getIdpao() != null) {
//            BigInteger idPepAnterior = (paoActual.getIdpep() != null) ? paoActual.getIdpep().getIdpep() : null;
//
//            if (idPepAnterior != null && !idPepAnterior.equals(selIdPepCabecera)) {
//                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmarCambioPep.show();");
//                return;
//            }
//        }
//
//        try {
//            ejecutarGuardadoReal(false);
//
//        } catch (Exception ex) {
//            Throwable causa = ex;
//            while (causa != null) {
//                if (causa instanceof ValidacionExcepcion) {
//                    procesarValidacionExcepcion((ValidacionExcepcion) causa);
//                    return;
//                }
//                causa = causa.getCause();
//            }
//
//            ex.printStackTrace();
//            this.showMsg("Error técnico: " + ex.getMessage(), ValidaDatos.ERROR);
//        }
//    }
    /**
     * CONFIRMA EL CAMBIO DE PEP DEL PAO. Limpia las acciones anteriores y
     * ejecuta el guardado.
     */
    public void confirmarCambioPep() {
        try {
            if (paoActual.getIdpao() != null) {
                adminPep.limpiarPaoPorCambioPep(paoActual.getIdpao());
            }
            ejecutarGuardadoReal(true);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmarCambioPep.hide();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cambiar el PEP: " + e.getMessage(), ValidaDatos.ERROR);

        }
    }

    /**
     * EJECUTA EL GUARDADO REAL DEL PAO.
     *
     * @param venimosDeBorradoMasivo indica si se limpiaron acciones por cambio
     * de PEP.
     */
    /**
     * EJECUTA EL GUARDADO REAL DEL PAO.
     *
     * @param venimosDeBorradoMasivo indica si se limpiaron acciones por cambio
     * de PEP.
     */
//    private void ejecutarGuardadoReal(boolean venimosDeBorradoMasivo) {
//        try {
//            if (paoActual.getNombrepao() != null) {
//                paoActual.setNombrepao(paoActual.getNombrepao().trim());
//            }
//            if (paoActual.getIdacta() != null) {
//                paoActual.setIdacta(paoActual.getIdacta());
//            }
//
//            String user = (sesion != null && sesion.getSegusuario() != null) ? sesion.getSegusuario().getCodusr() : "SISTEMA";
//
//            // CORRECCIÓN: Recuperar el objeto PEP COMPLETO de la BD o lista en lugar de crear uno vacío.
//            // Esto asegura que el EJB AdministracionPep pueda leer anioini y aniofin correctamente.
//            Plnpep pepCompleto = null;
//            List<Plnpep> pepsDisponibles = busqPep.buscarPeps(new HashMap());
//            if (pepsDisponibles != null) {
//                for (Plnpep p : pepsDisponibles) {
//                    if (p.getIdpep().equals(selIdPepCabecera)) {
//                        pepCompleto = p;
//                        break;
//                    }
//                }
//            }
//
//            // Fallback de seguridad por si no lo encuentra
//            if (pepCompleto == null) {
//                pepCompleto = new Plnpep();
//                pepCompleto.setIdpep(selIdPepCabecera);
//            }
//
//            paoActual.setIdpep(pepCompleto);
//            paoActual.setIdcoordinador(miEmpleado);
//
//            adminPep.guardarPao(paoActual, user);
//
//            if (venimosDeBorradoMasivo) {
//                this.showMsg("PAO actualizado. Se eliminaron las acciones anteriores debido al cambio de PEP.", ValidaDatos.INFO);
//            } else {
//                this.showMsg("Plan Operativo Guardado Exitosamente.", ValidaDatos.INFO);
//            }
//
//            this.selIdPep = this.selIdPepCabecera;
//            cambioPep(null);
//            cargarPaosDelUsuario();
//
//            listaAcciones.clear();
//            listaAccionesParaEvaluar.clear();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            this.showMsgLog(ex, ValidaDatos.ERROR);
//            showMsg("Error al guardar el PAO: " + ex.getMessage(), ValidaDatos.ERROR);
//        }
//    }
    /**
     * ELIMINA UN PAO.
     */
    public void eliminarPao(Plnpao p) {
        try {
            if (p.getIdacta() != null) {
                this.showMsg("No se puede eliminar un PAO aprobado (tiene No. Acta).", ValidaDatos.WARNING);
                return;
            }
            adminPep.eliminarPao(p);

            this.showMsg("El Plan Operativo ha sido eliminado correctamente.", ValidaDatos.INFO);

            cargarPaosDelUsuario();

            if (paoActual != null && paoActual.getIdpao() != null && paoActual.getIdpao().equals(p.getIdpao())) {
                cancelarPao();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al eliminar PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
        prepararEliminarGenerico(p, DEL_PAO);
    }

    /**
     * CANCELA LA EDICIÓN/CREACIÓN DEL PAO.
     */
    public void cancelarPao() {
        paoActual = new Plnpao();
        smPao.clear();
        nuevoPao = false;
        setCambiarTagPanel(0);
        limpiarSeleccionJerarquia();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO JERARQUÍA PEP - Combos en Cascada">
    /**
     * CARGA LA LISTA DE PEPs PARA EL COMBO. Filtra solo PEPs activos, con acta
     * y vigentes.
     */
    public void cargarPeps() {
        this.listaPeps.clear();
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int anioActual = cal.get(java.util.Calendar.YEAR);

            List<Plnpep> peps = busqPep.buscarPeps(new HashMap());

            if (peps != null) {
                for (Plnpep p : peps) {
                    boolean esActivo = (p.getIdpep() != null && p.getEstado() == 1);
                    boolean tieneActa = (p.getNumacta() != null);
                    boolean esVigente = false;
                    int anioFinPep = p.getAniofin();
                    if (anioFinPep >= anioActual) {
                        esVigente = true;
                    }

                    if (esActivo && tieneActa && esVigente) {
                        listaPeps.add(new SelectItem(p.getIdpep(), p.getDescrip()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los PEPs: " + e.getMessage(), ValidaDatos.ERROR);

        }
    }

    /**
     * EVENTO DE CAMBIO DE PEP. Carga las Perspectivas asociadas y limpia
     * selecciones inferiores.
     */
    public void cambioPep(AjaxBehaviorEvent e) {
        if (e != null) {
            this.selIdPep = (BigInteger) ((javax.faces.component.UIOutput) e.getSource()).getValue();
        }

        limpiarDesdePerspectiva();

        if (selIdPep != null) {
            try {
                List<Plnperspectivadeta> l = busqPep.buscarPerspectivasPorPep(selIdPep);
                if (l != null) {
                    for (Plnperspectivadeta p : l) {
                        listaPerspectivas.add(new SelectItem(p.getIdperspectiva(), p.getPerspectivaMaestra().getNombre()));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showMsg("Error al cambiar PEP: ", ValidaDatos.ERROR);

            }
        }
    }

    /**
     * EVENTO DE CAMBIO DE PERSPECTIVA. Carga los Objetivos Estratégicos
     * asociados.
     */
    public void cambioPerspectiva(AjaxBehaviorEvent e) {
        if (e != null) {
            this.selIdPerspectiva = (BigInteger) ((javax.faces.component.UIOutput) e.getSource()).getValue();
        }

        limpiarDesdeObjetivo();
        this.listaObjetivos.clear();

        if (this.selIdPerspectiva != null) {
            try {
                List<Plnpepobjetivo> list = busqPep.buscarObjetivosPorPerspectiva(selIdPerspectiva);
                if (list != null) {
                    for (Plnpepobjetivo o : list) {
                        if (o.getEstado() == 1) {
                            String codigo = (o.getCodigooe() != null) ? o.getCodigooe() : "S/C";
                            String desc = (o.getDescrip() != null) ? o.getDescrip() : "";
                            String label = codigo + " : " + desc;

                            if (label.length() > 80) {
                                label = label.substring(0, 77) + "...";
                            }
                            this.listaObjetivos.add(new SelectItem(o.getIdobjetivo(), label));
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showMsg("Error al cambiar la Perspectiva: ", ValidaDatos.ERROR);

            }
        }
    }

    /**
     * EVENTO DE CAMBIO DE OBJETIVO. Carga los Indicadores de Cumplimiento
     * asociados.
     */
    public void cambioObjetivo(AjaxBehaviorEvent e) {
        if (e != null) {
            this.selIdObjetivo = (BigInteger) ((javax.faces.component.UIOutput) e.getSource()).getValue();
        }

        limpiarDesdeIndicador();

        if (this.selIdObjetivo != null) {
            try {
                List<Plnpepindicumpl> list = busqPep.buscarIndicadoresPorObjetivo(selIdObjetivo);
                if (list != null) {
                    for (Plnpepindicumpl i : list) {
                        String codigo = (i.getCodigoindicador() != null) ? i.getCodigoindicador() : "S/C";
                        String desc = (i.getDescrip() != null) ? i.getDescrip() : "";
                        String label = codigo + " : " + desc;
                        if (label.length() > 80) {
                            label = label.substring(0, 77) + "...";
                        }
                        this.listaIndicadores.add(new SelectItem(i.getIdindicadorcump(), label));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showMsg("Error al cambiar el Objetivo Estratégico: ", ValidaDatos.ERROR);

            }
        }
    }

    /**
     * EVENTO DE CAMBIO DE INDICADOR. Carga las Líneas Estratégicas asociadas.
     */
    /**
     * EVENTO DE CAMBIO DE INDICADOR. Carga las Líneas Estratégicas asociadas
     * filtrando por el año del PAO actual.
     *
     * Regla: - Si PAO.anio == 2027 => solo estrategias con l.anio == 2027
     */
    public void cambioIndicador(AjaxBehaviorEvent e) {
        if (e != null) {
            Object val = ((javax.faces.component.UIOutput) e.getSource()).getValue();
            if (val == null || val.toString().trim().isEmpty()) {
                this.selIdIndicador = null;
            } else {
                BigInteger tmp = new BigInteger(val.toString().trim());
                this.selIdIndicador = tmp.compareTo(BigInteger.ZERO) <= 0 ? null : tmp;
            }
        }

        limpiarDesdeEstrategia();

        if (this.selIdIndicador == null) {
            return;
        }

        if (this.paoActual == null || this.paoActual.getAnio() == null) {
            showMsg("Debe seleccionar/guardar el PAO y su año antes de cargar las Líneas Estratégicas.",
                    ValidaDatos.WARNING);
            return;
        }

        final int anioPao = this.paoActual.getAnio(); // ← int primitivo

        try {
            List<Plnpeplinestr> estrategias =
                    busqPep.buscarEstrategiasPorIndicador(this.selIdIndicador);

            if (estrategias == null || estrategias.isEmpty()) {
                showMsg("El indicador no tiene Líneas Estratégicas asociadas.",
                        ValidaDatos.INFO);
                return;
            }

            for (Plnpeplinestr est : estrategias) {
                if (est == null || est.getAnio() == null) {
                    continue;
                }

                if (est.getAnio().intValue() != anioPao) {
                    continue;
                }

                String label = (est.getDescrip() != null) ? est.getDescrip().trim() : "";
                if (label.length() > 80) {
                    label = label.substring(0, 77) + "...";
                }

                this.listaEstrategias.add(
                        new SelectItem(est.getIdestrategia(), label));
            }

            if (this.listaEstrategias.isEmpty()) {
                showMsg("No existen Líneas Estratégicas para el año " + anioPao
                        + " en el indicador seleccionado.", ValidaDatos.INFO);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Error cargando Líneas Estratégicas: " + ex.getMessage(),
                    ValidaDatos.ERROR);
        }
    }

    /**
     * EVENTO DE CAMBIO DE ESTRATEGIA. Muestra el panel de acciones y carga las
     * acciones asociadas.
     */
    public void cambioEstrategia(AjaxBehaviorEvent e) {
        BigInteger id = null;
        if (e != null) {
            Object val = ((javax.faces.component.UIOutput) e.getSource()).getValue();
            if (val != null && !val.toString().trim().isEmpty()) {
                BigInteger tmp = new BigInteger(val.toString().trim());
                if (tmp.compareTo(BigInteger.ZERO) > 0) {
                    id = tmp;
                }
            }
        }
        this.selIdEstrategia = id;
        panelAccionesVisible = (this.selIdEstrategia != null);
        if (panelAccionesVisible && paoActual != null && paoActual.getIdpao() != null) {
            cargarAcciones();
        } else {
            listaAcciones.clear();
            listaNoPlanificadas.clear();
        }
    }

    /**
     * LIMPIA TODA LA SELECCIÓN JERÁRQUICA.
     */
    private void limpiarSeleccionJerarquia() {
        this.selIdPep = null;
        limpiarDesdePerspectiva();
    }

    /**
     * LIMPIA DESDE PERSPECTIVA HACIA ABAJO.
     */
    private void limpiarDesdePerspectiva() {
        listaPerspectivas.clear();
        selIdPerspectiva = null;
        limpiarDesdeObjetivo();
    }

    /**
     * LIMPIA DESDE OBJETIVO HACIA ABAJO.
     */
    private void limpiarDesdeObjetivo() {
        listaObjetivos.clear();
        selIdObjetivo = null;
        limpiarDesdeIndicador();
    }

    /**
     * LIMPIA DESDE INDICADOR HACIA ABAJO.
     */
    private void limpiarDesdeIndicador() {
        listaIndicadores.clear();
        selIdIndicador = null;
        limpiarDesdeEstrategia();
    }

    /**
     * LIMPIA DESDE ESTRATEGIA HACIA ABAJO.
     */
    private void limpiarDesdeEstrategia() {
        listaEstrategias.clear();
        selIdEstrategia = null;
        panelAccionesVisible = false;
        listaAcciones.clear();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ELIMINACIÓN GENÉRICA">
    /**
     * PREPARA LA ELIMINACIÓN DE CUALQUIER ELEMENTO. Configura el mensaje de
     * confirmación según el tipo de elemento.
     */
    public void prepararEliminarGenerico(Object objetoABorrar, int tipo) {
        if (objetoABorrar == null) {
            this.showMsg("No se ha seleccionado ningún registro para eliminar.", ValidaDatos.WARNING);
            return;
        }

        this.tipoEliminacion = tipo;

        switch (tipo) {
            case DEL_PAO:
                this.paoAEliminar = (Plnpao) objetoABorrar;
                this.mensajeConfirmacion = "ACCIÓN IRREVERSIBLE: Se borrarán todas las Actividades, Acciones, Evaluaciones y Seguimientos. Esta Acción es irreversible. ¿Desea continuar con la eliminación?";
                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.show();");
                break;

            case DEL_ACCION:
                Plnacciondeta accionAEliminar = (Plnacciondeta) objetoABorrar;
                this.accionPadreSeleccionada = accionAEliminar;

                try {
                    if (esPaoBloqueado()) {
                        this.showMsg("El PAO está aprobado. No se pueden eliminar acciones.", ValidaDatos.WARNING);
                        FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("msgForm");
                        return;
                    }

//                    // 2. Verificar si tiene evaluaciones
//                    boolean tieneEvaluaciones = busqPep.accionTieneEvaluaciones(accionAEliminar.getIdaccionpao());
//                    if (tieneEvaluaciones) {
//                        this.showMsg("No se puede eliminar: La acción tiene evaluaciones registradas.", ValidaDatos.WARNING);
//                        FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("msgForm");
//                        return;
//                    }
//
//                    // 3. Verificar si tiene seguimientos
//                    boolean tieneSeguimientos = busqPep.accionTieneSeguimientos(accionAEliminar.getIdaccionpao());
//                    if (tieneSeguimientos) {
//                        this.showMsg("No se puede eliminar: La acción tiene seguimientos registrados.", ValidaDatos.WARNING);
//                        FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("msgForm");
//                        return;
//                    }
//
//                    // 4. Verificar si está vinculada a iniciativa
//                    boolean estaVinculada = busqPep.accionEstaVinculadaAIniciativa(accionAEliminar.getIdaccionpao());
//                    if (estaVinculada) {
//                        this.showMsg("No se puede eliminar: La acción está asociada a una iniciativa. Debe desvincularla primero desde el módulo de Gestión Iniciativas.", ValidaDatos.WARNING);
//                        FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("msgForm");
//                        return;
//                    }

                    // ✅ Si pasó todas las validaciones, mostrar confirmación
                    this.mensajeConfirmacion = "¿Está seguro que desea eliminar esta acción?";
                    JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.show();");

                } catch (Exception e) {
                    e.printStackTrace();
                    this.showMsg("Error al validar la acción: " + e.getMessage(), ValidaDatos.ERROR);
                    FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("msgForm");
                }
                break;

            case DEL_TAREA:
                this.tareaAEliminar = (Plnaccidetplantrim) objetoABorrar;
                this.mensajeConfirmacion = "¿Seguro que desea eliminar esta Tarea/Programación Trimestral?";
                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.show();");
                break;

            case DEL_INICIATIVA:
                this.iniciativaAEliminar = (Plnpaoinic) objetoABorrar;
                this.mensajeConfirmacion = "¿Seguro que desea eliminar esta Iniciativa?";
                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.show();");
                break;
        }
    }

    /**
     * CONFIRMA Y EJECUTA LA ELIMINACIÓN DEL ELEMENTO PREPARADO. Ejecuta el
     * borrado según el tipo seleccionado.
     */
    public void confirmarEliminacion() {
        try {
            switch (tipoEliminacion) {
                case DEL_PAO:
                    adminPep.eliminarPao(this.paoAEliminar);
                    this.showMsg("PAO eliminado correctamente.", ValidaDatos.INFO);

                    buscarPaos();
                    cancelarPao();
                    break;

                case DEL_ACCION:
                    // La acción ya fue validada en prepararEliminarGenerico
                    adminPep.eliminarAccionPao(this.accionPadreSeleccionada);
                    this.showMsg("Acción eliminada correctamente.", ValidaDatos.INFO);
                    cargarAcciones();
                    this.accionPadreSeleccionada = null;
                    break;

                case DEL_TAREA:
                    adminPep.eliminarDetalleTrim(this.tareaAEliminar);

                    this.showMsg("Tarea trimestral eliminada.", ValidaDatos.INFO);

                    cargarDetallesTrim();

                    this.tareaAEliminar = null;
                    break;

                case DEL_INICIATIVA:
                    adminPep.eliminarIniciativa(this.iniciativaAEliminar);
                    this.showMsg("Iniciativa eliminada.", ValidaDatos.INFO);
                    break;
            }

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.hide();");

        } catch (Exception e) {
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.hide();");
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ACCIONES DEL PAO">
    /**
     * INICIALIZA UNA NUEVA ACCIÓN.
     */
    public void nuevaAccion() {
        accion = new Plnacciondeta();
        accion.setPresupropues(BigDecimal.ZERO);
    }

    /**
     * PREPARA EL POPUP PARA CREAR UNA NUEVA ACCIÓN.
     */
    public void prepararNuevaAccion() {
        this.accion = new Plnacciondeta();
        this.accion.setPresupropues(BigDecimal.ZERO);
        this.accion.setIdpao(paoActual);
        this.editandoAccion = false;

        if (this.selIdEstrategia != null) {
            try {
                this.resumenJerarquia = busqPep.obtenerResumenPorEstrategia(this.selIdEstrategia);
                this.resumenJerarquia.setAccion("Nueva Actividad (Por Registrar)");
            } catch (Exception e) {
                this.resumenJerarquia = new ResumenJerarquiaDTO();
            }
        } else {
            this.resumenJerarquia = new ResumenJerarquiaDTO();
            this.resumenJerarquia.setAccion("Nueva Actividad");
        }
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupAccion.show();");
    }

    /**
     * PREPARA UNA ACCIÓN PARA EDICIÓN.
     */
    public void prepararEditarAccion(Plnacciondeta a) {
        this.accion = a;
        this.editandoAccion = true;

        cargarResumenJerarquiaUnificado(a);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupAccion.show();");
    }

    /**
     * GUARDA O ACTUALIZA UNA ACCIÓN DEL PAO. Realiza validaciones de
     * descripción, presupuesto y medio de verificación.
     */
    public void guardarAccion() {
        try {
            if (esPaoBloqueado()) {
                this.showMsg("El PAO está aprobado. No se pueden modificar las acciones planificadas.", ValidaDatos.WARNING);
                return;
            }

            if (!validarLongitudCampo(accion.getDescrip(), "Descripción", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(accion.getMedioverifi(), "Medio de Verificación", MAX_LONGITUD_TEXTO)) {
                return;
            }

            if (accion.getDescrip() != null) {
                accion.setDescrip(accion.getDescrip().trim());
            }
            if (accion.getMedioverifi() != null) {
                accion.setMedioverifi(accion.getMedioverifi().trim());
            }
            if (selIdEstrategia == null) {
                this.showMsg("Seleccione la Línea Estratégica.", ValidaDatos.WARNING);
                return;
            }

            if (accion.getDescrip() == null || accion.getDescrip().isEmpty()) {
                this.showMsg("La Descripción de la actividad es obligatoria.", ValidaDatos.WARNING);
                return;
            }

            if (accion.getPresupropues() != null) {
                if (accion.getPresupropues().compareTo(BigDecimal.ZERO) < 0) {
                    this.showMsg("El presupuesto no puede ser negativo.", ValidaDatos.WARNING);
                    return;
                }
            } else {
                accion.setPresupropues(BigDecimal.ZERO);
            }

            if (accion.getMedioverifi() == null || accion.getMedioverifi().isEmpty()) {
                this.showMsg("El Medio de Verificación es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            Plnpeplinestr est = busqPep.buscarEstrategiaPorId(selIdEstrategia);

            if (est.getAnio() != null && paoActual.getAnio() != null) {
                if (est.getAnio().intValue() != paoActual.getAnio().intValue()) {
                    showMsg("No puede asociar acciones a líneas estratégicas de un año distinto al PAO ("
                            + paoActual.getAnio() + ").", ValidaDatos.WARNING);
                    return;
                }
            }

            accion.setIdestrategia(est);
            accion.setIdpao(paoActual);

            String user = (sesion != null && sesion.getSegusuario() != null) ? sesion.getSegusuario().getCodusr() : "SISTEMA";
            adminPep.guardarAccionPao(accion, user);

            cargarAcciones();
            //cargarAccionesParaEvaluar();

            this.showMsg(editandoAccion ? "Actividad actualizada." : "Actividad creada.", ValidaDatos.INFO);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupAccion.hide();");

        } catch (Exception e) {
            this.showMsg("Error: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * CARGA LAS ACCIONES DE LA ESTRATEGIA SELECCIONADA. Incluye acciones
     * planificadas y no planificadas.
     */
    private void cargarAcciones() {
        listaAcciones.clear();
        listaNoPlanificadas.clear();
        try {
            List<Plnacciondeta> res = busqPep.buscarAccionesPorPaoYEstrategia(paoActual.getIdpao(), selIdEstrategia);
            if (res != null) {
                listaAcciones.addAll(res);
            }

            List<Plnaccionsinplan> resNo = busqPep.buscarNoPlanificadasPorEstrategia(paoActual.getIdpao(), selIdEstrategia);
            if (resNo != null) {
                listaNoPlanificadas.addAll(resNo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar las acciones del PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * ELIMINA UNA ACCIÓN DEL PAO.
     */
    public void eliminarAccion(Plnacciondeta a) {
        prepararEliminarGenerico(a, DEL_ACCION);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO EVALUACIONES">
    /**
     * CARGA LOS COMBOS JERÁRQUICOS PARA FILTRAR EVALUACIONES.
     */
    public void cargarCombosEvaluacion() {
        listaPerspectivasEval.clear();
        listaObjetivosEval.clear();
        listaIndicadoresEval.clear();
        listaEstrategiasEval.clear();

        filtroIdPerspectiva = null;
        filtroIdObjetivo = null;
        filtroIdIndicador = null;
        filtroIdEstrategia = null;

        if (paoActual != null && paoActual.getIdpep() != null) {
            try {
                List<Plnperspectivadeta> list = busqPep.buscarPerspectivasPorPep(paoActual.getIdpep().getIdpep());
                if (list != null) {
                    for (Plnperspectivadeta p : list) {
                        listaPerspectivasEval.add(new SelectItem(p.getIdperspectiva(), p.getPerspectivaMaestra().getNombre()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar los datos de PEP en la evaluación: " + e.getMessage(), ValidaDatos.ERROR);

            }
        }
    }

    /**
     * EVENTO DE CAMBIO DE FILTRO PERSPECTIVA (EVALUACIÓN).
     */
    public void cambioFiltroPerspectiva(AjaxBehaviorEvent event) {
        Object val = ((javax.faces.component.UIOutput) event.getSource()).getValue();

        BigInteger idSeleccionado = null;
        if (val != null && !val.toString().isEmpty()) {
            idSeleccionado = new BigInteger(val.toString());
        }

        this.filtroIdObjetivo = null;
        this.filtroIdIndicador = null;
        this.filtroIdEstrategia = null;

        if (listaObjetivosEval != null) {
            listaObjetivosEval.clear();
        }
        if (listaIndicadoresEval != null) {
            listaIndicadoresEval.clear();
        }
        if (listaEstrategiasEval != null) {
            listaEstrategiasEval.clear();
        }

        if (idSeleccionado == null || idSeleccionado.intValue() == -1) {
            this.filtroIdPerspectiva = null;

        } else {
            this.filtroIdPerspectiva = idSeleccionado;

            try {
                List<Plnpepobjetivo> list = busqPep.buscarObjetivosPorPerspectiva(this.filtroIdPerspectiva);

                if (list != null) {
                    for (Plnpepobjetivo o : list) {
                        if (o.getEstado() != null && o.getEstado() == 1) {
                            String desc = (o.getDescrip() != null) ? o.getDescrip() : "";
                            if (desc.length() > 80) {
                                desc = desc.substring(0, 77) + "...";
                            }

                            String cod = (o.getCodigooe() != null) ? o.getCodigooe() : "S/C";

                            listaObjetivosEval.add(new SelectItem(o.getIdobjetivo(), cod + " - " + desc));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cambiar Perspectiva en los filtros: " + e.getMessage(), ValidaDatos.ERROR);
            }
        }

        aplicarFiltrosEvaluacion(null);
    }

    /**
     * EVENTO DE CAMBIO DE FILTRO OBJETIVO (EVALUACIÓN).
     */
    public void cambioFiltroObjetivo(AjaxBehaviorEvent event) {
        Object val = ((javax.faces.component.UIOutput) event.getSource()).getValue();
        BigInteger idSeleccionado = null;
        if (val != null && !val.toString().isEmpty()) {
            idSeleccionado = new BigInteger(val.toString());
        }

        this.filtroIdIndicador = null;
        this.filtroIdEstrategia = null;

        if (listaIndicadoresEval != null) {
            listaIndicadoresEval.clear();
        }
        if (listaEstrategiasEval != null) {
            listaEstrategiasEval.clear();
        }

        if (idSeleccionado == null || idSeleccionado.intValue() == -1) {
            this.filtroIdObjetivo = null;
        } else {
            this.filtroIdObjetivo = idSeleccionado;
            try {
                List<Plnpepindicumpl> list = busqPep.buscarIndicadoresPorObjetivo(this.filtroIdObjetivo);
                if (list != null) {
                    for (Plnpepindicumpl i : list) {
                        String desc = (i.getDescrip() != null) ? i.getDescrip() : "";
                        if (desc.length() > 80) {
                            desc = desc.substring(0, 77) + "...";
                        }

                        String cod = (i.getCodigoindicador() != null) ? i.getCodigoindicador() : "S/C";

                        listaIndicadoresEval.add(new SelectItem(i.getIdindicadorcump(), cod + " - " + desc));

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cambiar Objetivo Estratégico en los filtros: " + e.getMessage(), ValidaDatos.ERROR);

            }
        }

        aplicarFiltrosEvaluacion(null);
    }

    /**
     * EVENTO DE CAMBIO DE FILTRO INDICADOR (EVALUACIÓN).
     */
    public void cambioFiltroIndicador(AjaxBehaviorEvent event) {
        Object val = ((javax.faces.component.UIOutput) event.getSource()).getValue();
        BigInteger idSeleccionado = null;
        if (val != null && !val.toString().isEmpty()) {
            idSeleccionado = new BigInteger(val.toString());
        }

        this.filtroIdEstrategia = null;

        if (listaEstrategiasEval != null) {
            listaEstrategiasEval.clear();
        }

        if (idSeleccionado == null || idSeleccionado.intValue() == -1) {
            this.filtroIdIndicador = null;
        } else {
            this.filtroIdIndicador = idSeleccionado;
            try {
                List<Plnpeplinestr> list = busqPep.buscarEstrategiasPorIndicador(this.filtroIdIndicador);
                if (list != null) {
                    for (Plnpeplinestr l : list) {

                        String desc = (l.getDescrip() != null) ? l.getDescrip() : "";
                        if (desc.length() > 80) {
                            desc = desc.substring(0, 77) + "...";
                        }

                        listaEstrategiasEval.add(new SelectItem(l.getIdestrategia(), desc));

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cambiar Indicador de Cumplimiento en los filtros: " + e.getMessage(), ValidaDatos.ERROR);

            }
        }

        aplicarFiltrosEvaluacion(null);
    }

    /**
     * EVENTO DE CAMBIO DE FILTRO ESTRATEGIA (EVALUACIÓN).
     */
    public void cambioFiltroEstrategia(AjaxBehaviorEvent event) {
        Object val = ((javax.faces.component.UIOutput) event.getSource()).getValue();
        BigInteger idSeleccionado = null;
        if (val != null && !val.toString().isEmpty()) {
            idSeleccionado = new BigInteger(val.toString());
        }

        if (idSeleccionado == null || idSeleccionado.intValue() == -1) {
            this.filtroIdEstrategia = null;
        } else {
            this.filtroIdEstrategia = idSeleccionado;
        }

        aplicarFiltrosEvaluacion(null);
    }

    /**
     * FILTRA LAS ACCIONES PENDIENTES DE EVALUACIÓN PARA EL PERIODO
     * SELECCIONADO. Si no hay filtro, muestra todas las acciones sin
     * evaluación.
     */
    public void filtrarAccionesPorPeriodo(AjaxBehaviorEvent event) {
        try {
            // Sin filtro → mostrar todas las pendientes (comportamiento original)
            if (filtroPeriodoEvaluacion == null
                    || filtroPeriodoEvaluacion.trim().isEmpty()
                    || "0".equals(filtroPeriodoEvaluacion)) {
                aplicarFiltrosEvaluacion(null);
                return;
            }

            if (listaAccionesParaEvaluarMaster == null) {
                return;
            }

            List<Plnacciondeta> resultado = new ArrayList<>();

            for (Plnacciondeta accion : listaAccionesParaEvaluarMaster) {
                // Verificar si ya tiene evaluación para ESTE periodo específico
                try {
                    Plnaccioneval evalDelPeriodo = busqPep.buscarEvaluacionPorAccionYPeriodo(
                            accion.getIdaccionpao(),
                            filtroPeriodoEvaluacion.trim());

                    // Si NO tiene evaluación para el periodo → mostrar como pendiente
                    if (evalDelPeriodo == null) {
                        resultado.add(accion);
                    }
                } catch (Exception ex) {
                    // Si falla la consulta, incluir la acción por precaución
                    resultado.add(accion);
                }
            }

            this.listaAccionesParaEvaluar = resultado;

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al filtrar por periodo: " + e.getMessage(),
                    ValidaDatos.ERROR);
        }
    }

    /**
     * LIMPIA EL FILTRO DE PERIODO Y RESTAURA LA LISTA COMPLETA.
     */
    public void limpiarFiltroPeriodo() {
        this.filtroPeriodoEvaluacion = null;
        aplicarFiltrosEvaluacion(null);
    }

    /**
     * Listener del combo de periodo — recarga la tabla de pendientes según el
     * periodo seleccionado.
     */
    public void cambioFiltroPeriodo(AjaxBehaviorEvent event) {
        Object val = ((javax.faces.component.UIOutput) event.getSource()).getValue();
        this.filtroPerioEval = (val != null) ? val.toString() : null;
        cargarAccionesParaEvaluar();
    }

    /**
     * APLICA LOS FILTROS JERÁRQUICOS SOBRE LA LISTA DE ACCIONES A EVALUAR.
     */
    public void aplicarFiltrosEvaluacion(AjaxBehaviorEvent event) {
        if (listaAccionesParaEvaluarMaster == null) {
            return;
        }
        this.listaAccionesParaEvaluar = new ArrayList<Plnacciondeta>();
        for (Plnacciondeta accion : listaAccionesParaEvaluarMaster) {
            boolean pasaFiltro = true;
            Plnpeplinestr est = accion.getIdestrategia();

            if (est == null) {
                continue;
            }

            if (filtroIdPerspectiva != null) {
                try {
                    BigInteger idPerAccion = est.getIdindicadorcump().getIdobjetivo().getIdperspectiva().getIdperspectiva();
                    if (!idPerAccion.equals(filtroIdPerspectiva)) {
                        pasaFiltro = false;
                    }
                } catch (Exception e) {
                    pasaFiltro = false;
                }
            }

            if (pasaFiltro && filtroIdObjetivo != null) {
                try {
                    BigInteger idObjAccion = est.getIdindicadorcump().getIdobjetivo().getIdobjetivo();
                    if (!idObjAccion.equals(filtroIdObjetivo)) {
                        pasaFiltro = false;
                    }
                } catch (Exception e) {
                    pasaFiltro = false;
                }
            }

            if (pasaFiltro && filtroIdIndicador != null) {
                try {
                    BigInteger idIndAccion = est.getIdindicadorcump().getIdindicadorcump();
                    if (!idIndAccion.equals(filtroIdIndicador)) {
                        pasaFiltro = false;
                    }
                } catch (Exception e) {
                    pasaFiltro = false;
                }
            }

            if (pasaFiltro && filtroIdEstrategia != null) {
                if (!est.getIdestrategia().equals(filtroIdEstrategia)) {
                    pasaFiltro = false;
                }
            }

            if (pasaFiltro) {
                this.listaAccionesParaEvaluar.add(accion);
            }
        }
    }

    /**
     * CARGA LAS ACCIONES NO PLANIFICADAS SEPARADAS POR ESTADO. Divide entre las
     * que ya tienen cumplimiento registrado y las pendientes.
     *
     * Las acciones no planificadas NO tienen tabla de seguimiento.
     */
public void cargarAccionesParaEvaluar() {
    listaAccionesParaEvaluar = new ArrayList<Plnacciondeta>();
    listaEvaluacionesRealizadas = new ArrayList<Plnaccioneval>();
    //listaNoPlanificadas = new ArrayList<Plnaccionsinplan>();
    listaNoPlanificadasEvaluadas = new ArrayList<Plnaccionsinplan>();
    
    listaCriticasPendientes.clear();
    listaSeguimientosYaRealizados.clear();

    if (paoActual == null || paoActual.getIdpao() == null) {
        cargarCombosEvaluacion();
        aplicarFiltrosEvaluacion(null);
        return;
    }

    try {
        //Cargar Seguimientos que YA existen en la BD para este PAO
        this.listaSeguimientosYaRealizados = busqPep.buscarSeguimientosPorPao(paoActual.getIdpao());

        // Procesar Acciones Planificadas
        List<Plnacciondeta> todas = busqPep.buscarAccionesPorPao(paoActual.getIdpao());
        cargarPeriodosEvaluacionPermitidos();
        int totalPeriodosHabilitados = listaPeriodosEvaluacion.size();

        if (todas != null) {
            for (Plnacciondeta ax : todas) {
                List<Plnaccioneval> evalsDeAccion = busqPep.buscarEvaluacionesPorAccion(ax.getIdaccionpao());

                if (evalsDeAccion != null && !evalsDeAccion.isEmpty()) {
                    listaEvaluacionesRealizadas.addAll(evalsDeAccion);
                    
                    // --- LÓGICA DE SEGUIMIENTO ---
                    Plnaccioneval ultima = evalsDeAccion.get(evalsDeAccion.size() - 1);
                    if (ultima.getCumplipct() != null && ultima.getCumplipct().compareTo(this.umbralCumplimiento) < 0) {
                        
                        // Verificamos si esta acción ya tiene un seguimiento registrado
                        boolean tieneSeguimiento = false;
                        for(Plnaccionseguimiento s : listaSeguimientosYaRealizados) {
                            if(s.getIdaccionpao().getIdaccionpao().equals(ax.getIdaccionpao())) {
                                tieneSeguimiento = true;
                                break;
                            }
                        }
                        
                        // Si está reprobada y NO tiene seguimiento, va a la lista de "Por corregir"
                        if (!tieneSeguimiento) {
                            ax.setUltimaEvaluacion(ultima); 
                            listaCriticasPendientes.add(ax);
                        }
                    }
                }

                // Lógica de pendientes de evaluación (la original)
                int evalsRegistradas = (evalsDeAccion != null) ? evalsDeAccion.size() : 0;
                if (evalsRegistradas < totalPeriodosHabilitados) {
                    listaAccionesParaEvaluar.add(ax);
                }
            }
        }

        // Acciones no planificadas
        List<Plnaccionsinplan> todasNoPlan = busqPep.buscarAccionesNoPlanificadas(paoActual.getIdpao());
        if (todasNoPlan != null) {
            for (Plnaccionsinplan np : todasNoPlan) {
                if (np.getCumplipct() != null && np.getCumplipct().compareTo(BigDecimal.ZERO) > 0) {
                    listaNoPlanificadasEvaluadas.add(np);
                } 
            }
        }

        this.listaAccionesParaEvaluarMaster = new ArrayList<Plnacciondeta>(listaAccionesParaEvaluar);
    } catch (Exception e) {
        e.printStackTrace();
    }
    cargarCombosEvaluacion();
    aplicarFiltrosEvaluacion(null);
}

    /**
     * PREPARA EL POPUP PARA EVALUAR UNA ACCIÓN.
     */
    /**
     * GUARDA O ACTUALIZA UNA EVALUACIÓN. Realiza validaciones de fecha,
     * periodo, proyectado, realizado y porcentaje.
     */
    public void guardarEvaluacion() {
        List<String> lstMensaje = new ArrayList<>();

        try {

            if (!validarLongitudCampo(evaluacionActual.getProyec(), "Proyectado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getRealiz(), "Realizado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getDescriprealiz(), "Detalle de lo Realizado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getObserv(), "Observaciones", MAX_LONGITUD_TEXTO)) {
                return;
            }

            if (evaluacionActual.getPerieval() != null) {
                evaluacionActual.setPerieval(evaluacionActual.getPerieval().trim());
            }
            if (evaluacionActual.getProyec() != null) {
                evaluacionActual.setProyec(evaluacionActual.getProyec().trim());
            }
            if (evaluacionActual.getRealiz() != null) {
                evaluacionActual.setRealiz(evaluacionActual.getRealiz().trim());
            }
            if (evaluacionActual.getDescriprealiz() != null) {
                evaluacionActual.setDescriprealiz(evaluacionActual.getDescriprealiz().trim());
            }
            if (evaluacionActual.getObserv() != null) {
                evaluacionActual.setObserv(evaluacionActual.getObserv().trim());
            }

            if (evaluacionActual.getFchaeval() == null) {
                lstMensaje.add("La Fecha de Evaluación es obligatoria.");
            } else {
                Calendar calHoy = Calendar.getInstance();
                Calendar calEval = Calendar.getInstance();
                calEval.setTime(evaluacionActual.getFchaeval());

                if (evaluacionActual.getFchaeval().after(new java.util.Date())) {
                    lstMensaje.add("La fecha de evaluación no puede ser mayor a la fecha actual (futura).");
                }
                if (calEval.get(Calendar.YEAR) < calHoy.get(Calendar.YEAR)) {
                    lstMensaje.add("No puede registrar evaluaciones con fecha de años anteriores.");
                }
            }
            if (evaluacionActual.getPerieval() == null
                    || evaluacionActual.getPerieval().isEmpty()
                    || "0".equals(evaluacionActual.getPerieval())
                    || "-1".equals(evaluacionActual.getPerieval())) {
                lstMensaje.add("Debe seleccionar un Periodo de Evaluación.");
            }

            if (evaluacionActual.getProyec() == null || evaluacionActual.getProyec().isEmpty()) {
                lstMensaje.add("El campo Proyectado es obligatorio.");
            }
            if (evaluacionActual.getRealiz() == null || evaluacionActual.getRealiz().isEmpty()) {
                lstMensaje.add("El campo Realizado es obligatorio.");
            }
            if (evaluacionActual.getDescriprealiz() == null || evaluacionActual.getDescriprealiz().isEmpty()) {
                lstMensaje.add("El Detalle de lo Realizado es obligatorio.");
            }

            if (evaluacionActual.getCumplipct() == null) {
                lstMensaje.add("El % Cumplimiento es obligatorio.");
            } else {
                if (evaluacionActual.getCumplipct().compareTo(BigDecimal.ZERO) < 0
                        || evaluacionActual.getCumplipct().compareTo(new BigDecimal(100)) > 0) {
                    lstMensaje.add("El % Cumplimiento debe estar entre 0 y 100.");
                }
            }

            if (!lstMensaje.isEmpty()) {
                for (String msg : lstMensaje) {
                    this.showMsg(msg, ValidaDatos.WARNING);
                }
                return;
            }

            String usuarioActual = (sesion != null && sesion.getSegusuario() != null) ? sesion.getSegusuario().getCodusr() : "SISTEMA";

            if (evaluacionActual.getIdevaluacion() == null) {
                evaluacionActual.setFchacrea(new java.util.Date());
                evaluacionActual.setUsercrea(usuarioActual);
            } else {
                evaluacionActual.setFchamod(new java.util.Date());
                evaluacionActual.setUsermod(usuarioActual);
            }

            adminPep.guardarEvaluacion(evaluacionActual);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupEvaluacion.hide();");

            this.showMsg("Evaluación guardada exitosamente.", ValidaDatos.INFO);

            this.evaluacionActual = new Plnaccioneval();
            cargarAccionesParaEvaluar();

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al guardar una evaluación: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * CAMBIO DE ESTRATEGIA EN EVALUACIÓN.
     */
    public void cambioEstrategiaEvaluacion(AjaxBehaviorEvent e) {
        cambioEstrategia(e);
        cargarAccionesParaEvaluar();
        panelEvaluacionVisible = false;
    }

    /**
     * REDIRIGE A LA PESTAÑA DE EVALUACIÓN DESDE EL DETALLE DE UNA ACCIÓN.
     */
    public void irAEvaluarDesdeDetalle(Plnacciondeta accion) {
        try {
            cargarAccionesParaEvaluar();

            prepararEvaluacion(accion);

            cargarResumenJerarquiaUnificado(accion);

            //this.cambiarTagPanel = 2;

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al ir a la evaluación, busque la acción en el apartado de Evaluaciones: " + e.getMessage(), 3);
        }
    }
    //</editor-fold>

    /**
     * CALCULA EL CUMPLIMIENTO PONDERADO DE LA ACCIÓN PRINCIPAL.
     *
     * Fórmula: - Total meses de la acción = suma de (mesfin - mesini + 1) de
     * todos sus trimestrales - Peso de cada trimestral = sus meses / total
     * meses - Aporte de cada trimestral = cumplipct × peso - Solo se incluyen
     * trimestrales con cumplipct > 0 (ya evaluados) - Si ninguno está evaluado,
     * retorna 0
     */
    private BigDecimal calcularCumplimientoPonderado(List<Plnaccidetplantrim> trims) {
        if (trims == null || trims.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int totalMesesEvaluados = 0;
        for (Plnaccidetplantrim t : trims) {
            if (t.getCumplipct() != null
                    && t.getCumplipct().compareTo(BigDecimal.ZERO) > 0
                    && t.getMesini() > 0 && t.getMesfin() > 0) {
                totalMesesEvaluados += (t.getMesfin() - t.getMesini() + 1);
            }
        }

        if (totalMesesEvaluados == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPonderado = BigDecimal.ZERO;
        for (Plnaccidetplantrim t : trims) {
            if (t.getCumplipct() != null
                    && t.getCumplipct().compareTo(BigDecimal.ZERO) > 0
                    && t.getMesini() > 0 && t.getMesfin() > 0) {

                int mesesTrim = t.getMesfin() - t.getMesini() + 1;

                BigDecimal peso = new BigDecimal(mesesTrim)
                        .divide(new BigDecimal(totalMesesEvaluados), 10, RoundingMode.HALF_UP);

                BigDecimal aporte = t.getCumplipct().multiply(peso);

                totalPonderado = totalPonderado.add(aporte);
            }
        }

        return totalPonderado.setScale(2, RoundingMode.HALF_UP);
    }

    public void guardarCumplimientoTrimestral(Plnaccidetplantrim trimSeleccionado) {
        try {
            if (trimSeleccionado.getCumplipct() == null) {
                showMsg("El % de cumplimiento es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (trimSeleccionado.getCumplipct().compareTo(BigDecimal.ZERO) < 0
                    || trimSeleccionado.getCumplipct().compareTo(new BigDecimal(100)) > 0) {
                showMsg("El % debe estar entre 0 y 100.", ValidaDatos.WARNING);
                return;
            }

            adminPep.guardarCumplimientoTrimestral(trimSeleccionado,
                    sesion.getSegusuario().getCodusr());

            Plnacciondeta accionPrincipal = trimSeleccionado.getIdaccionpao();
            List<Plnaccidetplantrim> todosLosTrims =
                    busqPep.buscarDetallesPorAccion(accionPrincipal.getIdaccionpao());

            BigDecimal cumplimientoGeneral = calcularCumplimientoPonderado(todosLosTrims);

            Plnaccioneval eval = busqPep.buscarEvaluacionPorAccion(accionPrincipal.getIdaccionpao());

            if (eval == null) {
                eval = new Plnaccioneval();
                eval.setIdaccionpao(accionPrincipal);
                eval.setFchacrea(new java.util.Date());
                eval.setUsercrea(sesion.getSegusuario().getCodusr());
            } else {
                eval.setFchamod(new java.util.Date());
                eval.setUsermod(sesion.getSegusuario().getCodusr());
            }

            eval.setCumplipct(cumplimientoGeneral);
            adminPep.guardarEvaluacion(eval);

            showMsg("Cumplimiento guardado. Avance general: "
                    + cumplimientoGeneral + "%", ValidaDatos.INFO);

            cargarAccionesParaEvaluar();

            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(), "popupEvaluacion.hide();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ACCIONES NO PLANIFICADAS">
    /**
     * PREPARA EL POPUP PARA CREAR UNA NUEVA ACCIÓN NO PLANIFICADA.
     */
    public void prepararNuevaNoPlan() {
        this.accionNoPlanActual = new Plnaccionsinplan();
        this.accionNoPlanActual.setIdpao(paoActual);
        this.accionNoPlanActual.setFchacrea(new java.util.Date());

        if (this.selIdEstrategia != null) {
            try {
                this.resumenJerarquia = busqPep.obtenerResumenPorEstrategia(this.selIdEstrategia);
                this.resumenJerarquia.setAccion("Nueva Acción No Planificada");

            } catch (Exception e) {
                this.resumenJerarquia = new ResumenJerarquiaDTO();
            }
        } else {
            this.resumenJerarquia = new ResumenJerarquiaDTO();
            this.resumenJerarquia.setAccion("Nueva Acción No Planificada");
        }

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupNoPlan.show();");
    }

    /**
     * PREPARA UNA ACCIÓN NO PLANIFICADA PARA EDICIÓN.
     */
    public void prepararEditarNoPlan(Plnaccionsinplan item) {
        this.accionNoPlanActual = item;

        if (item.getIdestrategia() != null) {
            this.selIdEstrategia = item.getIdestrategia().getIdestrategia();
        } else {
            this.selIdEstrategia = null;
        }

        cargarResumenJerarquiaUnificado(item);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupNoPlan.show();");
    }

    /**
     * GUARDA O ACTUALIZA UNA ACCIÓN NO PLANIFICADA. Realiza validaciones de
     * descripción, periodo, proyectado, realizado y porcentaje.
     */
    public void guardarNoPlan() {

        try {
            if (accionNoPlanActual.getDescrip() != null) {
                accionNoPlanActual.setDescrip(accionNoPlanActual.getDescrip().trim());
            }
            if (accionNoPlanActual.getPerieval() != null) {
                accionNoPlanActual.setPerieval(accionNoPlanActual.getPerieval().trim());
            }
            
if (!validarLongitudCampo(accionNoPlanActual.getRealiz(), "Realizado", MAX_LONGITUD_TEXTO)) {
    return;
}
if (!validarLongitudCampo(accionNoPlanActual.getDescrip(), "Descripción", MAX_LONGITUD_TEXTO)) {
    return;
}
if (!validarLongitudCampo(accionNoPlanActual.getProyec(), "Proyectado", MAX_LONGITUD_TEXTO)) {
    return;
}
if (!validarLongitudCampo(accionNoPlanActual.getObserv(), "Observaciones", MAX_LONGITUD_TEXTO)) {
    return;
}
            
            if (selIdEstrategia == null && accionNoPlanActual.getIdestrategia() == null) {
                this.showMsg("Debe vincular a una Estrategia.", ValidaDatos.WARNING);
                return;
            }
            if (accionNoPlanActual.getDescrip() == null || accionNoPlanActual.getDescrip().isEmpty()) {
                this.showMsg("La Descripción es requerida.", ValidaDatos.WARNING);
                return;
            }

            if (accionNoPlanActual.getPerieval() == null || accionNoPlanActual.getPerieval().trim().isEmpty()) {
                this.showMsg("Seleccione el Periodo de Ejecución.", ValidaDatos.WARNING);
                return;
            }

            if (accionNoPlanActual.getProyec() == null || accionNoPlanActual.getProyec().isEmpty()) {
                this.showMsg("El campo Proyectado (Meta) es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (accionNoPlanActual.getRealiz() == null || accionNoPlanActual.getRealiz().isEmpty()) {
                this.showMsg("El campo Realizado es obligatorio.", ValidaDatos.WARNING);
                return;
            }

            if (accionNoPlanActual.getCumplipct() == null) {
                this.showMsg("El % de Cumplimiento es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (accionNoPlanActual.getCumplipct().compareTo(BigDecimal.ZERO) < 0
                    || accionNoPlanActual.getCumplipct().compareTo(new BigDecimal(100)) > 0) {
                this.showMsg("El % Cumplimiento debe ser un número entre 0 y 100.", ValidaDatos.WARNING);
                return;
            }

            if (selIdEstrategia != null) {
                Plnpeplinestr est = busqPep.buscarEstrategiaPorId(selIdEstrategia);
                accionNoPlanActual.setIdestrategia(est);
            }

            String user = sesion.getSegusuario().getCodusr();
            if (accionNoPlanActual.getIdaccinoplan() == null) {
                accionNoPlanActual.setUsercrea(user);
                accionNoPlanActual.setFchacrea(new java.util.Date());
            } else {
                accionNoPlanActual.setUsermod(user);
                accionNoPlanActual.setFchamod(new java.util.Date());
            }

            adminPep.guardarAccionNoPlanificada(accionNoPlanActual);

            this.showMsg("Acción No Planificada guardada.", ValidaDatos.INFO);

            cargarAcciones();
            //cargarAccionesParaEvaluar();

            //FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("frmAcciones:panelNoPlanificadas");
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("frmMttoPao:panelAcciones");



            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupNoPlan.hide();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar acción no Planificada " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * MUESTRA LOS DETALLES COMPLETOS DE UNA ACCIÓN NO PLANIFICADA. Incluye:
     * Descripción, Periodo, Proyectado, Realizado, % Cumplimiento y
     * Observaciones.
     */
    public void mostrarDetalleNoPlan(Plnaccionsinplan noPlan) {
        try {
            this.accionNoPlanActual = noPlan;

            // Cargar el resumen jerárquico para mostrar contexto
            cargarResumenJerarquiaUnificado(noPlan);

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                    "popupDetalleNoPlan.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar los datos de las acciones no Planificadas " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="MÓDULO SEGUIMIENTOS">
    /**
     * PREPARA EL POPUP PARA REGISTRAR UN SEGUIMIENTO. Carga el historial de
     * seguimientos de la acción.
     */
    public void prepararSeguimiento(Plnacciondeta accion) {
        try {
            this.accionPadreSeleccionada = accion;

            this.historialSeguimientos = busqPep.obtenerHistorialSeguimientos(accion.getIdaccionpao());

            if (this.historialSeguimientos == null) {
                this.historialSeguimientos = new ArrayList<Plnaccionseguimiento>();
            }

            this.seguimientoActual = new Plnaccionseguimiento();
            this.seguimientoActual.setIdaccionpao(accion);
            this.seguimientoActual.setEstado("EN PROCESO");
            this.seguimientoActual.setFchacrea(new java.util.Date());

            cargarResumenJerarquiaUnificado(accion);

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupSeguimiento.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsgLog(e, ValidaDatos.ERROR);
        }
    }

    /**
     * GUARDA UN NUEVO REGISTRO DE SEGUIMIENTO. Realiza validaciones de acción
     * requerida, compromiso y estado.
     */
    public void guardarSeguimiento() {
        try {
            if (seguimientoActual.getAccireque() == null || seguimientoActual.getAccireque().trim().isEmpty()) {
                showMsg("La 'Acción Requerida' es obligatoria.", ValidaDatos.WARNING);
                return;
            }
            if (seguimientoActual.getCompromiso() == null || seguimientoActual.getCompromiso().trim().isEmpty()) {
                showMsg("El 'Compromiso' es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (seguimientoActual.getEstado() == null || seguimientoActual.getEstado().trim().isEmpty()) {
                showMsg("El Estado es obligatorio.", ValidaDatos.WARNING);
                return;
            }

            String user = sesion.getSegusuario().getCodusr();

            seguimientoActual.setIdseguimiento(null);

            seguimientoActual.setUsercrea(user);
            seguimientoActual.setFchacrea(new java.util.Date());

            seguimientoActual.setUsermod(null);
            seguimientoActual.setFchamod(null);

            adminPep.guardarSeguimiento(seguimientoActual);

            showMsg("Seguimiento agregado al historial correctamente.", ValidaDatos.INFO);

            if (accionPadreSeleccionada != null) {
                this.historialSeguimientos = busqPep.obtenerHistorialSeguimientos(accionPadreSeleccionada.getIdaccionpao());

                this.seguimientoActual = new Plnaccionseguimiento();
                this.seguimientoActual.setIdaccionpao(accionPadreSeleccionada);
                this.seguimientoActual.setEstado("EN PROCESO");
            }

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupSeguimiento.hide();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar el seguimiento de una acción: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * VALIDA SI UNA ACCIÓN REQUIERE SEGUIMIENTO. Cumplimiento menor al umbral
     * definido (80%) es una constante (esta al inicio).
     *
     * @return true si requiere seguimiento, false en caso contrario.
     */
    public boolean isRequiereSeguimiento(Plnacciondeta accion) {
        if (accion == null) {
            return false;
        }

        for (Plnaccioneval eva : listaEvaluacionesRealizadas) {
            if (eva.getIdaccionpao().equals(accion)) {
                if (eva.getCumplipct() != null && eva.getCumplipct().compareTo(this.umbralCumplimiento) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void cargarSeguimientosCriticos() {
        listaAccionesBajoCumplimiento.clear();
        if (paoActual == null || paoActual.getIdpao() == null) {
            return;
        }

        try {
            // Obtenemos todas las acciones del PAO actual
            List<Plnacciondeta> todas = busqPep.buscarAccionesPorPao(paoActual.getIdpao());

            for (Plnacciondeta ax : todas) {
                // Buscamos la última evaluación registrada para esta acción
                Plnaccioneval eval = busqPep.buscarEvaluacionPorAccion(ax.getIdaccionpao());

                if (eval != null && eval.getCumplipct() != null) {
                    // Comprobamos si el cumplimiento es menor al 80%
                    if (eval.getCumplipct().compareTo(this.umbralCumplimiento) < 0) {

                        // IMPORTANTE: Debes tener el método setUltimaEvaluacion en Plnacciondeta
                        // como @Transient para que esto no falle.
                        ax.setUltimaEvaluacion(eval);

                        listaAccionesBajoCumplimiento.add(ax);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar seguimientos críticos: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    
    
public void cargarTabSeguimiento(javax.faces.event.ComponentSystemEvent event) {
    this.listaCriticasPendientes = new ArrayList<Plnacciondeta>();
    this.listaSeguimientosYaRealizados = new ArrayList<Plnaccionseguimiento>();

    if (paoActual == null || paoActual.getIdpao() == null) return;

    try {
        // --- TABLA 2: TODAS LAS QUE YA TIENEN SEGUIMIENTO (SIN EXCEPCIÓN) ---
        // Traemos absolutamente todos los registros de la tabla PLNACCIONSEGUIMIENTO para este PAO
        this.listaSeguimientosYaRealizados = busqPep.buscarSeguimientosPorPao(paoActual.getIdpao());
        
        // Inyectamos el periodo y cumplimiento a los registros que ya existen para que se vean en la tabla
        for (Plnaccionseguimiento seg : listaSeguimientosYaRealizados) {
             Plnaccioneval ev = busqPep.buscarEvaluacionPorAccion(seg.getIdaccionpao().getIdaccionpao());
             if (ev != null) {
                 seg.setPeriodoEvaluacion(ev.getPerieval());
                 seg.setCumplimientoEvaluacion(ev.getCumplipct());
             }
        }

        // --- TABLA 1: SOLO LAS PENDIENTES (FILTRADAS POR UMBRAL < 80%) ---
        List<Plnacciondeta> todasAcciones = busqPep.buscarAccionesPorPao(paoActual.getIdpao());

        for (Plnacciondeta ax : todasAcciones) {
            // Buscamos su evaluación para ver si "califica" como crítica
            Plnaccioneval eval = busqPep.buscarEvaluacionPorAccion(ax.getIdaccionpao());

            if (eval != null && eval.getCumplipct() != null) {
                // REGLA: Si es menor al 80%...
                if (eval.getCumplipct().compareTo(this.umbralCumplimiento) < 0) {
                    
                    // ...pero solo si aún NO le han registrado un seguimiento
                    boolean yaRegistrada = false;
                    for (Plnaccionseguimiento s : listaSeguimientosYaRealizados) {
                        if (s.getIdaccionpao().getIdaccionpao().equals(ax.getIdaccionpao())) {
                            yaRegistrada = true;
                            break;
                        }
                    }

                    if (!yaRegistrada) {
                        ax.setUltimaEvaluacion(eval); 
                        this.listaCriticasPendientes.add(ax);
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
public void imprimirSeguimientos() {
    try {
        if (this.paoActual == null || this.paoActual.getIdpao() == null) return;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("idPao", paoActual.getIdpao().intValue());
        
        if (this.periodoFiltroReporte == null || this.periodoFiltroReporte.equals("0") || this.periodoFiltroReporte.trim().isEmpty()) {
            params.put("perievalFiltro", null);
        } else {
            params.put("perievalFiltro", this.periodoFiltroReporte.trim());
        }

        imprimirReportePln(params, "rptSeguimientoGerencialPAO");
    } catch (Exception e) {
        showMsg("Error: " + e.getMessage(), ValidaDatos.ERROR);
    }
}

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="MÓDULO PLANIFICACIÓN TRIMESTRAL">
    /**
     * ABRE EL POPUP PARA GESTIONAR LA PLANIFICACIÓN TRIMESTRAL DE UNA ACCIÓN.
     */
    public void abrirPlanificacionTrim(Plnacciondeta accion) {
        this.accionPadreSeleccionada = accion;

        cargarDetallesTrim();
        prepararNuevoDetalleTrim();

        cargarResumenJerarquiaUnificado(accion);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupTrim.show();");
    }

    /**
     * CARGA LOS DETALLES TRIMESTRALES DE LA ACCIÓN SELECCIONADA.
     */
    public void cargarDetallesTrim() {
        listaDetallesTrim.clear();
        if (accionPadreSeleccionada != null && accionPadreSeleccionada.getIdaccionpao() != null) {
            try {
                List<Plnaccidetplantrim> res = busqPep.buscarDetallesPorAccion(accionPadreSeleccionada.getIdaccionpao());
                if (res != null) {
                    listaDetallesTrim.addAll(res);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar los datos de las acciones trimestrales: " + e.getMessage(), ValidaDatos.ERROR);
            }
        }
    }

    /**
     * INICIALIZA UN NUEVO DETALLE TRIMESTRAL.
     */
    public void prepararNuevoDetalleTrim() {
        this.detalleTrimActual = new Plnaccidetplantrim();
        this.detalleTrimActual.setIdaccionpao(accionPadreSeleccionada);
        this.detalleTrimActual.setTrim(null);
    }

    /**
     * PREPARA UN DETALLE TRIMESTRAL PARA EDICIÓN.
     */
    public void editarDetalleTrim(Plnaccidetplantrim item) {
        this.detalleTrimActual = item;
    }

    /**
     * GUARDA O ACTUALIZA UN DETALLE DE PLANIFICACIÓN TRIMESTRAL. Calcula
     * automáticamente el trimestre basado en el mes de inicio.
     */
    public void guardarDetalleTrim() {
        try {
            if (esPaoBloqueado()) {
                showMsg("El PAO está aprobado. No se puede modificar la planificación.", ValidaDatos.WARNING);
                return;
            }

            if (detalleTrimActual.getMesini() > detalleTrimActual.getMesfin()) {
                showMsg("El mes de inicio no puede ser mayor al mes de fin.", ValidaDatos.WARNING);
                return;
            }

            if (detalleTrimActual.getActiviprogra() == null || detalleTrimActual.getActiviprogra().isEmpty()) {
                showMsg("La Actividad Programada es obligatoria.", ValidaDatos.WARNING);
                return;
            }

            int m = detalleTrimActual.getMesini();
            short trimestreCalculado = (short) ((m <= 3) ? 1 : (m <= 6) ? 2 : (m <= 9) ? 3 : 4);
            detalleTrimActual.setTrim(trimestreCalculado);

            String user = sesion.getSegusuario().getCodusr();
            if (detalleTrimActual.getIddetalleplantrim() == null) {
                detalleTrimActual.setUsercrea(user);
                detalleTrimActual.setFchacrea(new java.util.Date());
            } else {
                detalleTrimActual.setUsermod(user);
                detalleTrimActual.setFchamod(new java.util.Date());
            }

            adminPep.guardarDetalleTrim(detalleTrimActual);

            showMsg("Planificación guardada correctamente.", ValidaDatos.INFO);
            cargarDetallesTrim();
            prepararNuevoDetalleTrim();

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar las acciones trimestrales: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * ELIMINA UN DETALLE DE PLANIFICACIÓN TRIMESTRAL.
     */
    public void eliminarDetalleTrim(Plnaccidetplantrim item) {
        try {
            if (esPaoBloqueado()) {
                showMsg("El PAO está aprobado. No se puede eliminar.", ValidaDatos.WARNING);
                return;
            }
            adminPep.eliminarDetalleTrim(item);
            showMsg("Registro eliminado.", ValidaDatos.INFO);
            cargarDetallesTrim();
            prepararNuevoDetalleTrim();
        } catch (Exception e) {
            showMsg("Error eliminando: " + e.getMessage(), ValidaDatos.ERROR);
        }
        prepararEliminarGenerico(item, DEL_TAREA);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO DASHBOARD Y RESUMEN">
    /**
     * CARGA EL RESUMEN GENERAL AGRUPADO POR ESTRATEGIA. Agrupa todas las
     * acciones del PAO por línea estratégica.
     */
    public void cargarResumenGeneral() {
        listaResumen = new ArrayList<ResumenPaoDTO>();

        if (paoActual == null || paoActual.getIdpao() == null) {
            showMsg("Debe seleccionar o guardar un PAO primero.", 2);
            return;
        }

        try {
            List<Plnacciondeta> todasLasAcciones = busqPep.buscarAccionesPorPao(paoActual.getIdpao());

            if (todasLasAcciones != null && !todasLasAcciones.isEmpty()) {
                HashMap<BigInteger, ResumenPaoDTO> mapa = new HashMap<BigInteger, ResumenPaoDTO>();

                for (Plnacciondeta ax : todasLasAcciones) {
                    Plnpeplinestr est = ax.getIdestrategia();

                    if (est != null) {
                        ResumenPaoDTO dto = mapa.get(est.getIdestrategia());

                        if (dto == null) {
                            dto = new ResumenPaoDTO(est);
                            mapa.put(est.getIdestrategia(), dto);
                            listaResumen.add(dto);
                        }
                        dto.getAcciones().add(ax);
                    }
                }
            }
            stateMapResumen = new RowStateMap();
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupDashboard.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error cargando resumen general del PAO: " + e.getMessage(), 3);
        }
    }

    /**
     * ACTIVA EL MODO EDICIÓN PARA UNA ACCIÓN EN EL RESUMEN GENERAL DE ACCIONES.
     */
    public void activarEdicion(Plnacciondeta a) {
        a.setEditando(true);
    }

    /**
     * CANCELA EL MODO EDICIÓN PARA UNA ACCIÓN EN EL RESUMEN GENERAL DE
     * ACCIONES.
     */
    public void cancelarEdicion(Plnacciondeta a) {
        a.setEditando(false);
    }

    /**
     * GENERA EL DASHBOARD DE CUMPLIMIENTO POR OBJETIVO ESTRATÉGICO. Calcula
     * métricas globales de cumplimiento.
     */
    public void generarDashboardCumplimiento() {
        recalcularMetricasDashboard();

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupResumenOE.show();");
    }

    /**
     * RECALCULA LAS MÉTRICAS DEL DASHBOARD DE CUMPLIMIENTO. Agrupa por Objetivo
     * Estratégico y procesa acciones planificadas y no planificadas.
     */
    private void recalcularMetricasDashboard() {
        listaDashboardOE.clear();

        if (paoActual == null || paoActual.getIdpao() == null) {
            return;
        }

        try {
            Map<BigInteger, ResumenCumplimientoDTO> mapaAgrupacion = new LinkedHashMap<BigInteger, ResumenCumplimientoDTO>();

            List<Plnacciondeta> planificadas = busqPep.buscarAccionesPorPao(paoActual.getIdpao());
            if (planificadas != null) {
                for (Plnacciondeta ax : planificadas) {
                    if (ax.getIdestrategia() != null && ax.getIdestrategia().getIdindicadorcump() != null) {
                        Plnpepobjetivo obj = ax.getIdestrategia().getIdindicadorcump().getIdobjetivo();
                        ResumenCumplimientoDTO dto = obtenerOcrearDTO(mapaAgrupacion, obj);

                        Plnaccioneval eval = busqPep.buscarEvaluacionPorAccion(ax.getIdaccionpao());
                        if (eval != null && eval.getCumplipct() != null) {
                            dto.agregarEvaluacion(eval.getCumplipct(), ax);
                        } else {
                            dto.agregarAccionSinEvaluar(ax);
                        }
                    }
                }
            }

            List<Plnaccionsinplan> noPlanificadas = busqPep.buscarAccionesNoPlanificadas(paoActual.getIdpao());
            if (noPlanificadas != null) {
                for (Plnaccionsinplan np : noPlanificadas) {
                    if (np.getIdestrategia() != null && np.getIdestrategia().getIdindicadorcump() != null) {
                        Plnpepobjetivo obj = np.getIdestrategia().getIdindicadorcump().getIdobjetivo();
                        ResumenCumplimientoDTO dto = obtenerOcrearDTO(mapaAgrupacion, obj);

                        if (np.getCumplipct() != null) {
                            dto.agregarEvaluacion(np.getCumplipct(), np);
                        } else {
                            dto.agregarAccionSinEvaluar(np);
                        }
                    }
                }
            }

            listaDashboardOE.addAll(mapaAgrupacion.values());

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al calcular el cumplimiento global: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * OBTIENE O CREA UN DTO DE CUMPLIMIENTO PARA UN OBJETIVO ESTRATÉGICO.
     */
    private ResumenCumplimientoDTO obtenerOcrearDTO(Map<BigInteger, ResumenCumplimientoDTO> mapa, Plnpepobjetivo obj) {
        BigInteger idObj = obj.getIdobjetivo();
        if (!mapa.containsKey(idObj)) {
            String nomPers = "Sin Datos";
            if (obj.getIdperspectiva() != null && obj.getIdperspectiva().getPerspectivaMaestra() != null) {
                nomPers = obj.getIdperspectiva().getPerspectivaMaestra().getNombre();
            }

            ResumenCumplimientoDTO nuevoDto = new ResumenCumplimientoDTO(nomPers, obj.getCodigooe(), obj.getDescrip());
            mapa.put(idObj, nuevoDto);
        }
        return mapa.get(idObj);
    }

    /**
     * GUARDA UNA ACCIÓN EDITADA DIRECTAMENTE DESDE EL DASHBOARD.
     */
    public void guardarAccionDesdeDashboard(Plnacciondeta accionEditada) {
        try {
            if (accionEditada == null) {
                return;
            }

            if (accionEditada.getDescrip() == null || accionEditada.getDescrip().trim().isEmpty()) {
                showMsg("La descripción es obligatoria.", ValidaDatos.WARNING);
                return;
            }
            if (accionEditada.getMedioverifi() == null || accionEditada.getMedioverifi().trim().isEmpty()) {
                showMsg("El medio de verificación es obligatoria.", ValidaDatos.WARNING);
                return;
            }
            if (accionEditada.getPresupropues() != null && accionEditada.getPresupropues().compareTo(BigDecimal.ZERO) < 0) {
                showMsg("El presupuesto no puede ser negativo o estar vacío.", ValidaDatos.WARNING);
                return;
            }

            String user = (sesion != null && sesion.getSegusuario() != null) ? sesion.getSegusuario().getCodusr() : "SISTEMA";
            accionEditada.setUsermod(user);
            accionEditada.setFchamod(new java.util.Date());

            adminPep.guardarAccionPao(accionEditada, user);

            accionEditada.setEditando(false);

            showMsg("Cambios guardados correctamente.", ValidaDatos.INFO);

            recalcularMetricasDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar accion desde la ventana emergente: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * LISTENER PARA EDICIÓN EN LÍNEA (DASHBOARD). Se ejecuta cuando se confirma
     * la edición de una fila en la tabla.
     */
    public void eventoEditarAccionDashboard(RowEditEvent event) {
        try {
            Plnacciondeta accionEditada = (Plnacciondeta) event.getObject();

            if (accionEditada == null) {
                return;
            }

            if (accionEditada.getDescrip() == null || accionEditada.getDescrip().trim().isEmpty()) {
                showMsg("La descripción es obligatoria.", ValidaDatos.WARNING);
                return;
            }

            String user = (sesion != null && sesion.getSegusuario() != null) ? sesion.getSegusuario().getCodusr() : "SISTEMA";
            accionEditada.setUsermod(user);
            accionEditada.setFchamod(new java.util.Date());

            adminPep.guardarAccionPao(accionEditada, user);

            showMsg("Acción actualizada correctamente.", ValidaDatos.INFO);

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar accion desde la ventana emergente: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO RESUMEN JERÁRQUICO (DTO)">
    /**
     * CARGA EL RESUMEN JERÁRQUICO UNIFICADO PARA CUALQUIER ENTIDAD.
     * Plnacciondeta, Plnaccidetplantrim, Plnaccioneval, Plnaccionseguimiento,
     * Plnaccionsinplan.
     */
    public void cargarResumenJerarquiaUnificado(Object entidad) {
        this.resumenJerarquia = new ResumenJerarquiaDTO();
        BigInteger idEst = null;
        String nombreAccion = "";

        try {
            if (entidad instanceof Plnacciondeta) {
                Plnacciondeta tmp = (Plnacciondeta) entidad;
                nombreAccion = tmp.getDescrip();
                if (tmp.getIdestrategia() != null) {
                    idEst = tmp.getIdestrategia().getIdestrategia();
                }
            } else if (entidad instanceof Plnaccidetplantrim) {
                Plnacciondeta padre = ((Plnaccidetplantrim) entidad).getIdaccionpao();
                nombreAccion = padre.getDescrip();
                if (padre.getIdestrategia() != null) {
                    idEst = padre.getIdestrategia().getIdestrategia();
                }
            } else if (entidad instanceof Plnaccioneval) {
                Plnacciondeta padre = ((Plnaccioneval) entidad).getIdaccionpao();
                nombreAccion = padre.getDescrip();
                if (padre.getIdestrategia() != null) {
                    idEst = padre.getIdestrategia().getIdestrategia();
                }
            } else if (entidad instanceof Plnaccionseguimiento) {
                Plnacciondeta padre = ((Plnaccionseguimiento) entidad).getIdaccionpao();
                nombreAccion = padre.getDescrip();
                if (padre.getIdestrategia() != null) {
                    idEst = padre.getIdestrategia().getIdestrategia();
                }
            } else if (entidad instanceof Plnaccionsinplan) {
                Plnaccionsinplan tmp = (Plnaccionsinplan) entidad;
                nombreAccion = tmp.getDescrip();
                if (tmp.getIdestrategia() != null) {
                    idEst = tmp.getIdestrategia().getIdestrategia();
                }
            }

            if (idEst != null) {
                this.resumenJerarquia = busqPep.obtenerResumenPorEstrategia(idEst);
            }
            this.resumenJerarquia.setAccion(nombreAccion);

        } catch (Exception e) {
            e.printStackTrace();
            this.resumenJerarquia = new ResumenJerarquiaDTO();
            this.resumenJerarquia.setAccion("Error cargando datos.");
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INICIATIVAS - CRUD">
    /**
     * PREPARA EL POPUP PARA CREAR UNA NUEVA INICIATIVA.
     */
    public void prepararNuevaIniciativa() {
        if (filtroInicIdIndicador == null) {
            showMsg("Seleccione un Indicador en los filtros para crear la iniciativa.", ValidaDatos.WARNING);
            return;
        }
        iniciativaActual = new Plnpaoinic();
        iniciativaActual.setDescrip("");
        editandoIniciativa = false;

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupIniciativa.show();");
    }

    /**
     * PREPARA UNA INICIATIVA PARA EDICIÓN.
     */
    public void prepararEditarIniciativa(Plnpaoinic item) {
        this.iniciativaActual = item;
        this.editandoIniciativa = true;
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupIniciativa.show();");
    }

    /**
     * GUARDA O ACTUALIZA UNA INICIATIVA. Realiza validaciones de descripción,
     * longitud y relación con indicador.
     */
    public void guardarIniciativa() {
        try {
            if (iniciativaActual.getDescrip() == null
                    || iniciativaActual.getDescrip().trim().isEmpty()) {
                showMsg("La descripción es obligatoria.", ValidaDatos.WARNING);
                return;
            }
            if (iniciativaActual.getDescrip().length() > 500) {
                showMsg("La descripción no puede exceder 500 caracteres.",
                        ValidaDatos.WARNING);
                return;
            }
            if (filtroInicIdIndicador == null) {
                showMsg("Error: No se ha seleccionado un Indicador padre.",
                        ValidaDatos.ERROR);
                return;
            }

            if (iniciativaActual.getIdindicadorcump() == null) {
                Plnpepindicumpl padre = busqPep.buscarIndicadorPorId(
                        filtroInicIdIndicador);
                iniciativaActual.setIdindicadorcump(padre);
            }

            // ✅ Asignar el coordinador dueño al crear
            if (iniciativaActual.getIdiniciativa() == null) {
                iniciativaActual.setIdcoordinador(miEmpleado);
            }
            // ✅ Al editar NO se cambia el dueño — se respeta al creador original

            String user = "SISTEMA";
            if (sesion != null && sesion.getSegusuario() != null
                    && sesion.getSegusuario().getCodusr() != null) {
                user = sesion.getSegusuario().getCodusr().trim();
            }
            if (user.length() > 20) {
                user = user.substring(0, 20);
            }

            if (iniciativaActual.getIdiniciativa() == null) {
                iniciativaActual.setUsercrea(user);
                iniciativaActual.setFchacrea(new java.util.Date());
            } else {
                iniciativaActual.setUsermod(user);
                iniciativaActual.setFchamod(new java.util.Date());
                if (iniciativaActual.getUsercrea() == null) {
                    iniciativaActual.setUsercrea(user);
                }
                if (iniciativaActual.getFchacrea() == null) {
                    iniciativaActual.setFchacrea(new java.util.Date());
                }
            }

            adminPep.guardarIniciativa(iniciativaActual);

            showMsg(editandoIniciativa ? "Iniciativa actualizada."
                    : "Iniciativa creada.", ValidaDatos.INFO);
            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(), "popupIniciativa.hide();");

            cambioFiltroInicIndicador(null);

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                msg = e.getCause().getMessage();
            }
            showMsg("Error al guardar: " + msg, ValidaDatos.ERROR);
        }
    }

    /**
     * ELIMINA UNA INICIATIVA.
     */
    public void eliminarIniciativa(Plnpaoinic item) {
        this.iniciativaAEliminar = item;
        this.tipoEliminacion = DEL_INICIATIVA;
        this.mensajeConfirmacion = "¿Seguro que desea eliminar esta Iniciativa?";
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.show();");
    }

    public void cargarResumenMisIniciativas() {
        listaResumenIniciativas = new ArrayList<>();
        try {
            if (miCodemp != null) {
                listaResumenIniciativas = busqPep
                        .buscarResumenIniciativasPorCoordinador(miCodemp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar resumen de iniciativas: "
                    + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="MÓDULO INICIATIVAS - Vinculación de Acciones">
    /**
     * CARGA LOS COMBOS INICIALES PARA EL MÓDULO DE INICIATIVAS. Carga las
     * perspectivas del PEP actual.
     */
    public void cargarCombosIniciativa() {
        listaInicPerspectivas.clear();
        if (paoActual != null && paoActual.getIdpep() != null) {
            try {
                List<Plnperspectivadeta> list = busqPep.buscarPerspectivasPorPep(paoActual.getIdpep().getIdpep());
                if (list != null) {
                    for (Plnperspectivadeta p : list) {
                        listaInicPerspectivas.add(new SelectItem(p.getIdperspectiva(), p.getPerspectivaMaestra().getNombre()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar datos de iniciativas: " + e.getMessage(), ValidaDatos.ERROR);
            }
        }
    }

    public void cargarListasVinculacion() {
        this.listaAccionesVinculadas = new ArrayList<Plnacciondeta>();
        this.listaAccionesDisponibles = new ArrayList<Plnacciondeta>();

        try {
            if (this.iniciativaActual == null
                    || this.iniciativaActual.getIdiniciativa() == null) {
                return;
            }
            if (this.paoActual == null
                    || this.paoActual.getIdpao() == null) {
                return;
            }

            BigInteger idIniciativa = this.iniciativaActual.getIdiniciativa();

            if (this.iniciativaActual.getIdindicadorcump() == null) {
                Plnpaoinic inicFull = busqPep.buscarIniciativaPorId(
                        idIniciativa);
                if (inicFull != null) {
                    this.iniciativaActual = inicFull;
                }
            }

            BigInteger idIndicador = null;
            if (this.iniciativaActual.getIdindicadorcump() != null) {
                idIndicador = this.iniciativaActual.getIdindicadorcump()
                        .getIdindicadorcump();
            }

            // ✅ Solo acciones vinculadas del PAO activo
            this.listaAccionesVinculadas = busqPep
                    .buscarAccionesVinculadasPorIniciativaYPao(
                    idIniciativa, this.paoActual.getIdpao());

            if (idIndicador != null) {
                this.listaAccionesDisponibles = busqPep
                        .buscarAccionesDisponiblesParaIniciativa(
                        idIndicador, this.paoActual.getIdpao());
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al cargar listas de vinculación: "
                    + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * PREPARA EL POPUP PARA VINCULAR ACCIONES A UNA INICIATIVA.
     */
    public void prepararVinculacion(Plnpaoinic inic) {
        this.iniciativaActual = inic;
        cargarListasVinculacion();
        cargarResumenMisIniciativas();
        JavascriptContext.addJavascriptCall(
                FacesContext.getCurrentInstance(),
                "popupVincularAcciones.show();");
    }

    /**
     * VINCULA UNA ACCIÓN A LA INICIATIVA ACTUAL.
     */
    public void agregarAccionAIniciativa(Plnacciondeta accion) {
        try {
            Plnpaoinicdeta nuevoVinculo = new Plnpaoinicdeta();

            nuevoVinculo.setIdiniciativa(this.iniciativaActual);

            nuevoVinculo.setIdacciondeta(accion);

            adminPep.guardarVinculoIniciativa(nuevoVinculo);

            cargarListasVinculacion();

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al vincular la accion a la iniciativa: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    public void quitarAccionDeIniciativa(Plnacciondeta accion) {
        try {
            // ✅ Validar que la acción pertenece al PAO activo
            if (accion.getIdpao() == null
                    || !accion.getIdpao().getIdpao()
                    .equals(paoActual.getIdpao())) {
                showMsg("No puede desvincular esta acción porque "
                        + "pertenece a otro PAO.",
                        ValidaDatos.WARNING);
                return;
            }

            // ✅ Validar que el usuario es dueño de la iniciativa
            if (iniciativaActual.getIdcoordinador() == null
                    || !iniciativaActual.getIdcoordinador().getCodemp()
                    .equals(miCodemp)) {
                showMsg("No puede desvincular acciones de una iniciativa "
                        + "que no le pertenece.",
                        ValidaDatos.WARNING);
                return;
            }

            Plnpaoinicdeta vinculoBorrar = busqPep.buscarVinculo(
                    this.iniciativaActual.getIdiniciativa(),
                    accion.getIdaccionpao());

            if (vinculoBorrar != null) {
                adminPep.eliminarVinculoIniciativa(vinculoBorrar);
                cargarListasVinculacion();
                // ✅ Actualizar conteos del resumen
                cargarResumenMisIniciativas();
            } else {
                showMsg("No se encontró el vínculo para eliminar.",
                        ValidaDatos.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al desvincular: " + e.getMessage(),
                    ValidaDatos.ERROR);
        }
    }

    /**
     * EVENTO DE CAMBIO DE FILTRO INDICADOR (INICIATIVAS). Carga la tabla de
     * iniciativas del indicador seleccionado.
     */
    public void cambioFiltroInicIndicador(AjaxBehaviorEvent event) {
        listaIniciativasPao.clear();
        if (filtroInicIdIndicador != null && miCodemp != null) {
            try {
                // ✅ Solo carga las iniciativas del usuario actual
                listaIniciativasPao = busqPep
                        .buscarIniciativasPorIndicadorYCoordinador(
                        filtroInicIdIndicador, miCodemp);
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar iniciativas: " + e.getMessage(),
                        ValidaDatos.ERROR);
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INICIATIVAS - Filtros y Combos en Cascada">
    /**
     * EVENTO DE CAMBIO DE FILTRO PERSPECTIVA (INICIATIVAS). Carga los objetivos
     * estratégicos y limpia selecciones inferiores.
     */
    public void cambioFiltroInicPerspectiva(AjaxBehaviorEvent event) {
        listaInicObjetivos.clear();
        listaInicIndicadores.clear();
        listaIniciativasPao.clear();
        filtroInicIdObjetivo = null;
        filtroInicIdIndicador = null;

        if (filtroInicIdPerspectiva != null) {
            try {
                List<Plnpepobjetivo> list = busqPep.buscarObjetivosPorPerspectiva(filtroInicIdPerspectiva);
                for (Plnpepobjetivo o : list) {
                    listaInicObjetivos.add(new SelectItem(o.getIdobjetivo(), o.getCodigooe() + " - " + o.getDescrip()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar los datos en el combo de "
                        + "perspectiva para iniciativas: " + e.getMessage(), ValidaDatos.ERROR);
            }
        }
    }

    /**
     * EVENTO DE CAMBIO DE FILTRO OBJETIVO (INICIATIVAS). Carga los indicadores
     * de cumplimiento.
     */
    public void cambioFiltroInicObjetivo(AjaxBehaviorEvent event) {
        listaInicIndicadores.clear();
        listaIniciativasPao.clear();
        filtroInicIdIndicador = null;

        if (filtroInicIdObjetivo != null) {
            try {
                List<Plnpepindicumpl> list = busqPep.buscarIndicadoresPorObjetivo(filtroInicIdObjetivo);
                for (Plnpepindicumpl i : list) {
                    listaInicIndicadores.add(new SelectItem(i.getIdindicadorcump(), i.getCodigoindicador() + " - " + i.getDescrip()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Error al cargar los datos en el combo de "
                        + "objetivo estratégicos para iniciativas: " + e.getMessage(), ValidaDatos.ERROR);
            }
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÉTODOS UTILITARIOS">
    /**
     * PROCESA EXCEPCIONES DE VALIDACIÓN Y MUESTRA MENSAJES.
     */
private void procesarValidacionExcepcion(ValidacionExcepcion ve) {
    if (ve.getMensajes() != null && !ve.getMensajes().isEmpty()) {
        for (String msj : ve.getMensajes()) {
            showMsg(msj, ValidaDatos.ERROR);
        }
    } else {
        showMsg(ve.getMessage(), ValidaDatos.ERROR);
    }
}

    /**
     * MUESTRA UN MENSAJE AL USUARIO.
     */
private void showMsg(String msg, int severity) {
    JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "mensaje.show();");
    this.validar.setMsgValidation(msg, "dialog", severity, null, null, null);
}

    /**
     * MUESTRA UN MENSAJE DE ERROR CON LOG DE EXCEPCIÓN.
     */
 private void showMsgLog(Throwable ex, int severity) {
    JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "mensaje.show();");
    this.validar.setMsgValidation(ex.getMessage(), "dialog", severity, ex.getLocalizedMessage(), this.getClass(), ex);
}
    
    /**
     * VALIDA LA LONGITUD DE UN CAMPO DE TEXTO.
     *
     * @param valor El valor a validar
     * @param nombreCampo El nombre del campo (para el mensaje)
     * @param maxLongitud La longitud máxima permitida
     * @return true si es válido, false si excede
     */
    private boolean validarLongitudCampo(String valor, String nombreCampo, int maxLongitud) {
        if (valor != null && valor.length() > maxLongitud) {
            this.showMsg("El campo '" + nombreCampo + "' no puede exceder " + maxLongitud
                    + " caracteres. Actualmente tiene " + valor.length() + " caracteres.",
                    ValidaDatos.WARNING);
            return false;
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO RESUMEN PEP">
//    public void generarReporteEjecutivo() {
//        listaReportePorPerspectiva = new ArrayList<ReportePerspectivaDTO>();
//        Map<String, ReportePerspectivaDTO> mapaAgrupado = new java.util.LinkedHashMap<String, ReportePerspectivaDTO>();
//
//        try {
//            if (paoActual == null || paoActual.getIdpao() == null) {
//                showMsg("Debe guardar o seleccionar un PAO primero.", ValidaDatos.WARNING);
//                return;
//            }
//
//            List<Plnpaoinic> iniciativas = busqPep.buscarIniciativasPorPep(paoActual.getIdpep().getIdpep());
//
//            for (Plnpaoinic inic : iniciativas) {
//                ReporteIniciativaDTO dtoInic = new ReporteIniciativaDTO(inic);
//                boolean tieneAccionesDelPao = false;
//
//                List<Plnacciondeta> accionesVinculadas = busqPep.buscarAccionesPorIniciativa(inic.getIdiniciativa());
//
//                if (accionesVinculadas != null) {
//                    for (Plnacciondeta ax : accionesVinculadas) {
//
//                        if (ax.getIdpao() == null || !ax.getIdpao().getIdpao().equals(paoActual.getIdpao())) {
//                            continue;
//                        }
//
//                        tieneAccionesDelPao = true;
//
//                        ReporteAccionDTO dtoAccion = new ReporteAccionDTO(ax);
//
//List<Plnaccioneval> evaluaciones = busqPep.buscarEvaluacionesPorAccion(ax.getIdaccionpao());
//if (evaluaciones != null && !evaluaciones.isEmpty()) {
//    // La última es la más reciente (ya vienen ordenadas por fchacrea ASC)
//    Plnaccioneval ultima = evaluaciones.get(evaluaciones.size() - 1);
//    dtoAccion.setPorcentajeCumplimiento(ultima.getCumplipct());
//    dtoAccion.setPeriodoEvaluacion(ultima.getPerieval()); // ← campo nuevo en DTO
//}
//
//                        List<Plnaccidetplantrim> trims = busqPep.buscarDetallesPorAccion(ax.getIdaccionpao());
//                        if (trims != null) {
//                            for (Plnaccidetplantrim t : trims) {
//                                dtoAccion.agregarPorRangoMeses(
//                                        t.getMesini(),
//                                        t.getMesfin(),
//                                        t.getActiviprogra());
//                            }
//                        }
//
//                        dtoInic.getAcciones().add(dtoAccion);
//                    }
//                }
//
//                if (tieneAccionesDelPao) {
//                    String nombrePersp = dtoInic.getNombrePerspectiva();
//                    if (nombrePersp == null || nombrePersp.isEmpty()) {
//                        nombrePersp = "OTRAS";
//                    }
//
//                    if (!mapaAgrupado.containsKey(nombrePersp)) {
//                        mapaAgrupado.put(nombrePersp, new ReportePerspectivaDTO(nombrePersp));
//                    }
//
//                    mapaAgrupado.get(nombrePersp).getListaIniciativas().add(dtoInic);
//                }
//            }
//
//            listaReportePorPerspectiva.addAll(mapaAgrupado.values());
//
//            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupReporteEjecutivo.show();");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            showMsg("Error generando el resumen del PAO: " + e.getMessage(), ValidaDatos.ERROR);
//        }
//    }
    public void generarReporteEjecutivo() {
        listaReportePorPerspectiva = new ArrayList<ReportePerspectivaDTO>();
        Map<String, ReportePerspectivaDTO> mapaAgrupado =
                new java.util.LinkedHashMap<String, ReportePerspectivaDTO>();

        try {
            if (paoActual == null || paoActual.getIdpao() == null) {
                showMsg("Debe guardar o seleccionar un PAO primero.",
                        ValidaDatos.WARNING);
                return;
            }

            // ✅ Solo trae iniciativas del usuario actual
            List<Plnpaoinic> iniciativas = busqPep.buscarIniciativasPorPepYCoordinador(
                    paoActual.getIdpep().getIdpep(), miCodemp);

            for (Plnpaoinic inic : iniciativas) {
                ReporteIniciativaDTO dtoInic = new ReporteIniciativaDTO(inic);
                boolean tieneAccionesDelPao = false;

                List<Plnacciondeta> accionesVinculadas =
                        busqPep.buscarAccionesPorIniciativa(
                        inic.getIdiniciativa());

                if (accionesVinculadas != null) {
                    for (Plnacciondeta ax : accionesVinculadas) {
                        if (ax.getIdpao() == null
                                || !ax.getIdpao().getIdpao()
                                .equals(paoActual.getIdpao())) {
                            continue;
                        }

                        tieneAccionesDelPao = true;
                        ReporteAccionDTO dtoAccion = new ReporteAccionDTO(ax);

                        List<Plnaccioneval> evaluaciones =
                                busqPep.buscarEvaluacionesPorAccion(
                                ax.getIdaccionpao());
                        if (evaluaciones != null && !evaluaciones.isEmpty()) {
                            Plnaccioneval ultima = evaluaciones
                                    .get(evaluaciones.size() - 1);
                            dtoAccion.setPorcentajeCumplimiento(
                                    ultima.getCumplipct());
                            dtoAccion.setPeriodoEvaluacion(ultima.getPerieval());
                        }

                        List<Plnaccidetplantrim> trims =
                                busqPep.buscarDetallesPorAccion(
                                ax.getIdaccionpao());
                        if (trims != null) {
                            for (Plnaccidetplantrim t : trims) {
                                dtoAccion.agregarPorRangoMeses(
                                        t.getMesini(), t.getMesfin(),
                                        t.getActiviprogra());
                            }
                        }
                        dtoInic.getAcciones().add(dtoAccion);
                    }
                }

                if (tieneAccionesDelPao) {
                    String nombrePersp = dtoInic.getNombrePerspectiva();
                    if (nombrePersp == null || nombrePersp.isEmpty()) {
                        nombrePersp = "OTRAS";
                    }
                    if (!mapaAgrupado.containsKey(nombrePersp)) {
                        mapaAgrupado.put(nombrePersp,
                                new ReportePerspectivaDTO(nombrePersp));
                    }
                    mapaAgrupado.get(nombrePersp)
                            .getListaIniciativas().add(dtoInic);
                }
            }

            listaReportePorPerspectiva.addAll(mapaAgrupado.values());
            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(),
                    "popupReporteEjecutivo.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error generando el resumen: " + e.getMessage(),
                    ValidaDatos.ERROR);
        }
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="MÓDULO REPORTES - Vista General PEP">

    /**
     * CARGA LA MATRIZ ESTRATÉGICA COMPLETA DEL PEP. Calcula KPIs globales
     * (perspectivas, objetivos, indicadores, estrategias).
     *
     */
    public void cargarVistaGeneralPep() {

        this.lstArbolCompleto = new ArrayList<ResumenPepDTO>();
        this.totalGlobalPerspectivas = 0;
        this.totalGlobalObjetivos = 0;
        this.totalGlobalIndicadores = 0;
        this.totalGlobalEstrategias = 0;

        BigInteger idPepParaConsultar = (paoActual != null && paoActual.getIdpep() != null)
                ? paoActual.getIdpep().getIdpep()
                : selIdPepCabecera;

        if (idPepParaConsultar == null) {
            showMsg("Debe seleccionar un PEP para su PAO para ver su matriz.", ValidaDatos.WARNING);
            return;
        }

        try {
            List<Object[]> filas = busqPep.obtenerDetallesPep(idPepParaConsultar);

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

    // <editor-fold defaultstate="collapsed" desc="GETTER Y SETTER">
    public void setSesion(Segsesion s) {
        this.sesion = s;
    }

    public List<SelectItem> getListaPeps() {
        if (listaPeps == null) {
            listaPeps = new ArrayList<SelectItem>();
            cargarPeps();
        }
        return listaPeps;
    }

    public List<SelectItem> getListaPerspectivas() {
        if (listaPerspectivas == null) {
            listaPerspectivas = new ArrayList<SelectItem>();
        }
        return listaPerspectivas;
    }

    public List<SelectItem> getListaObjetivos() {
        if (listaObjetivos == null) {
            listaObjetivos = new ArrayList<SelectItem>();
        }
        return listaObjetivos;
    }

    public List<SelectItem> getListaIndicadores() {
        if (listaIndicadores == null) {
            listaIndicadores = new ArrayList<SelectItem>();
        }
        return listaIndicadores;
    }

    public List<SelectItem> getListaEstrategias() {
        if (listaEstrategias == null) {
            listaEstrategias = new ArrayList<SelectItem>();
        }
        return listaEstrategias;
    }

    public List<Plnacciondeta> getListaAcciones() {
        if (listaAcciones == null) {
            listaAcciones = new ArrayList<Plnacciondeta>();
        }
        return listaAcciones;
    }

    public List<Plnpao> getListaPaos() {
        if (listaPaos == null) {
            listaPaos = new ArrayList<Plnpao>();
        }
        return listaPaos;
    }

    public Plnpao getPaoActual() {
        return paoActual;
    }

    public void setPaoActual(Plnpao p) {
        this.paoActual = p;
    }

    public Plnacciondeta getAccion() {
        return accion;
    }

    public void setAccion(Plnacciondeta a) {
        this.accion = a;
    }

    public int getCambiarTagPanel() {
        return cambiarTagPanel;
    }

    public void setCambiarTagPanel(int i) {
        this.cambiarTagPanel = i;
    }

    public boolean isNuevoPao() {
        return nuevoPao;
    }

    public boolean isPanelAccionesVisible() {
        return panelAccionesVisible;
    }

    public RowStateMap getSmPao() {
        return smPao;
    }

    public void setSmPao(RowStateMap s) {
        this.smPao = s;
    }

    public RowStateMap getSmAccion() {
        return smAccion;
    }

    public void setSmAccion(RowStateMap s) {
        this.smAccion = s;
    }

    public String getTxtNombreUsuario() {
        return txtNombreUsuario;
    }

    public String getTxtAgencia() {
        return txtAgencia;
    }

    public String getTxtPuesto() {
        return txtPuesto;
    }

    public String getTxtArea() {
        return txtArea;
    }

    public String getTxtNombreJefe() {
        return txtNombreJefe;
    }

    public BigInteger getSelIdPep() {
        return selIdPep;
    }

    public void setSelIdPep(BigInteger s) {
        this.selIdPep = s;
    }

    public BigInteger getSelIdPerspectiva() {
        return selIdPerspectiva;
    }

    public void setSelIdPerspectiva(BigInteger s) {
        this.selIdPerspectiva = s;
    }

    public BigInteger getSelIdObjetivo() {
        return selIdObjetivo;
    }

    public void setSelIdObjetivo(BigInteger s) {
        this.selIdObjetivo = s;
    }

    public BigInteger getSelIdIndicador() {
        return selIdIndicador;
    }

    public void setSelIdIndicador(BigInteger s) {
        this.selIdIndicador = s;
    }

    public BigInteger getSelIdEstrategia() {
        return selIdEstrategia;
    }

    public void setSelIdEstrategia(BigInteger s) {
        this.selIdEstrategia = s;
    }

    public Plnaccioneval getEvaluacionActual() {
        return evaluacionActual;
    }

    public void setEvaluacionActual(Plnaccioneval evaluacionActual) {
        this.evaluacionActual = evaluacionActual;
    }

    public boolean isPanelEvaluacionVisible() {
        return panelEvaluacionVisible;
    }

    public List<Plnacciondeta> getListaAccionesParaEvaluar() {
        return listaAccionesParaEvaluar;
    }

    public List<Plnaccioneval> getListaEvaluacionesRealizadas() {
        return listaEvaluacionesRealizadas;
    }

    public void setListaEvaluacionesRealizadas(List<Plnaccioneval> l) {
        this.listaEvaluacionesRealizadas = l;
    }

    public List<Plnaccionsinplan> getListaNoPlanificadas() {
        return listaNoPlanificadas;
    }

    public List<Plnaccionsinplan> getListaNoPlanificadasEvaluadas() {
        return listaNoPlanificadasEvaluadas;
    }

    public void setListaNoPlanificadasEvaluadas(List<Plnaccionsinplan> lista) {
        this.listaNoPlanificadasEvaluadas = lista;
    }

    public Plnaccionsinplan getAccionNoPlanActual() {
        return accionNoPlanActual;
    }

    public void setAccionNoPlanActual(Plnaccionsinplan a) {
        this.accionNoPlanActual = a;
    }

    public Plnaccionseguimiento getSeguimientoActual() {
        return seguimientoActual;
    }

    public void setSeguimientoActual(Plnaccionseguimiento s) {
        this.seguimientoActual = s;
    }

    public Integer getFiltroAnioPao() {
        return filtroAnioPao;
    }

    public void setFiltroAnioPao(Integer filtroAnioPao) {
        this.filtroAnioPao = filtroAnioPao;
    }

    public BigInteger getFiltroIdPepPao() {
        return filtroIdPepPao;
    }

    public void setFiltroIdPepPao(BigInteger filtroIdPepPao) {
        this.filtroIdPepPao = filtroIdPepPao;
    }

    public String getFiltroNombrePao() {
        return filtroNombrePao;
    }

    public void setFiltroNombrePao(String filtroNombrePao) {
        this.filtroNombrePao = filtroNombrePao;
    }

    public BigInteger getFiltroIdPerspectiva() {
        return filtroIdPerspectiva;
    }

    public void setFiltroIdPerspectiva(BigInteger filtroIdPerspectiva) {
        this.filtroIdPerspectiva = filtroIdPerspectiva;
    }

    public BigInteger getFiltroIdObjetivo() {
        return filtroIdObjetivo;
    }

    public void setFiltroIdObjetivo(BigInteger filtroIdObjetivo) {
        this.filtroIdObjetivo = filtroIdObjetivo;
    }

    public BigInteger getFiltroIdIndicador() {
        return filtroIdIndicador;
    }

    public void setFiltroIdIndicador(BigInteger filtroIdIndicador) {
        this.filtroIdIndicador = filtroIdIndicador;
    }

    public BigInteger getFiltroIdEstrategia() {
        return filtroIdEstrategia;
    }

    public String getFiltroPeriodoEvaluacion() {
        return filtroPeriodoEvaluacion;
    }

    public void setFiltroPeriodoEvaluacion(String v) {
        this.filtroPeriodoEvaluacion = v;
    }

    public void setFiltroIdEstrategia(BigInteger filtroIdEstrategia) {
        this.filtroIdEstrategia = filtroIdEstrategia;
    }

    public List<Plnaccidetplantrim> getListaDetallesTrim() {
        return listaDetallesTrim;
    }

    public void setListaDetallesTrim(List<Plnaccidetplantrim> listaDetallesTrim) {
        this.listaDetallesTrim = listaDetallesTrim;
    }

    public Plnaccidetplantrim getDetalleTrimActual() {
        return detalleTrimActual;
    }

    public void setDetalleTrimActual(Plnaccidetplantrim detalleTrimActual) {
        this.detalleTrimActual = detalleTrimActual;
    }

    public Plnacciondeta getAccionPadreSeleccionada() {
        return accionPadreSeleccionada;
    }

    public void setAccionPadreSeleccionada(Plnacciondeta accionPadreSeleccionada) {
        this.accionPadreSeleccionada = accionPadreSeleccionada;
    }

    public String getNombreTrimestre(Short t) {
        if (t == null) {
            return "";
        }
        if (t == 1) {
            return "TRIMESTRE I (Ene-Mar)";
        }
        if (t == 2) {
            return "TRIMESTRE II (Abr-Jun)";
        }
        if (t == 3) {
            return "TRIMESTRE III (Jul-Sep)";
        }
        if (t == 4) {
            return "TRIMESTRE IV (Oct-Dic)";
        }
        return "OTRO";
    }

    public List<Plnacciondeta> getListaCriticasPendientes() {
        return listaCriticasPendientes;
    }

    public List<Plnaccionseguimiento> getListaSeguimientosYaRealizados() {
        return listaSeguimientosYaRealizados;
    }

    //
    //INICIATIVAS
    public List<Plnpaoinic> getListaIniciativasPao() {
        return listaIniciativasPao;
    }

    public void setListaIniciativasPao(List<Plnpaoinic> listaIniciativasPao) {
        this.listaIniciativasPao = listaIniciativasPao;
    }

    public Plnpaoinic getIniciativaActual() {
        return iniciativaActual;
    }

    public void setIniciativaActual(Plnpaoinic iniciativaActual) {
        this.iniciativaActual = iniciativaActual;
    }

    public boolean isEditandoIniciativa() {
        return editandoIniciativa;
    }
    //FIN INICIATIVAS

    public List<SelectItem> getListaPerspectivasEval() {
        return listaPerspectivasEval;
    }

    public void setListaPerspectivasEval(List<SelectItem> l) {
        this.listaPerspectivasEval = l;
    }

    public List<SelectItem> getListaObjetivosEval() {
        return listaObjetivosEval;
    }

    public void setListaObjetivosEval(List<SelectItem> l) {
        this.listaObjetivosEval = l;
    }

    public List<SelectItem> getListaIndicadoresEval() {
        return listaIndicadoresEval;
    }

    public void setListaIndicadoresEval(List<SelectItem> l) {
        this.listaIndicadoresEval = l;
    }

    public List<SelectItem> getListaEstrategiasEval() {
        return listaEstrategiasEval;
    }

    public void setListaEstrategiasEval(List<SelectItem> l) {
        this.listaEstrategiasEval = l;
    }

    public BigInteger getSelIdPepCabecera() {
        return selIdPepCabecera;
    }

    public void setSelIdPepCabecera(BigInteger selIdPepCabecera) {
        this.selIdPepCabecera = selIdPepCabecera;
    }

    public String getMensajeConfirmacion() {
        return mensajeConfirmacion;
    }

    public void setMensajeConfirmacion(String mensajeConfirmacion) {
        this.mensajeConfirmacion = mensajeConfirmacion;
    }

    public ResumenJerarquiaDTO getResumenJerarquia() {
        return resumenJerarquia;
    }

    public void setResumenJerarquia(ResumenJerarquiaDTO resumenJerarquia) {
        this.resumenJerarquia = resumenJerarquia;
    }

    public List<ResumenCumplimientoDTO> getListaDashboardOE() {
        return listaDashboardOE;
    }

    public List<Plnaccionseguimiento> getHistorialSeguimientos() {
        return historialSeguimientos;
    }

    public void setHistorialSeguimientos(List<Plnaccionseguimiento> historialSeguimientos) {
        this.historialSeguimientos = historialSeguimientos;
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

    public boolean isEditandoAccion() {
        return editandoAccion;
    }

    public List<ResumenPaoDTO> getListaResumen() {
        return listaResumen;
    }

    public RowStateMap getStateMapResumen() {
        return stateMapResumen;
    }

    public void setStateMapResumen(RowStateMap stateMapResumen) {
        this.stateMapResumen = stateMapResumen;
    }

    public List<Plnacciondeta> getListaAccionesVinculadas() {
        return listaAccionesVinculadas;
    }

    public void setListaAccionesVinculadas(List<Plnacciondeta> listaAccionesVinculadas) {
        this.listaAccionesVinculadas = listaAccionesVinculadas;
    }

    public List<Plnacciondeta> getListaAccionesDisponibles() {
        return listaAccionesDisponibles;
    }

    public void setListaAccionesDisponibles(List<Plnacciondeta> listaAccionesDisponibles) {
        this.listaAccionesDisponibles = listaAccionesDisponibles;
    }

    public BigInteger getFiltroInicIdPerspectiva() {
        return filtroInicIdPerspectiva;
    }

    public void setFiltroInicIdPerspectiva(BigInteger filtroInicIdPerspectiva) {
        this.filtroInicIdPerspectiva = filtroInicIdPerspectiva;
    }

    public BigInteger getFiltroInicIdObjetivo() {
        return filtroInicIdObjetivo;
    }

    public void setFiltroInicIdObjetivo(BigInteger filtroInicIdObjetivo) {
        this.filtroInicIdObjetivo = filtroInicIdObjetivo;
    }

    public BigInteger getFiltroInicIdIndicador() {
        return filtroInicIdIndicador;
    }

    public void setFiltroInicIdIndicador(BigInteger filtroInicIdIndicador) {
        this.filtroInicIdIndicador = filtroInicIdIndicador;
    }

    public List<SelectItem> getListaInicPerspectivas() {
        return listaInicPerspectivas;
    }

    public List<SelectItem> getListaInicObjetivos() {
        return listaInicObjetivos;
    }

    public List<SelectItem> getListaInicIndicadores() {
        return listaInicIndicadores;
    }

    public List<ReporteIniciativaDTO> getListaReporteEjecutivo() {
        return listaReporteEjecutivo;
    }

    public List<ReportePerspectivaDTO> getListaReportePorPerspectiva() {
        return listaReportePorPerspectiva;
    }

    public List<ResumenPepDTO> getLstArbolCompleto() {
        return lstArbolCompleto;
    }

    public BigDecimal getUmbralCumplimiento() {
        return this.umbralCumplimiento;
    }

    public List<Plnacciondeta> getListaAccionesBajoCumplimiento() {
        return listaAccionesBajoCumplimiento;
    }
    
    public String getPeriodoFiltroReporte() { return periodoFiltroReporte; }
public void setPeriodoFiltroReporte(String s) { this.periodoFiltroReporte = s; }
//public String getComentarioGerencialGeneral() { return comentarioGerencialGeneral; }
//public void setComentarioGerencialGeneral(String s) { this.comentarioGerencialGeneral = s; }

    // </editor-fold>
    /**
     * ABRE EL POPUP DE EVALUACIÓN. Si no tiene planificaciones → muestra
     * advertencia y abre popup vacío. Si tiene → calcula pesos y abre popup con
     * tabla.
     */
    public void prepararEvaluacion(Plnacciondeta accion) {
        try {
            this.editandoEvaluacionExistente = false;
            
            cargarResumenJerarquiaUnificado(accion);
            cargarPeriodosEvaluacionPermitidos();

            this.listaDetallesEvaluar =
                    busqPep.buscarDetallesPorAccion(accion.getIdaccionpao());
            calcularPesosYPeriodos(this.listaDetallesEvaluar);

            this.listaEvaluacionesDeAccionActual =
                    busqPep.buscarEvaluacionesPorAccion(accion.getIdaccionpao());

            this.evaluacionActual = new Plnaccioneval();
            this.evaluacionActual.setIdaccionpao(accion);
            this.evaluacionActual.setCumplipct(BigDecimal.ZERO);

            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
            }

            if (!listaDetallesEvaluar.isEmpty()) {
                recalcularTotales();
            }

            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(), "popupEvaluacion.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al preparar evaluación: " + e.getMessage(),
                    ValidaDatos.ERROR);
        }
    }

    /**
     * Determina si un trimestral es evaluable según el periodo seleccionado. Si
     * no hay periodo seleccionado, todos son editables.
     */
    public boolean isTrimestralEvaluableEnPeriodo(Plnaccidetplantrim trim) {
        if (trim == null) {
            return false;
        }

        // Sin periodo seleccionado → todos editables
        if (evaluacionActual == null
                || evaluacionActual.getPerieval() == null
                || evaluacionActual.getPerieval().trim().isEmpty()
                || "0".equals(evaluacionActual.getPerieval())) {
            return true;
        }

        String periodo = evaluacionActual.getPerieval().trim();
        int mesLimite;
        switch (periodo) {
            case "ENERO - MARZO":
                mesLimite = 3;
                break;
            case "ENERO - JUNIO":
                mesLimite = 6;
                break;
            case "ENERO - SEPTIEMBRE":
                mesLimite = 9;
                break;
            case "ENERO - DICIEMBRE":
                mesLimite = 12;
                break;
            default:
                return true;
        }

        return trim.getMesfin() > 0 && trim.getMesfin() <= mesLimite;
    }

    /**
     * Convierte el string del periodo en el mes límite correspondiente.
     */
    private int getMesLimiteDePeriodo(String perieval) {
        if (perieval == null) {
            return 0;
        }
        switch (perieval.trim().toUpperCase()) {
            case "ENERO - MARZO":
                return 3;
            case "ENERO - JUNIO":
                return 6;
            case "ENERO - SEPTIEMBRE":
                return 9;
            case "ENERO - DICIEMBRE":
                return 12;
            default:
                return 0;
        }
    }

    /**
     * Retorna texto descriptivo del estado de un trimestral no evaluable.
     */
    public String getEstadoTrimestral(Plnaccidetplantrim trim) {
        if (trim == null) {
            return "";
        }
        if (!puedeEvaluarTrimestral(trim)) {
            return "Fuera de ventana (" + getFechaLimiteEvaluacion(trim) + ")";
        }
        return "Fuera del periodo seleccionado";
    }

    private void calcularPesosYPeriodos(List<Plnaccidetplantrim> trims) {
        if (trims == null || trims.isEmpty()) {
            return;
        }

        int totalMesesAccion = 0;
        for (Plnaccidetplantrim t : trims) {
            if (t.getMesini() > 0 && t.getMesfin() > 0) {
                int meses = t.getMesfin() - t.getMesini() + 1;
                t.setTotalMeses(meses);
                totalMesesAccion += meses;
            }
        }

        for (Plnaccidetplantrim t : trims) {
            if (totalMesesAccion > 0) {
                BigDecimal peso = new BigDecimal(t.getTotalMeses() * 100)
                        .divide(new BigDecimal(totalMesesAccion), 2, RoundingMode.HALF_UP);
                t.setPesoPonderado(peso);
            }
            t.setPeriodoDescripcion(calcularPeriodoTrimestral(t.getMesini(), t.getMesfin()));
            recalcularAporte(t);
        }
    }

    private String calcularPeriodoTrimestral(int mesini, int mesfin) {
        if (mesini >= 1 && mesfin <= 3) {
            return "I TRIM (Ene-Mar)";
        }
        if (mesini >= 4 && mesfin <= 6) {
            return "II TRIM (Abr-Jun)";
        }
        if (mesini >= 7 && mesfin <= 9) {
            return "III TRIM (Jul-Sep)";
        }
        if (mesini >= 10 && mesfin <= 12) {
            return "IV TRIM (Oct-Dic)";
        }
        String[] meses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return meses[mesini] + "-" + meses[mesfin];
    }

    private void recalcularAporte(Plnaccidetplantrim t) {
        if (t == null) {
            return;
        }
        if (t.getCumplipct() != null && t.getPesoPonderado() != null) {
            BigDecimal aporte = t.getCumplipct()
                    .multiply(t.getPesoPonderado())
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            t.setAportePonderado(aporte);
        } else {
            t.setAportePonderado(BigDecimal.ZERO);
        }
    }

    /**
     * Ya no aplica — se eliminó la lógica por mes. Se mantiene por
     * compatibilidad pero retorna null.
     */
    private Integer getUltimoMesHabilitadoParaEvaluar() {
        return null;
    }

    /**
     * Carga los periodos que aún pueden evaluarse. Regla: el periodo puede
     * evaluarse hasta 15 días después de su fecha de cierre.
     *
     * ENERO-MARZO → cierra Mar 31 → límite 15 Abr ENERO-JUNIO → cierra Jun 30 →
     * límite 15 Jul ENERO-SEPTIEMBRE→ cierra Sep 30 → límite 15 Oct
     * ENERO-DICIEMBRE → cierra Dic 31 → límite 15 Ene (año siguiente)
     */
public void cargarPeriodosEvaluacionPermitidos() {
    listaPeriodosEvaluacion.clear();

    Calendar hoy = Calendar.getInstance();
    int mesHoy = hoy.get(Calendar.MONTH) + 1;
    int anioHoy = hoy.get(Calendar.YEAR);

    Calendar limiteP1 = procesarFechaLimite(this.fechaLimiteQ1, anioHoy, false);
    if (hoy.compareTo(limiteP1) <= 0 && mesHoy >= 1) {
        listaPeriodosEvaluacion.add(
            new SelectItem("ENERO - MARZO", "ENERO - MARZO"));
    }

    Calendar limiteP2 = procesarFechaLimite(this.fechaLimiteQ2, anioHoy, false);
    if (hoy.compareTo(limiteP2) <= 0 && mesHoy >= 4) {
        listaPeriodosEvaluacion.add(
            new SelectItem("ENERO - JUNIO", "ENERO - JUNIO"));
    }

    Calendar limiteP3 = procesarFechaLimite(this.fechaLimiteQ3, anioHoy, false);
    if (hoy.compareTo(limiteP3) <= 0 && mesHoy >= 7) {
        listaPeriodosEvaluacion.add(
            new SelectItem("ENERO - SEPTIEMBRE", "ENERO - SEPTIEMBRE"));
    }

    // Q4 pasa al año siguiente
    Calendar limiteP4 = procesarFechaLimite(this.fechaLimiteQ4, anioHoy, true);
    if (hoy.compareTo(limiteP4) <= 0 && mesHoy >= 10) {
        listaPeriodosEvaluacion.add(
            new SelectItem("ENERO - DICIEMBRE", "ENERO - DICIEMBRE"));
    }
}

    public List<SelectItem> getListaPeriodosEvaluacion() {
        return listaPeriodosEvaluacion;
    }

    /**
     * Ya no aplica — se eliminó la lógica por trimestre. Se mantiene por
     * compatibilidad pero retorna null.
     */
    private Integer getUltimoCierreTrimestralHabilitado() {
        return null;
    }

    private void recalcularTotales() {
        if (this.listaDetallesEvaluar == null || this.listaDetallesEvaluar.isEmpty()) {
            if (this.evaluacionActual != null) {
                this.evaluacionActual.setCumplipct(BigDecimal.ZERO);
            }
            return;
        }

        BigDecimal sumaAportes = BigDecimal.ZERO;

        for (Plnaccidetplantrim t : this.listaDetallesEvaluar) {
            recalcularAporte(t);
            if (t.getAportePonderado() != null) {
                sumaAportes = sumaAportes.add(t.getAportePonderado());
            }
        }

        if (sumaAportes.compareTo(new BigDecimal(100)) > 0) {
            sumaAportes = new BigDecimal(100);
        }

        if (this.evaluacionActual != null) {
            this.evaluacionActual.setCumplipct(sumaAportes.setScale(2, RoundingMode.HALF_UP));
        }
    }

    public void limpiarCumplimientosTrimestrales() {
        if (listaDetallesEvaluar != null) {
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                t.setCumplipct(BigDecimal.ZERO);
                t.setAportePonderado(BigDecimal.ZERO);
            }
        }
        if (evaluacionActual != null) {
            evaluacionActual.setCumplipct(BigDecimal.ZERO);
        }
    }

    /**
     * GUARDA LOS CUMPLIMIENTOS TRIMESTRALES. Incluye validación estricta en
     * memoria (Java) para aislar la acción y evitar falsos positivos de
     * duplicidad de periodo.
     */
    public void guardarCumplimientosTrimestrales() {
        try {


            if (listaDetallesEvaluar == null || listaDetallesEvaluar.isEmpty()) {
                showMsg("No hay planificaciones trimestrales para evaluar.", ValidaDatos.WARNING);
                return;
            }

            // Validar longitud de campos ANTES de validar vacíos
            if (!validarLongitudCampo(evaluacionActual.getProyec(), "Proyectado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getRealiz(), "Realizado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getDescriprealiz(), "¿Qué se Realizó?", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getObserv(), "Observaciones", MAX_LONGITUD_TEXTO)) {
                return;
            }

            // ── Validaciones campos obligatorios ──
            if (evaluacionActual.getPerieval() == null
                    || evaluacionActual.getPerieval().trim().isEmpty()
                    || "0".equals(evaluacionActual.getPerieval())) {
                showMsg("Debe seleccionar un Periodo Acumulado.", ValidaDatos.WARNING);
                return;
            }
            evaluacionActual.setFchaeval(new java.util.Date());
            
            if (evaluacionActual.getProyec() == null || evaluacionActual.getProyec().trim().isEmpty()) {
                showMsg("El campo Proyectado es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (evaluacionActual.getRealiz() == null || evaluacionActual.getRealiz().trim().isEmpty()) {
                showMsg("El campo Realizado es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (evaluacionActual.getDescriprealiz() == null || evaluacionActual.getDescriprealiz().trim().isEmpty()) {
                showMsg("El campo '¿Qué se Realizó?' es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            // ── CORRECCIÓN DE LA VALIDACIÓN DUPLICADA EN MEMORIA (JAVA) ──
            if (evaluacionActual.getIdevaluacion() == null) {
                boolean periodoDuplicado = false;

                if (listaEvaluacionesDeAccionActual != null) {
                    for (Plnaccioneval ev : listaEvaluacionesDeAccionActual) {
                        // Verificación blindada: Compara ID de Acción + Nombre de Periodo
                        if (ev.getIdaccionpao() != null
                                && ev.getIdaccionpao().getIdaccionpao().equals(evaluacionActual.getIdaccionpao().getIdaccionpao())
                                && ev.getPerieval() != null
                                && ev.getPerieval().trim().equals(evaluacionActual.getPerieval().trim())) {

                            periodoDuplicado = true;
                            break;
                        }
                    }
                }

                if (periodoDuplicado) {
                    showMsg("Ya existe una evaluación registrada para el periodo '"
                            + evaluacionActual.getPerieval()
                            + "' EN ESTA ACTIVIDAD. No se puede duplicar.", ValidaDatos.WARNING);
                    return;
                }
            }

            // ── Validar al menos un % registrado ──
            boolean alMenosUno = false;
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (t.getCumplipct() != null && t.getCumplipct().compareTo(BigDecimal.ZERO) > 0) {
                    alMenosUno = true;
                    break;
                }
            }
            if (!alMenosUno) {
                showMsg("Debe registrar el % de al menos una planificación.", ValidaDatos.WARNING);
                return;
            }

            // ── Validar rango 0-100 ──
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (t.getCumplipct() != null) {
                    if (t.getCumplipct().compareTo(BigDecimal.ZERO) < 0
                            || t.getCumplipct().compareTo(new BigDecimal(100)) > 0) {
                        showMsg("El % debe estar entre 0 y 100 en todas las planificaciones.", ValidaDatos.WARNING);
                        return;
                    }
                }
            }

            String user = sesion.getSegusuario().getCodusr();

            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (t.getCumplipct() == null) {
                    t.setCumplipct(BigDecimal.ZERO);
                }
                adminPep.guardarCumplimientoTrimestral(t, user);
            }

            recalcularTotales();

            // Limpiar strings
            evaluacionActual.setPerieval(evaluacionActual.getPerieval().trim());
            evaluacionActual.setProyec(evaluacionActual.getProyec().trim());
            evaluacionActual.setRealiz(evaluacionActual.getRealiz().trim());
            evaluacionActual.setDescriprealiz(evaluacionActual.getDescriprealiz().trim());
            if (evaluacionActual.getObserv() != null) {
                evaluacionActual.setObserv(evaluacionActual.getObserv().trim());
            }

            if (evaluacionActual.getIdevaluacion() == null) {
                evaluacionActual.setFchacrea(new java.util.Date());
                evaluacionActual.setUsercrea(user);
            } else {
                evaluacionActual.setFchamod(new java.util.Date());
                evaluacionActual.setUsermod(user);
            }

            adminPep.guardarEvaluacion(evaluacionActual);

            showMsg("Evaluación del periodo '" + evaluacionActual.getPerieval()
                    + "' guardada. Cumplimiento: " + evaluacionActual.getCumplipct() + "%", ValidaDatos.INFO);
           
            if (this.umbralCumplimiento != null && evaluacionActual.getCumplipct().compareTo(this.umbralCumplimiento) < 0) {
                showMsg("Cumplimiento del periodo '" + evaluacionActual.getPerieval()
                        + "' requiere seguimiento por bajo cumplimiento. Mínimo requerido: "
                        + this.umbralCumplimiento + "%", ValidaDatos.INFO);
            }

            cargarAccionesParaEvaluar();
            
            if (this.panelAccionesVisible && this.selIdEstrategia != null) {
                cargarAcciones();
            }

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupEvaluacion.hide();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * VALIDA TODO ANTES DE GUARDAR Y ABRE EL POPUP DE CONFIRMACIÓN.
     */
    public void prepararConfirmacionEvaluacion() {
        try {
            if (listaDetallesEvaluar == null || listaDetallesEvaluar.isEmpty()) {
                showMsg("No hay planificaciones trimestrales para evaluar.", ValidaDatos.WARNING);
                return;
            }
            // ✅ Validar longitud PRIMERO
            if (!validarLongitudCampo(evaluacionActual.getProyec(), "Proyectado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getRealiz(), "Realizado", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getDescriprealiz(), "¿Qué se Realizó?", MAX_LONGITUD_TEXTO)) {
                return;
            }
            if (!validarLongitudCampo(evaluacionActual.getObserv(), "Observaciones", MAX_LONGITUD_TEXTO)) {
                return;
            }

            if (evaluacionActual.getPerieval() == null
                    || evaluacionActual.getPerieval().trim().isEmpty()
                    || "0".equals(evaluacionActual.getPerieval())) {
                showMsg("Debe seleccionar un Periodo Acumulado de Evaluación.", ValidaDatos.WARNING);
                return;
            }
            evaluacionActual.setFchaeval(new java.util.Date());
            
            if (evaluacionActual.getProyec() == null || evaluacionActual.getProyec().trim().isEmpty()) {
                showMsg("El campo Proyectado (Meta) es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (evaluacionActual.getRealiz() == null || evaluacionActual.getRealiz().trim().isEmpty()) {
                showMsg("El campo Realizado es obligatorio.", ValidaDatos.WARNING);
                return;
            }
            if (evaluacionActual.getDescriprealiz() == null || evaluacionActual.getDescriprealiz().trim().isEmpty()) {
                showMsg("El campo '¿Qué se Realizó?' es obligatorio.", ValidaDatos.WARNING);
                return;
            }


            boolean alMenosUnoEvaluado = false;
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (t.getCumplipct() != null && t.getCumplipct().compareTo(BigDecimal.ZERO) > 0) {
                    alMenosUnoEvaluado = true;
                    break;
                }
            }
            if (!alMenosUnoEvaluado) {
                showMsg("Debe registrar el % de cumplimiento de al menos una planificación trimestral.", ValidaDatos.WARNING);
                return;
            }

            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (t.getCumplipct() != null
                        && (t.getCumplipct().compareTo(BigDecimal.ZERO) < 0
                        || t.getCumplipct().compareTo(new BigDecimal(100)) > 0)) {
                    showMsg("El % de cumplimiento debe estar entre 0 y 100 en todas las planificaciones.", ValidaDatos.WARNING);
                    return;
                }
            }

            // ── CORRECCIÓN DE LA VALIDACIÓN DUPLICADA EN MEMORIA (JAVA) ──
            if (evaluacionActual.getIdevaluacion() == null) {
                boolean periodoDuplicado = false;

                if (listaEvaluacionesDeAccionActual != null) {
                    for (Plnaccioneval ev : listaEvaluacionesDeAccionActual) {
                        if (ev.getIdaccionpao() != null
                                && ev.getIdaccionpao().getIdaccionpao().equals(evaluacionActual.getIdaccionpao().getIdaccionpao())
                                && ev.getPerieval() != null
                                && ev.getPerieval().trim().equals(evaluacionActual.getPerieval().trim())) {

                            periodoDuplicado = true;
                            break;
                        }
                    }
                }

                if (periodoDuplicado) {
                    showMsg("Ya existe una evaluación registrada para el periodo '"
                            + evaluacionActual.getPerieval()
                            + "' EN ESTA ACTIVIDAD. No se puede duplicar.", ValidaDatos.WARNING);
                    return;
                }
            }

            // ── Validar si la acción ya está al 100% (bloqueada) ──
            if (evaluacionActual.getIdevaluacion() == null) {
                if (listaEvaluacionesDeAccionActual != null) {
                    for (Plnaccioneval ev : listaEvaluacionesDeAccionActual) {
                        if (ev.getIdaccionpao() != null && ev.getIdaccionpao().getIdaccionpao().equals(evaluacionActual.getIdaccionpao().getIdaccionpao())) {
                            if (ev.getCumplipct() != null && ev.getCumplipct().compareTo(new BigDecimal(100)) >= 0) {
                                showMsg("Esta actividad ya alcanzó el 100% de cumplimiento. No se permiten más evaluaciones.", ValidaDatos.WARNING);
                                return;
                            }
                        }
                    }
                }
            }

            recalcularTotales();

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmarEvaluacion.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al validar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * GUARDA LA EVALUACIÓN CONFIRMADA POR EL USUARIO. Solo se llama desde el
     * popup de confirmación.
     */
    public void confirmarYGuardarEvaluacion() {
        try {
            String user = sesion.getSegusuario().getCodusr();

            // Guardar cada trimestral
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (t.getCumplipct() == null) {
                    t.setCumplipct(BigDecimal.ZERO);
                }
                adminPep.guardarCumplimientoTrimestral(t, user);
            }

            // Recalcular total ponderado final
            recalcularTotales();

            // Limpiar espacios en campos de texto
            if (evaluacionActual.getPerieval() != null) {
                evaluacionActual.setPerieval(evaluacionActual.getPerieval().trim());
            }
            if (evaluacionActual.getProyec() != null) {
                evaluacionActual.setProyec(evaluacionActual.getProyec().trim());
            }
            if (evaluacionActual.getRealiz() != null) {
                evaluacionActual.setRealiz(evaluacionActual.getRealiz().trim());
            }
            if (evaluacionActual.getDescriprealiz() != null) {
                evaluacionActual.setDescriprealiz(
                        evaluacionActual.getDescriprealiz().trim());
            }
            if (evaluacionActual.getObserv() != null) {
                evaluacionActual.setObserv(evaluacionActual.getObserv().trim());
            }

            // Auditoría
            if (evaluacionActual.getIdevaluacion() == null) {
                evaluacionActual.setFchacrea(new java.util.Date());
                evaluacionActual.setUsercrea(user);
            } else {
                evaluacionActual.setFchamod(new java.util.Date());
                evaluacionActual.setUsermod(user);
            }

            adminPep.guardarEvaluacion(evaluacionActual);

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                    "popupConfirmarEvaluacion.hide();");
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                    "popupEvaluacion.hide();");

            showMsg("Evaluación registrada. Cumplimiento: "
                    + evaluacionActual.getCumplipct() + "%", ValidaDatos.INFO);

            cargarAccionesParaEvaluar();

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al guardar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * Listener de cambio de periodo — recalcula qué trimestrales aplican.
     */
    public void cambioPeriodoEvaluacion(AjaxBehaviorEvent event) {
        if (listaDetallesEvaluar != null) {
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (!isTrimestralEvaluableEnPeriodo(t)) {
                    t.setCumplipct(BigDecimal.ZERO);
                    t.setAportePonderado(BigDecimal.ZERO);
                }
            }
            recalcularTotales();
        }
    }

    /**
     * Retorna true si el periodo aún está dentro de su ventana de
     * evaluación/edición. Ventanas: ENERO-MARZO → hasta 15 Abr ENERO-JUNIO →
     * hasta 15 Jul ENERO-SEPTIEMBRE → hasta 15 Oct ENERO-DICIEMBRE → hasta 15
     * Ene año siguiente
     */
public boolean isPeriodoEditable(String perieval) {
    if (perieval == null || perieval.trim().isEmpty()) {
        return false;
    }

    Calendar hoy = Calendar.getInstance();
    Calendar limite = null;
    int anio = hoy.get(Calendar.YEAR);
    String p = perieval.trim().toUpperCase();

    if (p.equals("ENERO - MARZO")) {
        limite = procesarFechaLimite(this.fechaLimiteQ1, anio, false);
    } else if (p.equals("ENERO - JUNIO")) {
        limite = procesarFechaLimite(this.fechaLimiteQ2, anio, false);
    } else if (p.equals("ENERO - SEPTIEMBRE")) {
        limite = procesarFechaLimite(this.fechaLimiteQ3, anio, false);
    } else if (p.equals("ENERO - DICIEMBRE")) {
        limite = procesarFechaLimite(this.fechaLimiteQ4, anio, true);
    } else {
        return false;
    }

    return hoy.compareTo(limite) <= 0;
}

    public boolean isEvaluacionYaRealizada() {
        if (this.evaluacionActual == null || this.evaluacionActual.getIdevaluacion() == null) {
            return false;
        }
        // Si el periodo aún está abierto → permitir edición (no es "solo lectura")
        if (isPeriodoEditable(this.evaluacionActual.getPerieval())) {
            return false;
        }
        return true;
    }

    /**
     * ABRE EL POPUP EN MODO LECTURA CON EL DETALLE DE UNA EVALUACIÓN YA
     * REALIZADA.
     */
    public void verDetalleEvaluacion(Plnaccioneval evalRealizada) {
        try {
            this.evaluacionActual = evalRealizada;
            cargarResumenJerarquiaUnificado(evalRealizada.getIdaccionpao());

            this.listaDetallesEvaluar = busqPep.buscarDetallesPorAccion(
                    evalRealizada.getIdaccionpao().getIdaccionpao());

            calcularPesosYPeriodos(this.listaDetallesEvaluar);
            recalcularTotales();

            this.listaEvaluacionesDeAccionActual = busqPep.buscarEvaluacionesPorAccion(
                    evalRealizada.getIdaccionpao().getIdaccionpao());

            cargarPeriodosEvaluacionPermitidos();

            JavascriptContext.addJavascriptCall(
                    FacesContext.getCurrentInstance(), "popupEvaluacion.show();");

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al cargar detalle: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * Todos los trimestrales son evaluables libremente. La restricción es solo
     * a nivel de PERIODO (combo), no por trimestral individual.
     *
     * @return siempre true — la validación real está en el periodo seleccionado
     */
    public boolean puedeEvaluarTrimestral(Plnaccidetplantrim trim) {
        return true;
    }

    /**
     * Retorna true si HAY AL MENOS UN trimestral editable en la lista actual.
     * Usado en la vista para mostrar/ocultar el botón de actualizar %.
     */
    public boolean isHayTrimestralEditable() {
        if (listaDetallesEvaluar == null || listaDetallesEvaluar.isEmpty()) {
            return false;
        }
        for (Plnaccidetplantrim t : listaDetallesEvaluar) {
            if (puedeEvaluarTrimestral(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ACTUALIZA SOLO LOS % DE LOS TRIMESTRALES QUE AÚN ESTÁN EN PERIODO VÁLIDO.
     * Recalcula el cumplimiento ponderado y actualiza la evaluación más
     * reciente.
     */
    public void actualizarPorcentajesTrimestrales() {
        try {
            if (listaDetallesEvaluar == null || listaDetallesEvaluar.isEmpty()) {
                showMsg("No hay planificaciones para actualizar.", ValidaDatos.WARNING);
                return;
            }

            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (!puedeEvaluarTrimestral(t)) {
                    continue;
                }

                if (t.getCumplipct() != null) {
                    if (t.getCumplipct().compareTo(BigDecimal.ZERO) < 0
                            || t.getCumplipct().compareTo(new BigDecimal(100)) > 0) {
                        showMsg("El % de '"
                                + t.getActiviprogra()
                                + "' debe estar entre 0 y 100.",
                                ValidaDatos.WARNING);
                        return;
                    }
                }
            }

            String user = sesion.getSegusuario().getCodusr();

            int actualizados = 0;
            for (Plnaccidetplantrim t : listaDetallesEvaluar) {
                if (!puedeEvaluarTrimestral(t)) {
                    continue;
                }

                if (t.getCumplipct() == null) {
                    t.setCumplipct(BigDecimal.ZERO);
                }
                adminPep.guardarCumplimientoTrimestral(t, user);
                actualizados++;
            }

            if (actualizados == 0) {
                showMsg("No hay periodos habilitados para actualizar.", ValidaDatos.INFO);
                return;
            }

            recalcularTotales();

            if (listaEvaluacionesDeAccionActual != null
                    && !listaEvaluacionesDeAccionActual.isEmpty()) {

                Plnaccioneval evalMasReciente =
                        listaEvaluacionesDeAccionActual
                        .get(listaEvaluacionesDeAccionActual.size() - 1);

                evalMasReciente.setCumplipct(evaluacionActual.getCumplipct());
                evalMasReciente.setFchamod(new java.util.Date());
                evalMasReciente.setUsermod(user);

                adminPep.guardarEvaluacion(evalMasReciente);
            }

            showMsg("Se actualizaron " + actualizados
                    + " planificación(es). Cumplimiento recalculado: "
                    + evaluacionActual.getCumplipct() + "%",
                    ValidaDatos.INFO);

            if (evaluacionActual.getIdaccionpao() != null) {
                listaEvaluacionesDeAccionActual =
                        busqPep.buscarEvaluacionesPorAccion(
                        evaluacionActual.getIdaccionpao().getIdaccionpao());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al actualizar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * Retorna descripción del periodo límite de evaluación
     */
    public String getFechaLimiteEvaluacion(Plnaccidetplantrim trim) {

        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        int mesFin = trim.getMesfin();
        int mesLimite = (mesFin == 12) ? 1 : mesFin + 1;

        return "hasta el 15 de " + meses[mesLimite];
    }

    public List<Plnaccidetplantrim> getListaDetallesEvaluar() {
        return listaDetallesEvaluar;
    }

    public void setListaDetallesEvaluar(List<Plnaccidetplantrim> v) {
        this.listaDetallesEvaluar = v;
    }
// ✅ Deja solo este método y asegúrate de que sea PUBLIC
    private boolean editandoEvaluacionExistente;

// Getter
    public boolean isEditandoEvaluacionExistente() {
        return editandoEvaluacionExistente;
    }

// Setter
    public void setEditandoEvaluacionExistente(boolean editandoEvaluacionExistente) {
        this.editandoEvaluacionExistente = editandoEvaluacionExistente;
    }

    public void seleccionarEvaluacionParaEditar(AjaxBehaviorEvent event) {
        try {
            Plnaccioneval eval = (Plnaccioneval) event.getComponent().getAttributes().get("evaluacionSeleccionada");

            if (eval != null) {
                if (!isPeriodoEditable(eval.getPerieval())) {
                    showMsg("El periodo '" + eval.getPerieval() + "' está cerrado para edición.", ValidaDatos.WARNING);
                    return;
                }

                this.evaluacionActual = eval;
                this.editandoEvaluacionExistente = true;

                cargarResumenJerarquiaUnificado(eval.getIdaccionpao());

                this.listaDetallesEvaluar = busqPep.buscarDetallesPorAccion(
                        eval.getIdaccionpao().getIdaccionpao());

                calcularPesosYPeriodos(this.listaDetallesEvaluar);
                recalcularTotales();

                FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("popupEvaluacionForm");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recalcularCumplimientoEnVivo(ValueChangeEvent event) {
        try {
            Object newValue = event.getNewValue();
            if (newValue == null) {
                return;
            }

            BigDecimal nuevoPct;
            if (newValue instanceof BigDecimal) {
                nuevoPct = (BigDecimal) newValue;
            } else {
                String str = newValue.toString().trim().replace(",", ".");
                if (str.isEmpty()) {
                    return;
                }
                nuevoPct = new BigDecimal(str);
            }

            if (nuevoPct.compareTo(BigDecimal.ZERO) < 0) {
                nuevoPct = BigDecimal.ZERO;
                                    showMsg("El cumplimiento no puede ser menor a 0.", ValidaDatos.WARNING);
            }
            if (nuevoPct.compareTo(new BigDecimal(100)) > 0) {
                nuevoPct = new BigDecimal(100);
                                    showMsg("El cumplimiento no puede ser mayor a 100.", ValidaDatos.WARNING);
            }
            
            if (nuevoPct.compareTo(BigDecimal.ZERO) < 0 || nuevoPct.compareTo(new BigDecimal(100)) > 0) {
                    showMsg("Por favor, ingrese un % de cumplimiento entre 0 y 100.", ValidaDatos.WARNING);
                    
                    // Refrescamos la tabla para borrar el número malo y regresar al valor anterior
                    FacesContext.getCurrentInstance().getPartialViewContext()
                            .getRenderIds().add("popupEvaluacionForm:panelTablaEvaluacion");
                    return; // Abortamos la ejecución aquí
                }

            UIComponent comp = event.getComponent();
            Object idFila = comp.getAttributes().get("idFila");

            if (idFila != null) {
                BigInteger idDetalle = new BigInteger(idFila.toString());

                for (Plnaccidetplantrim t : this.listaDetallesEvaluar) {
                    if (t.getIddetalleplantrim() != null
                            && t.getIddetalleplantrim().equals(idDetalle)) {

                        if (esTrimestralBloqueado(t)) {
                            showMsg("Esta planificación ya alcanzó el 100% "
                                    + "y no puede modificarse.",
                                    ValidaDatos.WARNING);
                            Plnaccidetplantrim enBd = busqPep
                                    .buscarDetalleTrimPorId(idDetalle);
                            if (enBd != null) {
                                t.setCumplipct(enBd.getCumplipct());
                            }
                            FacesContext.getCurrentInstance()
                                    .getPartialViewContext().getRenderIds()
                                    .add("popupEvaluacionForm:panelTablaEvaluacion");
                            return;
                        }

                        t.setCumplipct(nuevoPct);
                        recalcularAporte(t);
                        break;
                    }
                }
            }

            recalcularTotales();

            FacesContext.getCurrentInstance()
                    .getPartialViewContext().getRenderIds()
                    .add("popupEvaluacionForm:panelTablaEvaluacion");

        } catch (NumberFormatException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna true si la accion trimestral ya fue GUARDADO en BD con 100%.
     * Consulta directamente la BD para tener el valor real.
     */
    public boolean esTrimestralBloqueado(Plnaccidetplantrim trim) {
        if (trim == null || trim.getIddetalleplantrim() == null) {
            return false;
        }
        try {
            Plnaccidetplantrim enBd = busqPep.buscarDetalleTrimPorId(
                    trim.getIddetalleplantrim());

            if (enBd != null
                    && enBd.getCumplipct() != null
                    && enBd.getCumplipct().compareTo(new BigDecimal(100)) >= 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public void imprimirResumenPao() {
        try {
            if (this.paoActual == null || this.paoActual.getIdpao() == null) {
                this.showMsg("Debe cargar los detalles de un PAO para imprimir el resumen.", ValidaDatos.WARNING);
                return;
            }

            Map<String, Object> filtro = new HashMap<String, Object>();
            filtro.put("idPaoSeleccionado", this.paoActual.getIdpao().intValue());

            imprimirReportePln(filtro, "rptPlaneacionPAO");

        } catch (Exception e) {
            e.printStackTrace();
            this.showMsg("Error al preparar la impresión del PAO: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    public void imprimirResumenPep() {
        try {
            if (this.paoActual == null || this.paoActual.getIdpao() == null) {
                this.showMsg("Debe cargar los detalles de un PEP para imprimir el resumen.", ValidaDatos.WARNING);
                return;
            }

            Map<String, Object> filtro = new HashMap<String, Object>();

            filtro.put("idPepSeleccionado", this.paoActual.getIdpep().getIdpep().intValue());

            Integer anio = this.paoActual.getAnio();
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



/**
     * Carga de variables operativas desde la tabla GENCONFIG utilizando Enumeradores.
     * Elimina el uso de parámetros "quemados" en el código.
     */
/**
     * Carga de variables operativas desde la tabla GENCONFIG utilizando parámetros directos.
     * Elimina el uso de parámetros "quemados" en el resto de la lógica.
     */
    private void cargarConfiguracionesBD() {
        try {
            // ── 1. UMBRAL MÍNIMO DE CUMPLIMIENTO (CORPARAM=30, CORRELATIVO=10) ──
            List<Genconfig> cfgsUmbral = busqGen.buscarGenconfigAll(
                    new BigInteger("150"),  // CODMOD del módulo PAO
                    new BigInteger("110"),  // CODPANT
                    new BigInteger("30")    // CORPARAM = Cumplimiento mínimo seguimiento
            );

            if (cfgsUmbral != null) {
                for (Genconfig cfg : cfgsUmbral) {
                    // CORRECCIÓN: Usar .intValue() para poder comparar con el primitivo 10
                    if (cfg.getGenconfigPK() != null 
                            && cfg.getGenconfigPK().getCorrelativo().intValue() == 10
                            && cfg.getValor() != null 
                            && !cfg.getValor().trim().isEmpty()) {
                        this.umbralCumplimiento = new BigDecimal(cfg.getValor().trim());
                    }
                }
            }

            // ── 2. FECHAS LÍMITE DE EVALUACIÓN (CORPARAM=20, CORRELATIVOS 10-40) ──
            List<Genconfig> cfgsFechas = busqGen.buscarGenconfigAll(
                    new BigInteger("150"),  // CODMOD
                    new BigInteger("110"),  // CODPANT
                    new BigInteger("20")    // CORPARAM = Fechas límite evaluaciones
            );

            if (cfgsFechas != null) {
                for (Genconfig cfg : cfgsFechas) {
                    if (cfg.getGenconfigPK() == null 
                            || cfg.getValor() == null 
                            || cfg.getValor().trim().isEmpty()) {
                        continue;
                    }

                    // CORRECCIÓN: Usar .intValue() en lugar de castear con (int)
                    int corr = cfg.getGenconfigPK().getCorrelativo().intValue();
                    String valor = cfg.getValor().trim(); // Formato "15/04", "15/07", etc.

                    switch (corr) {
                        case 10: this.fechaLimiteQ1 = valor; break; // 15/04
                        case 20: this.fechaLimiteQ2 = valor; break; // 15/07
                        case 30: this.fechaLimiteQ3 = valor; break; // 15/10
                        case 40: this.fechaLimiteQ4 = valor; break; // 15/01
                    }
                }
            }

//        // ── 3. FALLBACKS DEFENSIVOS si la BD no tiene datos ──
//        if (this.umbralCumplimiento == null)   this.umbralCumplimiento = new BigDecimal("80");
//        if (this.fechaLimiteQ1 == null)        this.fechaLimiteQ1 = "15/04";
//        if (this.fechaLimiteQ2 == null)        this.fechaLimiteQ2 = "15/07";
//        if (this.fechaLimiteQ3 == null)        this.fechaLimiteQ3 = "15/10";
//        if (this.fechaLimiteQ4 == null)        this.fechaLimiteQ4 = "15/01";

        } catch (Exception e) {
            e.printStackTrace();
            // Si falla la BD, el sistema sigue funcionando con valores seguros
//        this.umbralCumplimiento = new BigDecimal("80");
//        this.fechaLimiteQ1 = "15/04";
//        this.fechaLimiteQ2 = "15/07";
//        this.fechaLimiteQ3 = "15/10";
//        this.fechaLimiteQ4 = "15/01";
        }
    }
    
    /**
     * Convierte la cadena dinámica de la BD (dd/MM) en un Calendar para validación de límites.
     */
private Calendar procesarFechaLimite(String fechaStr, int anioBase, 
                                      boolean esAnioSiguiente) {
    Calendar cal = Calendar.getInstance();
    try {
        String[] partes = fechaStr.split("/");
        int dia = Integer.parseInt(partes[0].trim());
        int mes = Integer.parseInt(partes[1].trim()) - 1; // MONTH inicia en 0
        int anioReal = esAnioSiguiente ? (anioBase + 1) : anioBase;
        cal.set(anioReal, mes, dia, 23, 59, 59);
    } catch (Exception e) {
        // Fallback seguro si el formato en BD es incorrecto
        cal.set(anioBase, Calendar.DECEMBER, 31, 23, 59, 59);
    }
    return cal;
}
    
}