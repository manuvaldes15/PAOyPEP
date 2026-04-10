package com.coop1.soficoop.pln.dto;

import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteAccionDTO implements Serializable {

    private String descripcionAccion;
    private String descrip;
    private String estrategia;
    private BigDecimal presupropues;
    private BigDecimal presupuestoPropuesto;
    private BigDecimal porcentajeCumplimiento;
    private String medioverifi;
    private String medioVerificacion;

    // Subfilas: cada planificación trimestral registrada
    private List<PlanificacionDTO> planificaciones = new ArrayList<PlanificacionDTO>();

    // Mantenido por compatibilidad
    private Map<String, String> cronograma = new HashMap<String, String>();

    // =========================================================
    // CLASE INTERNA — representa una fila de planificación
    // =========================================================
    public static class PlanificacionDTO implements Serializable {

        private String actividad;
        private Map<String, String> meses = new HashMap<String, String>();

        public PlanificacionDTO(String actividad, int mesini, int mesfin) {
            this.actividad = (actividad != null) ? actividad : "";

            // Inicializar todos vacíos
            for (int i = 1; i <= 12; i++) {
                meses.put(String.valueOf(i), "");
            }

            // Marcar el rango activo — compatible Java 6
            if (mesini >= 1 && mesfin <= 12 && mesini <= mesfin) {
                for (int m = mesini; m <= mesfin; m++) {
                    meses.put(String.valueOf(m), "X");
                }
            }
        }

        public String getActividad()                { return actividad; }
        public void setActividad(String v)          { this.actividad = v; }
        public Map<String, String> getMeses()       { return meses; }
        public void setMeses(Map<String, String> v) { this.meses = v; }
    }

    public ReporteAccionDTO(Plnacciondeta accion) {
        this.descripcionAccion    = accion.getDescrip();
        this.descrip              = accion.getDescrip();
        this.estrategia           = (accion.getIdestrategia() != null)
                                        ? accion.getIdestrategia().getDescrip() : "";
        this.presupropues         = accion.getPresupropues();
        this.presupuestoPropuesto = accion.getPresupropues();
        this.medioverifi          = accion.getMedioverifi();
        this.medioVerificacion    = accion.getMedioverifi();

        for (int i = 1; i <= 12; i++) {
            cronograma.put(String.valueOf(i), "");
        }
    }


    public void agregarPorRangoMeses(int mesini, int mesfin, String tarea) {
        if (tarea == null) {
            tarea = "";
        }
        if (mesini < 1 || mesfin > 12 || mesini > mesfin) {
            return;
        }
        planificaciones.add(new PlanificacionDTO(tarea, mesini, mesfin));
    }


    public void agregarTareaAlTrimestre(int trimestre, String tarea) {
        if (tarea == null) {
            tarea = "";
        }
        int mesini = 1, mesfin = 3;
        if (trimestre == 2)      { mesini = 4;  mesfin = 6; }
        else if (trimestre == 3) { mesini = 7;  mesfin = 9; }
        else if (trimestre == 4) { mesini = 10; mesfin = 12; }
        planificaciones.add(new PlanificacionDTO(tarea, mesini, mesfin));
    }

    private String periodoEvaluacion = "";

public String getPeriodoEvaluacion() {
    return periodoEvaluacion != null ? periodoEvaluacion : "Sin evaluar";
}
public void setPeriodoEvaluacion(String v) {
    this.periodoEvaluacion = v;
}
    
    
    // --- Getters / Setters ---
    public String getDescripcionAccion()                            { return descripcionAccion; }
    public void setDescripcionAccion(String v)                      { this.descripcionAccion = v; }
    public String getDescrip()                                      { return descrip; }
    public void setDescrip(String v)                                { this.descrip = v; }
    public String getEstrategia()                                   { return estrategia; }
    public void setEstrategia(String v)                             { this.estrategia = v; }
    public BigDecimal getPresupropues()                             { return presupropues; }
    public void setPresupropues(BigDecimal v)                       { this.presupropues = v; }
    public BigDecimal getPresupuestoPropuesto()                     { return presupuestoPropuesto; }
    public void setPresupuestoPropuesto(BigDecimal v)               { this.presupuestoPropuesto = v; }
    public String getMedioverifi()                                  { return medioverifi; }
    public void setMedioverifi(String v)                            { this.medioverifi = v; }
    public String getMedioVerificacion()                            { return medioVerificacion; }
    public void setMedioVerificacion(String v)                      { this.medioVerificacion = v; }
    public Map<String, String> getCronograma()                      { return cronograma; }
    public void setCronograma(Map<String, String> v)                { this.cronograma = v; }
    public List<PlanificacionDTO> getPlanificaciones()              { return planificaciones; }
    public void setPlanificaciones(List<PlanificacionDTO> v)        { this.planificaciones = v; }

    public BigDecimal getPorcentajeCumplimiento() {
        return porcentajeCumplimiento != null ? porcentajeCumplimiento : BigDecimal.ZERO;
    }
    public void setPorcentajeCumplimiento(BigDecimal p) { this.porcentajeCumplimiento = p; }
}