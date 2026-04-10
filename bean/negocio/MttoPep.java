package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.general.excepciones.ValidacionExcepcion;
import com.coop1.banksys.general.negocio.ProcesosGenLocal;
import com.coop1.banksys.general.utilidades.web.ValidaDatos;
import com.coop1.banksys.login.entidades.Segsesion;
import com.coop1.soficoop.pln.dto.ResumenIndicadorDTO;
import com.coop1.soficoop.pln.dto.ResumenJerarquiaDTO;
import com.coop1.soficoop.pln.dto.ResumenObjetivoDTO;
import com.coop1.soficoop.pln.dto.ResumenPepDTO;
import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import com.coop1.soficoop.pln.entidades.Plnaccionseguimiento;
import com.coop1.soficoop.pln.entidades.Plnpao;
import com.coop1.soficoop.pln.entidades.Plnperspectiva;
import com.coop1.soficoop.pln.entidades.Plnpep;
import com.coop1.soficoop.pln.entidades.Plnpepindicumpl;
import com.coop1.soficoop.pln.entidades.Plnpeplinestr;
import com.coop1.soficoop.pln.entidades.Plnpepobjetivo;
import com.coop1.soficoop.pln.entidades.Plnperspectivadeta;
import com.icesoft.faces.context.effects.JavascriptContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.icefaces.ace.model.table.RowStateMap;
import org.icefaces.ace.event.SelectEvent;

/**
 * Mantenimiento del Módulo PEP (Plan Estratégico Participativo).
 *
 * Gestiona la jerarquía completa: PEP → Perspectivas → Objetivos Estratégicos →
 * Indicadores de Cumplimiento → Líneas Estratégicas
 *
 * @author ENVY360
 */
@ManagedBean(name = "mttoPep")
@SessionScoped
public class MttoPep implements Serializable {

    //<editor-fold defaultstate="collapsed" desc="EJBs y Sesión">
    @EJB
    private BusquedasPepLocal busqPep;
    @EJB
    private AdministracionPepLocal adminPep;
//    @EJB
//    private ProcesosGenLocal procGen;
    private ValidaDatos validar = new ValidaDatos(FacesContext.getCurrentInstance());
    //private ValidaDatos valida = new ValidaDatos(FacesContext.getCurrentInstance());
    @ManagedProperty(value = "#{login.sesion}")
    private Segsesion sesion;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Constantes de Control">
    private static final int DEL_PERSPECTIVA = 1;
    private static final int DEL_OBJETIVO = 2;
    private static final int DEL_INDICADOR = 3;
    private static final int DEL_CATALOGO = 4;
    private static final int DEL_PEP = 5;
    private static final int DEL_ESTRATEGIA = 6;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables de Control General">
    private int cambiarTagPanel = 0; // 0 = Búsqueda, 1 = Detalle
    private int tipoEliminacion = 0;
    private String mensajeConfirmacion = "";
    private boolean panelCollapsed = true;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables PEP">
    private Plnpep pep = new Plnpep();
    private List<Plnpep> lstPeps = new ArrayList<Plnpep>();
    private RowStateMap smPep = new RowStateMap();
    private boolean nuevoPep = false;
    // Filtros de búsqueda
    private Short busqAnio;
    private Integer busqEstado;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Perspectivas (Asignación a PEP)">
    private List<Plnperspectivadeta> lstPerspectivasAsignadas = new ArrayList<Plnperspectivadeta>();
    private RowStateMap smPerspectiva = new RowStateMap();
    private List<SelectItem> comboCatPerspectivas = new ArrayList<SelectItem>();
    private String idMaestroSeleccionado;
    private Plnperspectivadeta perspectivaAEliminar;
    private Plnperspectivadeta perspectivaSeleccionada;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Catálogo Maestro de Perspectivas">
    private Plnperspectiva nuevaCategoria = new Plnperspectiva();
    private List<Plnperspectiva> lstCatalogoMaestro = new ArrayList<Plnperspectiva>();
    private RowStateMap smCatalogo = new RowStateMap();
    private boolean editandoCategoria = false;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Objetivos Estratégicos">
    private Plnpepobjetivo nuevoObjetivo = new Plnpepobjetivo();
    private List<Plnpepobjetivo> lstObjetivos = new ArrayList<Plnpepobjetivo>();
    private RowStateMap smObjetivo = new RowStateMap();
    private Plnpepobjetivo objetivoSeleccionado;
    private boolean editandoObjetivo = false;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Indicadores de Cumplimiento">
    private Plnpepindicumpl nuevoIndicador = new Plnpepindicumpl();
    private List<Plnpepindicumpl> lstIndicadores = new ArrayList<Plnpepindicumpl>();
    private RowStateMap smIndicador = new RowStateMap();
    private Plnpepindicumpl indicadorSeleccionado;
    private boolean editandoIndicador = false;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Líneas Estratégicas">
    private Integer filtroAnioEstrategia;
    private Plnpeplinestr nuevaEstrategia = new Plnpeplinestr();
    private List<Plnpeplinestr> lstEstrategias = new ArrayList<Plnpeplinestr>();
    private RowStateMap smEstrategia = new RowStateMap();
    private boolean editandoEstrategia = false;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Variables Vista General y KPIs">
    private List<Plnperspectivadeta> lstArbolCompleto = new ArrayList<Plnperspectivadeta>();
    private List<ResumenPepDTO> lstResumenKpis = new ArrayList<ResumenPepDTO>();
    private ResumenJerarquiaDTO resumenJerarquia = new ResumenJerarquiaDTO();
    // KPIs Totales del PEP
    private int totalGlobalPerspectivas;
    private int totalGlobalObjetivos;
    private int totalGlobalIndicadores;
    private int totalGlobalEstrategias;
    private int totalIniciativas;
    //</editor-fold>

    public MttoPep() {
    }

    @PostConstruct
    public void init() {
        this.lstPeps = new ArrayList<>();
        buscarPeps();
        refrescarComboCatalogo();
    }

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PEP - Búsqueda y Gestión">
    /**
     * BUSCA PLANES ESTRATÉGICOS PARTICIPATIVOS CON FILTROS. Permite filtrar por
     * año de inicio y estado del PEP.
     */
    public void buscarPeps() {
        Map<String, Object> filtro = new HashMap<>();
        this.lstPeps = new ArrayList<>();

        try {
            if (this.busqAnio != null) {
                filtro.put("anioini", this.busqAnio);
            }
            if (this.busqEstado != null) {
                filtro.put("estado", this.busqEstado);
            }
            this.lstPeps = this.busqPep.buscarPeps(filtro);

            if (this.smPep != null) {
                this.smPep.clear();
            }
            if (this.lstPeps.isEmpty()) {
                this.showMsg("No se encontraron Planes Estratégicos con los filtros indicados.", ValidaDatos.INFO);
            }

        } catch (Exception ex) {
            this.showMsg("Error al buscar PEPs", ValidaDatos.ERROR);
            this.showMsgLog(ex, ValidaDatos.ERROR);
        }
    }

