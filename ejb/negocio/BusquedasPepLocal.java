package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.rhu.entidades.Rhuempleado;
import com.coop1.soficoop.pln.dto.ReporteIniciativaDTO;
import com.coop1.soficoop.pln.dto.ResumenJerarquiaDTO;
import com.coop1.soficoop.pln.entidades.Plnaccidetplantrim;
import com.coop1.soficoop.pln.entidades.Plnaccioneval;
import com.coop1.soficoop.pln.entidades.Plnaccionsinplan;
import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import com.coop1.soficoop.pln.entidades.Plnaccionseguimiento;
import com.coop1.soficoop.pln.entidades.Plnpao;
import com.coop1.soficoop.pln.entidades.Plnpaoinic;
import com.coop1.soficoop.pln.entidades.Plnpaoinicdeta;
import com.coop1.soficoop.pln.entidades.Plnpep;
import com.coop1.soficoop.pln.entidades.Plnpepindicumpl;
import com.coop1.soficoop.pln.entidades.Plnpeplinestr;
import com.coop1.soficoop.pln.entidades.Plnpepobjetivo;
import com.coop1.soficoop.pln.entidades.Plnperspectivadeta;
import com.coop1.soficoop.pln.entidades.Plnperspectiva;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 * Interfaz de Búsquedas para el Módulo de Planificación Estratégica. Jerarquía:
 * PEP → Perspectivas → Objetivos Estratégicos → Indicadores de cumplimiento →
 * Líneas Estratégicas
 */
@Local
public interface BusquedasPepLocal {

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PEP">
    List<Plnpep> buscarPeps(Map filtro) throws Exception;

    boolean tienePaosAsociados(BigInteger idPep);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PERSPECTIVAS">
    List<Plnperspectiva> buscarTodasLasMaestras() throws Exception;

    Plnperspectiva buscarMaestroPorId(BigInteger idMaestro) throws Exception;

    List<Plnperspectivadeta> buscarPerspectivasPorPep(BigInteger idPep) throws Exception;

    List<Plnperspectivadeta> buscarPerspectivasPorMaestro(BigInteger idMaestro) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO OBJETIVOS ESTRATÉGICOS (OE)">
    List<Plnpepobjetivo> buscarObjetivosPorPerspectiva(BigInteger idPerspectiva) throws Exception;

    Plnpepobjetivo buscarObjetivoPorId(BigInteger id) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INDICADORES DE CUMPLIMIENTO">
    List<Plnpepindicumpl> buscarIndicadoresPorObjetivo(BigInteger idObjetivo) throws Exception;

    Plnpepindicumpl buscarIndicadorPorId(BigInteger id) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO LÍNEAS ESTRATÉGICAS">
    List<Plnpeplinestr> buscarEstrategiasPorIndicador(BigInteger idIndicador) throws Exception;

    Plnpeplinestr buscarEstrategiaPorId(BigInteger idEstrategia) throws Exception;

    ResumenJerarquiaDTO obtenerResumenPorEstrategia(BigInteger idEstrategia) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INICIATIVAS DE CUMPLIMIENTO">
    List<Plnpaoinic> buscarIniciativasPorIndicador(BigInteger idIndicador) throws Exception;

    List<Plnpaoinic> buscarIniciativasPorPep(BigInteger idPep) throws Exception;

    Plnpaoinic buscarIniciativaPorId(BigInteger idIniciativa) throws Exception;

    List<Plnacciondeta> buscarAccionesPorIniciativa(BigInteger idIniciativa) throws Exception;

    List<Plnacciondeta> buscarAccionesDisponiblesParaIniciativa(BigInteger idIndicador, BigInteger idPao) throws Exception;

    Plnpaoinicdeta buscarVinculo(BigInteger idIniciativa, BigInteger idAccion) throws Exception;
    //</editor-fold>
    
// Reemplaza el método actual buscarIniciativasPorIndicador
List<Plnpaoinic> buscarIniciativasPorIndicadorYCoordinador(
        BigInteger idIndicador, BigInteger codCoordinador);

// Para el reporte ejecutivo — solo las del usuario actual
List<Plnpaoinic> buscarIniciativasPorPepYCoordinador(
        BigInteger idPep, BigInteger codCoordinador);

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PAO">
    List<Plnpao> buscarTodosLosPaos() throws Exception;

