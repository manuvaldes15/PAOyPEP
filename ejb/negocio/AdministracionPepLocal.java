package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.rhu.entidades.Rhuempleado;
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
import javax.ejb.Local;

/**
 * Interfaz de Administración para el Módulo de Planificación Estratégica.
 * Jerarquía: PEP → Perspectivas → Objetivos Estratégicos → Indicadores de cumplimiento → Líneas Estratégicas
 */
@Local
public interface AdministracionPepLocal {

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PEP">
    void guardarPep(Plnpep pep, String usuario) throws Exception;
    
    void eliminarPep(Plnpep pep) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PERSPECTIVAS">
    void guardarMaestro(Plnperspectiva maestro, String usuario) throws Exception;
    
    void eliminarMaestro(Plnperspectiva maestro) throws Exception;
    
    void guardarPerspectiva(Plnperspectivadeta union, String usuario) throws Exception;
    
    void eliminarPerspectiva(Plnperspectivadeta union) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO OBJETIVOS ESTRATÉGICOS (OE)">
    void guardarObjetivo(Plnpepobjetivo obj, String usuario) throws Exception;
    
    void eliminarObjetivo(Plnpepobjetivo obj) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INDICADORES DE CUMPLIMIENTO">
    void guardarIndicador(Plnpepindicumpl ind, String usuario) throws Exception;
    
    void eliminarIndicador(Plnpepindicumpl ind) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO LÍNEAS ESTRATÉGICAS">
    void guardarEstrategia(Plnpeplinestr est, String usuario) throws Exception;
    
    void eliminarEstrategia(Plnpeplinestr est) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO INICIATIVAS">
    void guardarIniciativa(Plnpaoinic iniciativa) throws Exception;
    
    void eliminarIniciativa(Plnpaoinic iniciativa) throws Exception;
    
    void guardarVinculoIniciativa(Plnpaoinicdeta vinculo) throws Exception;
    
    void eliminarVinculoIniciativa(Plnpaoinicdeta vinculo) throws Exception;
    
    
    //</editor-fold>
    


    //<editor-fold defaultstate="collapsed" desc="MÓDULO PAO">
    void guardarPao(Plnpao pao, String usuario) throws Exception;
    
    void eliminarPao(Plnpao pao) throws Exception;
    
    void limpiarPaoPorCambioPep(BigInteger idPao) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ACCIONES DEL PAO">
    void guardarAccionPao(Plnacciondeta accion, String usuario) throws Exception;
    
    void eliminarAccionPao(Plnacciondeta accion) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO ACCIONES NO PLANIFICADAS">
    void guardarAccionNoPlanificada(Plnaccionsinplan accion) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO PLAN TRIMESTRAL">
    void guardarDetalleTrim(Plnaccidetplantrim detalle) throws Exception;
    
    void eliminarDetalleTrim(Plnaccidetplantrim detalle) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO EVALUACIÓN">
    void guardarEvaluacion(Plnaccioneval evaluacion) throws Exception;
    //</editor-fold>
    
    void guardarCumplimientoTrimestral(Plnaccidetplantrim trim, String usuario) throws Exception;
    

    //<editor-fold defaultstate="collapsed" desc="MÓDULO SEGUIMIENTOS">
    void guardarSeguimiento(Plnaccionseguimiento seguimiento) throws Exception;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="MÓDULO APROBACIÓN GERENCIAL">
    void aprobarPepGerencia(Plnpep pep, String usuario) throws Exception;
    
    void aprobarPresupuestoAccion(Plnacciondeta accion, String usuario) throws Exception;
    
    void cerrarPaoGerencia(Plnpao pao, Integer idActa, String usuario) throws Exception;
    //</editor-fold>
}