    /**
     * LIMPIA LOS FILTROS DE BÚSQUEDA Y REINICIA LA LISTA.
     */
    public void limpiarBusqueda() {
        this.busqAnio = null;
        this.busqEstado = null;
        this.lstPeps = new ArrayList<Plnpep>();
    }

    /**
     * INICIALIZA VARIABLES PARA CREAR UN NUEVO PEP.
     */
    public void nuevoPep() {
        this.pep = new Plnpep();
        this.nuevoPep = true;
        this.smPep.clear();

        this.lstPerspectivasAsignadas = new ArrayList<Plnperspectivadeta>();
        this.perspectivaSeleccionada = null;
        this.lstObjetivos = new ArrayList<Plnpepobjetivo>();
        this.objetivoSeleccionado = null;
        this.lstIndicadores = new ArrayList<Plnpepindicumpl>();
        this.lstArbolCompleto = new ArrayList<Plnperspectivadeta>();
        this.panelCollapsed = true;

        this.idMaestroSeleccionado = null;
    }

    /**
     * CARGA UN PEP SELECCIONADO DESDE LA TABLA PARA SU EDICIÓN. Inicializa
     * todos los datos jerárquicos asociados al PEP.
     */
    public void llenarPepSelect() {
        try {
            if (!this.smPep.isEmpty() && !this.smPep.getSelected().isEmpty()) {

                Plnpep objOriginal = (Plnpep) this.smPep.getSelected().get(0);

                this.pep = new Plnpep();
                this.pep.setIdpep(objOriginal.getIdpep());
                this.pep.setDescrip(objOriginal.getDescrip());
                this.pep.setAnioini(objOriginal.getAnioini());
                this.pep.setAniofin(objOriginal.getAniofin());
                this.pep.setEstado(objOriginal.getEstado());

                this.pep.setUsercrea(objOriginal.getUsercrea());
                this.pep.setFchacrea(objOriginal.getFchacrea());
                this.pep.setUsermod(objOriginal.getUsermod());
                this.pep.setFchamod(objOriginal.getFchamod());

                this.pep.setPtoacta(objOriginal.getPtoacta());
                this.pep.setFchacta(objOriginal.getFchacta());
                this.pep.setNumacta(objOriginal.getNumacta());

                this.lstPerspectivasAsignadas = this.busqPep.buscarPerspectivasPorPep(this.pep.getIdpep());

                this.perspectivaSeleccionada = null;
                this.smPerspectiva.clear();

                this.lstObjetivos = new ArrayList<Plnpepobjetivo>();
                this.objetivoSeleccionado = null;
                this.smObjetivo.clear();

                this.lstIndicadores = new ArrayList<Plnpepindicumpl>();
                this.indicadorSeleccionado = null;
                this.smIndicador.clear();

                this.lstEstrategias = new ArrayList<Plnpeplinestr>();
                this.nuevaEstrategia = new Plnpeplinestr();
                this.smEstrategia.clear();

                this.nuevoPep = false;
                this.lstArbolCompleto = new ArrayList<Plnperspectivadeta>();
                this.panelCollapsed = true;

                cargarVistaGeneral();

                this.setCambiarTagPanel(1);

            } else {
                this.showMsg("Debe seleccionar un registro.", ValidaDatos.WARNING);
            }
        } catch (Exception ex) {
            this.showMsg("Error al cargar datos.", ValidaDatos.ERROR);
            ex.printStackTrace();
        }
    }