    List<Plnpao> buscarPaosPorCoordinador(BigInteger codemp) throws Exception;

    List<Plnpao> buscarPaosPorFiltros(BigInteger codEmp, Integer anio, BigInteger idPep) throws Exception;

    List<Plnpao> buscarPaosPorPep(BigInteger idPep);

    List<Plnpao> buscarPaosDinamico(Map<String, Object> filtros) throws Exception;

    Plnpao buscarPaoPorDepartamento(BigInteger idDepto, Integer anio) throws Exception;

    Plnpao buscarPaoPorJefe(BigInteger codJefe, Integer anio) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ACCIONES DEL PAO">
    List<Plnacciondeta> buscarAccionesPorPaoYEstrategia(BigInteger idPao, BigInteger idEstrategia) throws Exception;

    List<Plnacciondeta> buscarAccionesPorEstrategia(BigInteger idEstrategia) throws Exception;

    List<Plnacciondeta> buscarAccionesPorPao(BigInteger idPao) throws Exception;

    /**
     * Valida si una acción tiene evaluaciones registradas.
     */
    boolean accionTieneEvaluaciones(BigInteger idAccion) throws Exception;

    /**
     * Valida si una acción tiene seguimientos registrados.
     */
    boolean accionTieneSeguimientos(BigInteger idAccion) throws Exception;

    /**
     * Valida si una acción está vinculada a alguna iniciativa.
     */
    boolean accionEstaVinculadaAIniciativa(BigInteger idAccion) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ACCIONES NO PLANIFICADAS">
    List<Plnaccionsinplan> buscarAccionesNoPlanificadas(BigInteger idPao) throws Exception;

    List<Plnaccionsinplan> buscarNoPlanificadasPorEstrategia(BigInteger idPao, BigInteger idEstrategia) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PLAN TRIMESTRAL">
    List<Plnaccidetplantrim> buscarDetallesPorAccion(BigInteger idAccion) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO EVALUACIÓN">
    Plnaccioneval buscarEvaluacionPorAccion(BigInteger idAccion) throws Exception;

    List<Plnaccioneval> buscarEvaluacionesPorAccion(BigInteger idAccion) throws Exception;

    public Plnaccioneval buscarEvaluacionPorAccionYPeriodo(BigInteger idAccion, String periodo) throws Exception;
    
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="MÓDULO SEGUIMIENTOS">
        Plnaccionseguimiento buscarSeguimientoPorAccion
        (BigInteger idAccion
        ) throws Exception;

        List<Plnaccionseguimiento> obtenerHistorialSeguimientos
        (BigInteger idAccion
        );
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO EMPLEADOS">
    Rhuempleado buscarEmpleadoPorId
        (BigInteger codemp
        ) throws Exception;

        List<Rhuempleado> buscarEmpleadosActivos
        () throws Exception;
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="CONSULTAS NATIVAS">
        List<Object[]> ejecutarQueryNativo
        (String sql, Object parametro
        ) throws Exception;

        /**
         * OBTIENE COMPLETO UN PEP DESDE LA VISTA VWDETALLESPEP. Retorna las
         * filas ordenadas por perspectiva, objetivo e indicador.
         *
         * @param idPep Identificador del PEP a consultar.
         * @return Lista de Object[] con las columnas de VWDETALLESPEP.
         */
        List<Object[]> obtenerDetallesPep
        (BigInteger idPep
    

);
    //</editor-fold>
        
        /**
 * BUSCA UN DETALLE TRIMESTRAL POR SU ID DIRECTAMENTE EN BD.
 * Se usa para verificar el valor real guardado sin depender del estado en memoria.
 */
 Plnaccidetplantrim buscarDetalleTrimPorId(BigInteger idDetalle);
 List<ReporteIniciativaDTO> buscarResumenIniciativasPorCoordinador(
        BigInteger codCoordinador);
// Acciones vinculadas a una iniciativa filtradas por PAO activo
List<Plnacciondeta> buscarAccionesVinculadasPorIniciativaYPao(
        BigInteger idIniciativa, BigInteger idPao);
}