    /**
     * GUARDA O ACTUALIZA UN PEP. Realiza validaciones de años, descripción y
     * rango temporal.
     */
    public void guardarPep() {
        List<String> lstMensaje = new ArrayList<>();

        if (pep.getAnioini() == 0 || pep.getAniofin() == 0) {
            lstMensaje.add("Los años son obligatorios.");
        }
        if (pep.getDescrip() == null || pep.getDescrip().trim().isEmpty()) {
            lstMensaje.add("La descripción es obligatoria.");
        }
        if (pep.getAnioini() != 0 && (pep.getAnioini() < 2025 || pep.getAnioini() > 9999)) {
            lstMensaje.add("El Año de Inicio debe estar entre 2025 y 9999.");
        }
        if (pep.getAniofin() != 0 && (pep.getAniofin() < 2025 || pep.getAniofin() > 9999)) {
            lstMensaje.add("El Año Fin debe estar entre 2025 y 9999.");
        }
        if (pep.getAnioini() != 0 && pep.getAniofin() != 0 && pep.getAnioini() >= pep.getAniofin()) {
            lstMensaje.add("El Año de Inicio debe ser menor al Año de Fin.");
        }

        if (!lstMensaje.isEmpty()) {
            for (String msg : lstMensaje) {
                this.showMsg(msg, ValidaDatos.WARNING);
            }
            return;
        }

        try {
            adminPep.guardarPep(this.pep, sesion.getSegusuario().getCodusr());

            this.setCambiarTagPanel(1);
            this.showMsg("Plan Estratégico Participativo guardado correctamente.", ValidaDatos.INFO);

            buscarPeps();
            this.cancelarPep();

        } catch (ValidacionExcepcion ve) {
            procesarValidacionExcepcion(ve);
        } catch (Exception ex) {
            Throwable causa = ex;
            while (causa != null) {
                if (causa instanceof ValidacionExcepcion) {
                    procesarValidacionExcepcion((ValidacionExcepcion) causa);
                    return;
                }
                causa = causa.getCause();
            }
            this.showMsg("Error al guardar: " + ex.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * CANCELA LA EDICIÓN/CREACIÓN DEL PEP Y REGRESA A LA BÚSQUEDA.
     */
    public void cancelarPep() {
        this.pep = new Plnpep();
        this.smPep.clear();
        this.setCambiarTagPanel(0);
    }

    /**
     * VALIDA SI EL PEP ESTÁ BLOQUEADO POR ACTA DE APROBACIÓN.
     *
     * @return true si tiene acta, false en caso contrario.
     */
    public boolean isPepBloqueado() {
        if (this.pep != null && this.pep.getPtoacta() != null && pep.getFchacta() != null) {
            return true;
        }
        return false;
    }

    /**
     * VALIDA SI SE PERMITE ELIMINAR ELEMENTOS DEL PEP. Considera tanto el Acta
     * de Aprobación como la existencia de PAOs asociados.
     *
     * @return true si está bloqueado, false si se permite eliminar.
     */
    public boolean isAccionEliminarBloqueada() {
        if (isPepBloqueado()) {
            return true;
        }
        try {
            if (this.pep != null && this.pep.getIdpep() != null) {
                boolean tienePaos = busqPep.tienePaosAsociados(this.pep.getIdpep());
                if (tienePaos) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * FILTRA LAS LÍNEAS ESTRATÉGICAS POR AÑO. Si filtroAnioEstrategia es null,
     * carga todas.
     */
    public void filtrarEstrategiasPorAnio() {
        try {
            if (indicadorSeleccionado == null) {
                this.lstEstrategias = new ArrayList<Plnpeplinestr>();
                showMsg("Seleccione un Indicador primero.", ValidaDatos.WARNING);
                return;
            }

            List<Plnpeplinestr> todas = busqPep.buscarEstrategiasPorIndicador(
                    indicadorSeleccionado.getIdindicadorcump());

            if (todas == null || todas.isEmpty()) {
                this.lstEstrategias = new ArrayList<Plnpeplinestr>();
                showMsg("No hay líneas estratégicas para este indicador.", ValidaDatos.WARNING);
                return;
            }

            if (filtroAnioEstrategia == null || filtroAnioEstrategia <= 0) {
                this.lstEstrategias = todas;
                showMsg("Mostrando todas las líneas (" + todas.size() + ").", ValidaDatos.INFO);
                return;
            }

            List<Plnpeplinestr> filtradas = new ArrayList<Plnpeplinestr>();
            for (Plnpeplinestr est : todas) {
                if (est.getAnio() != null
                        && est.getAnio().intValue() == filtroAnioEstrategia) {
                    filtradas.add(est);
                }
            }

            this.lstEstrategias = filtradas;

            if (filtradas.isEmpty()) {
                showMsg("No hay líneas estratégicas con año de ejecución: "
                        + filtroAnioEstrategia, ValidaDatos.WARNING);
            } else {
                showMsg("Se encontraron " + filtradas.size()
                        + " línea(s) con año: " + filtroAnioEstrategia, ValidaDatos.INFO);
            }

            cargarVistaGeneral();

        } catch (Exception e) {
            e.printStackTrace();
            showMsg("Error al filtrar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * LIMPIA EL FILTRO DE AÑO Y RECARGA TODAS LAS ESTRATEGIAS.
     */
    public void limpiarFiltroAnioEstrategia() {
        this.filtroAnioEstrategia = null;
        cargarEstrategias();
        cargarVistaGeneral();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PERSPECTIVAS - Catálogo Maestro">
    /**
     * REFRESCA EL COMBO DE PERSPECTIVAS DESDE PERSPECTIVAS. Carga solo las
     * perspectivas activas.
     */
    public void refrescarComboCatalogo() {
        this.comboCatPerspectivas = new ArrayList<SelectItem>();

        try {
            List<Plnperspectiva> maestras = busqPep.buscarTodasLasMaestras();

            if (maestras != null && !maestras.isEmpty()) {
                for (Plnperspectiva m : maestras) {
                    if (m != null && m.getIdmaestro() != null && m.getEstado() == 1) {
                        String etiqueta = m.getNombre();
                        if (etiqueta == null || etiqueta.trim().isEmpty()) {
                            etiqueta = "SIN NOMBRE (ID: " + m.getIdmaestro() + ")";
                        }
                        this.comboCatPerspectivas.add(new SelectItem(m.getIdmaestro().toString(), etiqueta));
                    }
                }
                this.lstCatalogoMaestro = maestras;
            }
        } catch (Exception e) {
            System.out.println("Error cargando catálogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * GUARDA O ACTUALIZA UN REGISTRO EN PERSPECTIVAS.
     */
    public void guardarCategoriaMaestra() {
        try {
            if (nuevaCategoria.getNombre() == null || nuevaCategoria.getNombre().trim().isEmpty()) {
                this.showMsg("Escriba un nombre para la categoría.", ValidaDatos.WARNING);
                return;
            }
            if (nuevaCategoria.getEstado() == null) {
                nuevaCategoria.setEstado((short) 1);
            }

            adminPep.guardarMaestro(nuevaCategoria, this.sesion.getSegusuario().getCodusr());

            this.showMsg(editandoCategoria ? "Categoría actualizada." : "Categoría creada.", ValidaDatos.INFO);

            this.nuevaCategoria = new Plnperspectiva();
            this.editandoCategoria = false;

            this.lstCatalogoMaestro = busqPep.buscarTodasLasMaestras();
            refrescarComboCatalogo();

            if (this.pep != null && this.pep.getIdpep() != null) {
                this.lstPerspectivasAsignadas = this.busqPep.buscarPerspectivasPorPep(this.pep.getIdpep());
            }

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupCatalogo.hide();");

        } catch (Exception e) {
            this.showMsg("Error al guardar categoría: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * ABRE EL POPUP PARA CREAR UNA NUEVA PERSPECTIVA.
     */
    public void abrirPopupCatalogo() {
        this.nuevaCategoria = new Plnperspectiva();
        this.nuevaCategoria.setEstado((short) 1);
        this.editandoCategoria = false;
        refrescarComboCatalogo();
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupCatalogo.show();");
    }

    /**
     * SELECCIONA UNA PERSPECTIVA PARA EDICIÓN.
     */
    public void seleccionarDelCatalogo() {
        if (!smCatalogo.isEmpty() && !smCatalogo.getSelected().isEmpty()) {
            this.nuevaCategoria = (Plnperspectiva) smCatalogo.getSelected().get(0);
            this.editandoCategoria = true;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PERSPECTIVAS - Asignación a PEP">
    /**
     * ASIGNA UNA PERSPECTIVA AL PEP ACTUAL.
     */
    public void agregarPerspectivaAlPlan() {
        try {
            if (idMaestroSeleccionado == null || idMaestroSeleccionado.trim().isEmpty()) {
                this.showMsg("Seleccione una perspectiva del catálogo.", ValidaDatos.WARNING);
                return;
            }

            Plnperspectiva maestro = busqPep.buscarMaestroPorId(new BigInteger(idMaestroSeleccionado));

            if (maestro == null) {
                this.showMsg("Error: No se encontró la categoría maestra.", ValidaDatos.ERROR);
                return;
            }

            for (Plnperspectivadeta asignada : lstPerspectivasAsignadas) {
                if (asignada.getPerspectivaMaestra().getIdmaestro().equals(maestro.getIdmaestro())) {
                    this.showMsg("Esta perspectiva ya está asignada a este plan.", ValidaDatos.WARNING);
                    return;
                }
            }

            Plnperspectivadeta union = new Plnperspectivadeta();
            union.setIdpep(this.pep);
            union.setPerspectivaMaestra(maestro);

            adminPep.guardarPerspectiva(union, this.sesion.getSegusuario().getCodusr());

            this.lstPerspectivasAsignadas = this.busqPep.buscarPerspectivasPorPep(this.pep.getIdpep());
            this.showMsg("Perspectiva asignada correctamente.", ValidaDatos.INFO);

            this.idMaestroSeleccionado = null;

        } catch (Exception e) {
            String msjError = getMensajeRaiz(e);
            this.showMsg(msjError, ValidaDatos.ERROR);
        }
    }

    /**
     * EVENTO DE SELECCIÓN DE PERSPECTIVA EN LA TABLA. Carga los Objetivos
     * Estratégicos asociados y limpia selecciones inferiores.
     */
    public void seleccionarPerspectiva(SelectEvent event) {
        this.perspectivaSeleccionada = (Plnperspectivadeta) event.getObject();

        cargarObjetivos();

        this.objetivoSeleccionado = null;
        this.smObjetivo.clear();

        this.lstIndicadores = new ArrayList<Plnpepindicumpl>();
        this.indicadorSeleccionado = null;
        this.smIndicador.clear();

        this.lstEstrategias = new ArrayList<Plnpeplinestr>();
        this.nuevaEstrategia = new Plnpeplinestr();
        this.smEstrategia.clear();
    }

    /**
     * CARGA LOS OBJETIVOS ESTRATÉGICOS DE LA PERSPECTIVA SELECCIONADA.
     */
    public void cargarObjetivos() {
        try {
            if (perspectivaSeleccionada != null) {
                this.lstObjetivos = busqPep.buscarObjetivosPorPerspectiva(perspectivaSeleccionada.getIdperspectiva());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO OBJETIVOS ESTRATÉGICOS">
    /**
     * ABRE EL POPUP PARA CREAR UN NUEVO OBJETIVO ESTRATÉGICO.
     */
    public void abrirPopupObjetivo() {
        if (perspectivaSeleccionada == null) {
            this.showMsg("Seleccione una perspectiva primero.", ValidaDatos.WARNING);
            return;
        }
        this.nuevoObjetivo = new Plnpepobjetivo();
        this.nuevoObjetivo.setEstado(1);
        this.editandoObjetivo = false;

        cargarResumenJerarquia(1);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupObjetivo.show();");
    }

    /**
     * PREPARA UN OBJETIVO ESTRATÉGICO PARA EDICIÓN.
     */
    public void prepararEdicionObjetivo(Plnpepobjetivo obj) {
        this.nuevoObjetivo = obj;
        this.editandoObjetivo = true;

        cargarResumenJerarquia(1);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupObjetivo.show();");
    }

    /**
     * GUARDA O ACTUALIZA UN OBJETIVO ESTRATÉGICO.
     */
    public void guardarObjetivo() {
        List<String> lstMensaje = new ArrayList<>();

        try {
            if (nuevoObjetivo.getCodigooe() == null || nuevoObjetivo.getCodigooe().trim().isEmpty()) {
                lstMensaje.add("El Código es obligatorio.");
            }
            if (nuevoObjetivo.getDescrip() == null || nuevoObjetivo.getDescrip().trim().isEmpty()) {
                lstMensaje.add("La Descripción es obligatoria.");
            }
            if (perspectivaSeleccionada == null) {
                lstMensaje.add("No hay perspectiva seleccionada.");
            }

            if (!lstMensaje.isEmpty()) {
                for (String msg : lstMensaje) {
                    this.showMsg(msg, ValidaDatos.WARNING);
                }
                return;
            }

            nuevoObjetivo.setIdperspectiva(perspectivaSeleccionada);

            adminPep.guardarObjetivo(nuevoObjetivo, sesion.getSegusuario().getCodusr());

            this.showMsg(editandoObjetivo ? "Objetivo actualizado." : "Objetivo creado.", ValidaDatos.INFO);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupObjetivo.hide();");

            cargarObjetivos();

        } catch (Exception ex) {
            Throwable causa = ex;
            while (causa != null) {
                if (causa instanceof ValidacionExcepcion) {
                    procesarValidacionExcepcion((ValidacionExcepcion) causa);
                    return;
                }
                causa = causa.getCause();
            }
            this.showMsg("Error: " + ex.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * EVENTO DE SELECCIÓN DE OBJETIVO EN LA TABLA. Carga los Indicadores de
     * Cumplimiento asociados.
     */
    public void seleccionarObjetivo(SelectEvent event) {
        this.objetivoSeleccionado = (Plnpepobjetivo) event.getObject();

        cargarIndicadores();

        this.indicadorSeleccionado = null;
        this.smIndicador.clear();

        this.lstEstrategias = new ArrayList<Plnpeplinestr>();
        this.nuevaEstrategia = new Plnpeplinestr();
        this.smEstrategia.clear();
    }

    /**
     * CARGA LOS INDICADORES DE CUMPLIMIENTO DEL OBJETIVO SELECCIONADO.
     */
    public void cargarIndicadores() {
        try {
            if (objetivoSeleccionado != null) {
                this.lstIndicadores = busqPep.buscarIndicadoresPorObjetivo(objetivoSeleccionado.getIdobjetivo());
            } else {
                this.lstIndicadores = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INDICADORES DE CUMPLIMIENTO">
    /**
     * EVENTO DE SELECCIÓN DE INDICADOR EN LA TABLA. Carga las Líneas
     * Estratégicas asociadas.
     */
    public void seleccionarIndicador(SelectEvent event) {
        this.indicadorSeleccionado = (Plnpepindicumpl) event.getObject();

        this.smEstrategia.clear();
        this.nuevaEstrategia = new Plnpeplinestr();

        cargarEstrategias();
    }

    /**
     * ABRE EL POPUP PARA CREAR UN NUEVO INDICADOR DE CUMPLIMIENTO.
     */
    public void abrirPopupIndicador() {
        if (objetivoSeleccionado == null) {
            this.showMsg("Seleccione un objetivo estratégico primero.", ValidaDatos.WARNING);
            return;
        }
        this.nuevoIndicador = new Plnpepindicumpl();
        this.editandoIndicador = false;

        cargarResumenJerarquia(2);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupIndicador.show();");
    }

    /**
     * PREPARA UN INDICADOR PARA EDICIÓN.
     */
    public void prepararEdicionIndicador(Plnpepindicumpl ind) {
        this.nuevoIndicador = ind;
        this.editandoIndicador = true;

        cargarResumenJerarquia(2);
    }

    /**
     * GUARDA O ACTUALIZA UN INDICADOR DE CUMPLIMIENTO.
     */
    public void guardarIndicador() {
        try {
            if (nuevoIndicador.getDescrip() == null || nuevoIndicador.getDescrip().isEmpty()) {
                this.showMsg("La descripción es obligatoria", ValidaDatos.WARNING);
                return;
            }
            nuevoIndicador.setIdobjetivo(objetivoSeleccionado);
            adminPep.guardarIndicador(nuevoIndicador, sesion.getSegusuario().getCodusr());
            this.showMsg("Indicador guardado.", ValidaDatos.INFO);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupIndicador.hide();");
            cargarIndicadores();

        } catch (Exception e) {
            this.showMsg(e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * CARGA LAS LÍNEAS ESTRATÉGICAS DEL INDICADOR SELECCIONADO.
     */
    public void cargarEstrategias() {
        try {
            if (indicadorSeleccionado == null) {
                this.lstEstrategias = new ArrayList<Plnpeplinestr>();
                return;
            }

            List<Plnpeplinestr> todas = busqPep.buscarEstrategiasPorIndicador(
                    indicadorSeleccionado.getIdindicadorcump());

            if (todas == null) {
                this.lstEstrategias = new ArrayList<Plnpeplinestr>();
                return;
            }

            if (filtroAnioEstrategia == null || filtroAnioEstrategia <= 0) {
                this.lstEstrategias = todas;
                return;
            }

            List<Plnpeplinestr> filtradas = new ArrayList<Plnpeplinestr>();
            for (Plnpeplinestr est : todas) {
                if (est.getAnio() != null
                        && est.getAnio().intValue() == filtroAnioEstrategia) {
                    filtradas.add(est);
                }
            }

            this.lstEstrategias = filtradas;

        } catch (Exception e) {
            e.printStackTrace();
            this.lstEstrategias = new ArrayList<Plnpeplinestr>();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO LÍNEAS ESTRATÉGICAS">
    /**
     * ABRE EL POPUP PARA CREAR UNA NUEVA LÍNEA ESTRATÉGICA.
     */
    public void abrirPopupEstrategia() {
        if (indicadorSeleccionado == null) {
            this.showMsg("Seleccione un Indicador primero.", ValidaDatos.WARNING);
            return;
        }
        this.nuevaEstrategia = new Plnpeplinestr();
        this.editandoEstrategia = false;

        cargarResumenJerarquia(3);

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupEstrategia.show();");
    }

    /**
     * PREPARA UNA LÍNEA ESTRATÉGICA PARA EDICIÓN.
     */
    public void prepararEdicionEstrategia(Plnpeplinestr estDeLaTabla) {
        try {
            BigInteger id = estDeLaTabla.getIdestrategia();

            Plnpeplinestr estrategiaFresca = busqPep.buscarEstrategiaPorId(id);

            if (estrategiaFresca != null) {
                this.nuevaEstrategia = estrategiaFresca;
            } else {
                this.nuevaEstrategia = estDeLaTabla;
            }

            this.editandoEstrategia = true;

            cargarResumenJerarquia(2);

        } catch (Exception e) {
            this.showMsg("Error al cargar datos actualizados: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }

    /**
     * GUARDA O ACTUALIZA UNA LÍNEA ESTRATÉGICA.
     */
    public void guardarEstrategia() {
        try {
            if (nuevaEstrategia.getDescrip() == null || nuevaEstrategia.getDescrip().trim().isEmpty()) {
                this.showMsg("La descripción es obligatoria.", ValidaDatos.WARNING);
                return;
            }
            nuevaEstrategia.setIdindicadorcump(indicadorSeleccionado);

            adminPep.guardarEstrategia(nuevaEstrategia, sesion.getSegusuario().getCodusr());

            this.showMsg(editandoEstrategia ? "Estrategia actualizada." : "Estrategia guardada.", ValidaDatos.INFO);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupEstrategia.hide();");
            cargarEstrategias();
//            this.indicadorSeleccionado.setDescrip() = null;

        } catch (Exception e) {
            this.showMsg("Error: " + e.getMessage(), ValidaDatos.ERROR);
        }

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ELIMINACIÓN GENÉRICA">
    /**
     * PREPARA LA ELIMINACIÓN DE CUALQUIER ELEMENTO DE LA JERARQUÍA. Configura
     * el mensaje de confirmación según el tipo de elemento.
     */
    public void prepararEliminarGenerico(Object objetoABorrar, int tipo) {
        if (objetoABorrar == null) {
            this.showMsg("No se ha seleccionado ningún registro para eliminar.", ValidaDatos.WARNING);
            return;
        }

        if (isAccionEliminarBloqueada()) {
            this.showMsg("Este PEP ya cuenta con Planes Operativos (PAO) asociados. No se permite eliminar sus componentes.", ValidaDatos.WARNING);
            return;
        }

        this.tipoEliminacion = tipo;

        switch (tipo) {
            case DEL_PEP:
                this.pep = (Plnpep) objetoABorrar;
                this.mensajeConfirmacion = "¿Está seguro que desea eliminar este Plan Estratégico Participativo completo y todos sus datos asociados? Acción irreversible.";
                break;

            case DEL_PERSPECTIVA:
                this.perspectivaAEliminar = (Plnperspectivadeta) objetoABorrar;
                this.mensajeConfirmacion = "¿Seguro que desea quitar esta Perspectiva de su PEP? Se borrarán sus objetivos asociados.";
                break;

            case DEL_OBJETIVO:
                this.objetivoSeleccionado = (Plnpepobjetivo) objetoABorrar;
                this.mensajeConfirmacion = "¿Seguro que desea eliminar este Objetivo Estratégico? Se borrarán sus indicadores asociados.";
                break;

            case DEL_INDICADOR:
                this.nuevoIndicador = (Plnpepindicumpl) objetoABorrar;
                this.mensajeConfirmacion = "¿Seguro que desea eliminar este Indicador de Cumplimiento? Se borrarán sus líneas estratégicas asociadas.";
                break;

            case DEL_ESTRATEGIA:
                this.nuevaEstrategia = (Plnpeplinestr) objetoABorrar;
                this.mensajeConfirmacion = "¿Seguro que desea eliminar esta Línea Estratégica?";
                break;

            case DEL_CATALOGO:
                this.nuevaCategoria = (Plnperspectiva) objetoABorrar;

                if (this.nuevaCategoria.getIdmaestro() == null || BigInteger.ZERO.equals(this.nuevaCategoria.getIdmaestro())) {
                    this.showMsg("El registro seleccionado no es válido.", ValidaDatos.WARNING);
                    return;
                }

                this.mensajeConfirmacion = "¿Eliminar esta Perspectiva del sistema? Si está en uso, no se podrá borrar.";
                break;
        }

        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.show();");
    }

    /**
     * VALIDA Y ELIMINA UN PEP CON SUS RELACIONES.
     *
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    public boolean eliminarPepHijos(Plnpep pepSeleccionado) {
        try {
            if (pepSeleccionado == null) {
                return false;
            }

            boolean tieneHijos = busqPep.tienePaosAsociados(pepSeleccionado.getIdpep());

            if (tieneHijos) {
                this.showMsg("No se puede eliminar el PEP: Ya existen Planes Operativos asociados.", ValidaDatos.WARNING);
                return false;
            }

            adminPep.eliminarPep(pepSeleccionado);
            this.showMsg("PEP eliminado correctamente.", ValidaDatos.INFO);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("ConstraintViolation") || e.getMessage().contains("foreign key")) {
                this.showMsg("Error de integridad: El registro está siendo usado en otras tablas.", ValidaDatos.ERROR);
            } else {
                this.showMsg("Error al eliminar: " + e.getMessage(), ValidaDatos.ERROR);
            }
        }
        return false;
    }

    /**
     * CONFIRMA Y EJECUTA LA ELIMINACIÓN DEL ELEMENTO PREPARADO. Aplica las
     * validaciones de bloqueo y ejecuta el borrado en cascada correspondiente.
     */
    public void confirmarEliminacion() {
        try {
            if (isPepBloqueado()) {
                this.showMsg("El PEP está Aprobado (Tiene Acta de Aprobación). No se pueden eliminar registros, solo se permite editar estados.", ValidaDatos.WARNING);
                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.hide();");
                return;
            }
            if (isAccionEliminarBloqueada()) {
                this.showMsg("No se pueden eliminar registros porque el PEP tiene PAOs asociados o acta de aprobación.", ValidaDatos.WARNING);
                JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.hide();");
                return;
            }

            switch (tipoEliminacion) {
                case DEL_PEP:
                    if (eliminarPepHijos(this.pep)) {
                        this.showMsg("PEP eliminado.", ValidaDatos.INFO);
                        buscarPeps();
                        cancelarPep();
                    }
                    break;

                case DEL_PERSPECTIVA:
                    if (this.perspectivaAEliminar.getIdperspectiva() != null) {
                        adminPep.eliminarPerspectiva(this.perspectivaAEliminar);
                    }
                    this.lstPerspectivasAsignadas.remove(this.perspectivaAEliminar);
                    this.showMsg("Perspectiva eliminada del PEP.", ValidaDatos.INFO);

                    this.perspectivaSeleccionada = null;
                    this.lstObjetivos = new ArrayList<>();
                    this.objetivoSeleccionado = null;
                    this.lstIndicadores = new ArrayList<>();
                    this.indicadorSeleccionado = null;
                    this.lstEstrategias = new ArrayList<>();
                    this.smEstrategia.clear();

                    cargarObjetivos();
                    break;

                case DEL_OBJETIVO:
                    adminPep.eliminarObjetivo(this.objetivoSeleccionado);
                    this.showMsg("Objetivo estratégico eliminado.", ValidaDatos.INFO);
                    cargarObjetivos();

                    this.objetivoSeleccionado = null;
                    this.lstIndicadores = new ArrayList<>();
                    this.indicadorSeleccionado = null;
                    this.lstEstrategias = new ArrayList<>();
                    this.smEstrategia.clear();

                    cargarIndicadores();
                    break;

                case DEL_INDICADOR:
                    adminPep.eliminarIndicador(this.nuevoIndicador);
                    this.showMsg("Indicador de cumplimiento eliminado.", ValidaDatos.INFO);
                    cargarIndicadores();
                    this.nuevoIndicador = new Plnpepindicumpl();
                    this.lstEstrategias = new ArrayList<>();
                    cargarEstrategias();
                    break;

                case DEL_ESTRATEGIA:
                    adminPep.eliminarEstrategia(this.nuevaEstrategia);
                    this.showMsg("Línea estratégica eliminada.", ValidaDatos.INFO);
                    cargarEstrategias();
                    this.nuevaEstrategia = new Plnpeplinestr();
                    break;

                case DEL_CATALOGO:
                    adminPep.eliminarMaestro(this.nuevaCategoria);
                    this.showMsg("Perspectiva eliminada del catálogo.", ValidaDatos.INFO);
                    this.nuevaCategoria = new Plnperspectiva();
                    this.lstCatalogoMaestro = busqPep.buscarTodasLasMaestras();
                    refrescarComboCatalogo();
                    break;
            }
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.hide();");

        } catch (Exception e) {
            String msjError = getMensajeRaiz(e);
            this.showMsg(msjError, ValidaDatos.ERROR);
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupConfirmacionGeneral.hide();");
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO VISTA GENERAL Y KPIs">
    /**
     * CARGA LA VISTA GENERAL COMPLETA DEL PEP. Genera tanto la estructura
     * jerárquica para la tabla como los KPIs consolidados. Se carga toda la
     * jerarquía: Perspectivas → Objetivos → Indicadores → Estratégicas.
     */
    public void cargarVistaGeneral(javax.faces.event.AjaxBehaviorEvent event) {
        cargarVistaGeneral();
    }

    public void cargarVistaGeneral() {
        try {
            this.lstResumenKpis = new ArrayList<ResumenPepDTO>();
            this.lstArbolCompleto = new ArrayList<Plnperspectivadeta>();
            this.totalGlobalPerspectivas = 0;
            this.totalGlobalObjetivos = 0;
            this.totalGlobalIndicadores = 0;
            this.totalGlobalEstrategias = 0;

            if (this.pep == null || this.pep.getIdpep() == null) {
                return;
            }

            this.lstArbolCompleto = busqPep.buscarPerspectivasPorPep(this.pep.getIdpep());
            this.totalGlobalPerspectivas = this.lstArbolCompleto.size();

            for (Plnperspectivadeta per : this.lstArbolCompleto) {
                ResumenPepDTO dtoPer = new ResumenPepDTO(per.getPerspectivaMaestra().getNombre());

                List<Plnpepobjetivo> objetivos = busqPep.buscarObjetivosPorPerspectiva(
                        per.getIdperspectiva());
                per.setPlnpepobjetivoList(objetivos);

                for (Plnpepobjetivo obj : objetivos) {
                    ResumenObjetivoDTO dtoObj = new ResumenObjetivoDTO(
                            obj.getCodigooe(), obj.getDescrip());

                    List<Plnpepindicumpl> indicadores = busqPep.buscarIndicadoresPorObjetivo(
                            obj.getIdobjetivo());
                    obj.setPlnpepindicumplList(indicadores);

                    for (Plnpepindicumpl ind : indicadores) {
                        ResumenIndicadorDTO dtoInd = new ResumenIndicadorDTO(
                                ind.getCodigoindicador(), ind.getDescrip());

                        List<Plnpeplinestr> todasEstrategias = busqPep.buscarEstrategiasPorIndicador(
                                ind.getIdindicadorcump());

                        List<Plnpeplinestr> estrategiasFiltradas = new ArrayList<Plnpeplinestr>();
                        for (Plnpeplinestr est : todasEstrategias) {

                            boolean pasaFiltro;

                            if (filtroAnioEstrategia == null || filtroAnioEstrategia <= 0) {
                                pasaFiltro = true;
                            } else {
                                pasaFiltro = est.getAnio() != null
                                        && est.getAnio().intValue() == filtroAnioEstrategia;
                            }

                            if (pasaFiltro) {
                                estrategiasFiltradas.add(est);
                            }
                        }

                        ind.setPlnpeplinestrList(estrategiasFiltradas);

                        for (Plnpeplinestr est : estrategiasFiltradas) {
                            dtoInd.getPlnpeplinestrList().add(
                                    new ResumenIndicadorDTO.EstrategiaSimpleDTO(
                                    est.getDescrip(),
                                    est.getAnio() != null ? est.getAnio().intValue() : null));
                        }
                        dtoObj.getPlnpepindicumplList().add(dtoInd);
                    }
                    dtoPer.getPlnpepobjetivoList().add(dtoObj);
                }

                this.totalGlobalObjetivos += dtoPer.getTotalObjetivos();
                this.totalGlobalIndicadores += dtoPer.getTotalIndicadores();
                this.totalGlobalEstrategias += dtoPer.getTotalEstrategias();

                this.lstResumenKpis.add(dtoPer);
            }

        } catch (Exception e) {
            this.showMsg("Error al cargar el resumen: " + e.getMessage(), ValidaDatos.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * CARGA EL RESUMEN JERÁRQUICO PARA LOS POPUPS. Permite mostrar el contexto
     * (Perspectiva → Objetivo → Indicador) al crear/editar elementos.
     *
     * @param nivel 1=Perspectiva, 2=Objetivo, 3=Indicador
     */
    public void cargarResumenJerarquia(int nivel) {
        this.resumenJerarquia = new ResumenJerarquiaDTO();

        try {
            if (nivel >= 1 && this.perspectivaSeleccionada != null) {
                String nom = "S/D";
                if (this.perspectivaSeleccionada.getPerspectivaMaestra() != null) {
                    nom = this.perspectivaSeleccionada.getPerspectivaMaestra().getNombre();
                }
                this.resumenJerarquia.setPerspectiva(nom);
            }

            if (nivel >= 2 && this.objetivoSeleccionado != null) {
                String cod = this.objetivoSeleccionado.getCodigooe();
                String desc = this.objetivoSeleccionado.getDescrip();
                this.resumenJerarquia.setObjetivo((cod != null ? cod : "") + " - " + desc);
            }

            if (nivel >= 3 && this.indicadorSeleccionado != null) {
                String cod = this.indicadorSeleccionado.getCodigoindicador();
                String desc = this.indicadorSeleccionado.getDescrip();
                this.resumenJerarquia.setIndicador((cod != null ? cod : "") + " - " + desc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO APROBACIÓN GERENCIAL">
    /**
     * PREPARA EL POPUP PARA APROBAR UN PEP. Carga los datos del acta (número,
     * punto, fecha).
     */
    public void prepararAprobacionPep(Plnpep p) {
        try {
            if (p == null) {
                this.showMsg("Error: No se seleccionó ningún PEP.", ValidaDatos.ERROR);
                return;
            }
            this.pep = p;
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupAprobarPep.show();");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GUARDA LA APROBACIÓN DEL PEP CON DATOS DEL ACTA.
     */
    public void guardarAprobacionPep() {
        try {
            adminPep.aprobarPepGerencia(this.pep, sesion.getSegusuario().getCodusr());

            this.showMsg("PEP Aprobado Correctamente.", ValidaDatos.INFO);

            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "popupAprobarPep.hide();");
            buscarPeps();

        } catch (Exception e) {
            this.showMsg("Error al aprobar: " + e.getMessage(), ValidaDatos.ERROR);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÉTODOS UTILITARIOS">
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
     * OBTIENE EL MENSAJE RAÍZ DE UNA EXCEPCIÓN.
     */
    private String getMensajeRaiz(Exception e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METOOS GENERAR REPORTES">
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
                    "window.open('" + url + "','RptPEP','location=0,menubar=0,resizable=1,status=0,toolbar=0');");

        } catch (Exception ex) {
            this.showMsg("Error al invocar el motor de reportes", ValidaDatos.WARNING);
            // this.showMsgLog(ex, ValidaDatos.WARNING); // Usa tu método log si existe
        }
    }

    public void imprimirResumenPep() {
        try {
            if (this.pep == null || this.pep.getIdpep() == null) {
                this.showMsg("Debe cargar los detalles de un PEP para imprimir el resumen.", ValidaDatos.WARNING);
                return;
            }

            Map<String, Object> filtro = new HashMap<String, Object>();

            // Se asume que getIdpep() devuelve BigDecimal o BigInteger que nunca es nulo por la validación superior
            filtro.put("idPepSeleccionado", this.pep.getIdpep().intValue());

            // CORRECCIÓN DEL NPE: Verificación de seguridad (Null-Check) para evitar el error de Unboxing
            Integer anio = this.getFiltroAnioEstrategia();
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GETTERS Y SETTERS">
    public int getCambiarTagPanel() {
        return cambiarTagPanel;
    }

    public void setCambiarTagPanel(int cambiarTagPanel) {
        this.cambiarTagPanel = cambiarTagPanel;
    }

    public Plnpep getPep() {
        return pep;
    }

    public void setPep(Plnpep pep) {
        this.pep = pep;
    }

    public List<Plnpep> getLstPeps() {
        return lstPeps;
    }

    public void setLstPeps(List<Plnpep> lstPeps) {
        this.lstPeps = lstPeps;
    }

    public RowStateMap getSmPep() {
        return smPep;
    }

    public void setSmPep(RowStateMap smPep) {
        this.smPep = smPep;
    }

    public Short getBusqAnio() {
        return busqAnio;
    }

    public void setBusqAnio(Short busqAnio) {
        this.busqAnio = busqAnio;
    }

    public Integer getBusqEstado() {
        return busqEstado;
    }

    public void setBusqEstado(Integer busqEstado) {
        this.busqEstado = busqEstado;
    }

    public Plnperspectivadeta getPerspectivaAEliminar() {
        return perspectivaAEliminar;
    }

    public void setPerspectivaAEliminar(Plnperspectivadeta perspectivaAEliminar) {
        this.perspectivaAEliminar = perspectivaAEliminar;
    }

    public List<Plnperspectivadeta> getLstPerspectivasAsignadas() {
        return lstPerspectivasAsignadas;
    }

    public void setLstPerspectivasAsignadas(List<Plnperspectivadeta> l) {
        this.lstPerspectivasAsignadas = l;
    }

    public RowStateMap getSmPerspectiva() {
        return smPerspectiva;
    }

    public void setSmPerspectiva(RowStateMap sm) {
        this.smPerspectiva = sm;
    }

    public List<SelectItem> getComboCatPerspectivas() {
        return comboCatPerspectivas;
    }

    public void setComboCatPerspectivas(List<SelectItem> l) {
        this.comboCatPerspectivas = l;
    }

    public String getIdMaestroSeleccionado() {
        return idMaestroSeleccionado;
    }

    public void setIdMaestroSeleccionado(String s) {
        this.idMaestroSeleccionado = s;
    }

    public Plnperspectiva getNuevaCategoria() {
        return nuevaCategoria;
    }

    public void setNuevaCategoria(Plnperspectiva n) {
        this.nuevaCategoria = n;
    }

    public List<Plnperspectiva> getLstCatalogoMaestro() {
        return lstCatalogoMaestro;
    }

    public void setLstCatalogoMaestro(List<Plnperspectiva> l) {
        this.lstCatalogoMaestro = l;
    }

    public RowStateMap getSmCatalogo() {
        return smCatalogo;
    }

    public void setSmCatalogo(RowStateMap sm) {
        this.smCatalogo = sm;
    }

    public ValidaDatos getValidar() {
        return validar;
    }

    public void setValidar(ValidaDatos validar) {
        this.validar = validar;
    }

    public Segsesion getSesion() {
        return sesion;
    }

    public void setSesion(Segsesion sesion) {
        this.sesion = sesion;
    }

    public boolean isNuevoPep() {
        return nuevoPep;
    }

    public void setNuevoPep(boolean nuevoPep) {
        this.nuevoPep = nuevoPep;
    }

    public List<Plnpepobjetivo> getLstObjetivos() {
        return lstObjetivos;
    }

    public void setLstObjetivos(List<Plnpepobjetivo> lstObjetivos) {
        this.lstObjetivos = lstObjetivos;
    }

    public Plnpepobjetivo getNuevoObjetivo() {
        return nuevoObjetivo;
    }

    public void setNuevoObjetivo(Plnpepobjetivo nuevoObjetivo) {
        this.nuevoObjetivo = nuevoObjetivo;
    }

    public RowStateMap getSmObjetivo() {
        return smObjetivo;
    }

    public void setSmObjetivo(RowStateMap smObjetivo) {
        this.smObjetivo = smObjetivo;
    }

    public Plnperspectivadeta getPerspectivaSeleccionada() {
        return perspectivaSeleccionada;
    }

    public void setPerspectivaSeleccionada(Plnperspectivadeta perspectivaSeleccionada) {
        this.perspectivaSeleccionada = perspectivaSeleccionada;
    }

    public boolean isEditandoObjetivo() {
        return editandoObjetivo;
    }

    public void setEditandoObjetivo(boolean editandoObjetivo) {
        this.editandoObjetivo = editandoObjetivo;
    }

    public List<Plnpepindicumpl> getLstIndicadores() {
        return lstIndicadores;
    }

    public void setLstIndicadores(List<Plnpepindicumpl> lstIndicadores) {
        this.lstIndicadores = lstIndicadores;
    }

    public Plnpepindicumpl getNuevoIndicador() {
        return nuevoIndicador;
    }

    public void setNuevoIndicador(Plnpepindicumpl nuevoIndicador) {
        this.nuevoIndicador = nuevoIndicador;
    }

    public RowStateMap getSmIndicador() {
        return smIndicador;
    }

    public void setSmIndicador(RowStateMap smIndicador) {
        this.smIndicador = smIndicador;
    }

    public Plnpepobjetivo getObjetivoSeleccionado() {
        return objetivoSeleccionado;
    }

    public void setObjetivoSeleccionado(Plnpepobjetivo objetivoSeleccionado) {
        this.objetivoSeleccionado = objetivoSeleccionado;
    }

    public boolean isEditandoIndicador() {
        return editandoIndicador;
    }

    public void setEditandoIndicador(boolean editandoIndicador) {
        this.editandoIndicador = editandoIndicador;
    }

    public Plnpepindicumpl getIndicadorSeleccionado() {
        return indicadorSeleccionado;
    }

    public void setIndicadorSeleccionado(Plnpepindicumpl indicadorSeleccionado) {
        this.indicadorSeleccionado = indicadorSeleccionado;
    }

    public List<Plnpeplinestr> getLstEstrategias() {
        return lstEstrategias;
    }

    public void setLstEstrategias(List<Plnpeplinestr> lstEstrategias) {
        this.lstEstrategias = lstEstrategias;
    }

    public Plnpeplinestr getNuevaEstrategia() {
        return nuevaEstrategia;
    }

    public void setNuevaEstrategia(Plnpeplinestr nuevaEstrategia) {
        this.nuevaEstrategia = nuevaEstrategia;
    }

    public boolean isEditandoEstrategia() {
        return editandoEstrategia;
    }

    public void setEditandoEstrategia(boolean editandoEstrategia) {
        this.editandoEstrategia = editandoEstrategia;
    }

    public Integer getFiltroAnioEstrategia() {
        return filtroAnioEstrategia;
    }

    public void setFiltroAnioEstrategia(Integer filtroAnioEstrategia) {
        this.filtroAnioEstrategia = filtroAnioEstrategia;
    }

    public String getMensajeConfirmacion() {
        return mensajeConfirmacion;
    }

    public boolean isPanelCollapsed() {
        return panelCollapsed;
    }

    public void setPanelCollapsed(boolean panelCollapsed) {
        this.panelCollapsed = panelCollapsed;
    }

    public List<Plnperspectivadeta> getLstArbolCompleto() {
        return lstArbolCompleto;
    }

    public void setLstArbolCompleto(List<Plnperspectivadeta> l) {
        this.lstArbolCompleto = l;
    }

    public RowStateMap getSmEstrategia() {
        return smEstrategia;
    }

    public void setSmEstrategia(RowStateMap smEstrategia) {
        this.smEstrategia = smEstrategia;
    }

    public ResumenJerarquiaDTO getResumenJerarquia() {
        return resumenJerarquia;
    }

    public void setResumenJerarquia(ResumenJerarquiaDTO resumenJerarquia) {
        this.resumenJerarquia = resumenJerarquia;
    }

    public List<ResumenPepDTO> getLstResumenKpis() {
        return lstResumenKpis;
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

    public int getTotalIniciativas() {
        return totalIniciativas;
    }
    //</editor-fold>
}