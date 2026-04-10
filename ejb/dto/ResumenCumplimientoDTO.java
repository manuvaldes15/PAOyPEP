package com.coop1.soficoop.pln.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ResumenCumplimientoDTO implements Serializable {

    private String nombrePerspectiva;
    private String codigoOE;
    private String descripcionOE;
    private int totalAcciones;
    private int accionesEvaluadas;
    private BigDecimal sumaPorcentajes;
    private List<Object> acciones; 

    public ResumenCumplimientoDTO(String nomPers, String codOE, String descOE) {
        this.nombrePerspectiva = nomPers;
        this.codigoOE = codOE;
        this.descripcionOE = descOE;
        this.totalAcciones = 0;
        this.accionesEvaluadas = 0;
        this.sumaPorcentajes = BigDecimal.ZERO;
        this.acciones = new ArrayList<Object>();
    }

    public void agregarEvaluacion(BigDecimal porcentaje, Object accion) {
        this.totalAcciones++;
        if (accion != null) this.acciones.add(accion);
        
        if (porcentaje != null) {
            this.accionesEvaluadas++;
            this.sumaPorcentajes = this.sumaPorcentajes.add(porcentaje);
        }
    }

    public void agregarAccionSinEvaluar(Object accion) {
        this.totalAcciones++;
        if (accion != null) this.acciones.add(accion);
    }

    public double getPromedio() {
        if (totalAcciones == 0) return 0.0;
        return sumaPorcentajes.divide(new BigDecimal(totalAcciones), 2, RoundingMode.HALF_UP).doubleValue();
    }

    public String getColorEstilo() {
        double p = getPromedio();
        if (p < 50) return "#d9534f"; // Rojo
        if (p < 85) return "#f0ad4e"; // Naranja
        return "#5cb85c";             // Verde
    }

    // Getters
    public List<Object> getAcciones() { return acciones; }
    public String getNombrePerspectiva() { return nombrePerspectiva; }
    public String getCodigoOE() { return codigoOE; }
    public String getDescripcionOE() { return descripcionOE; }
    public int getTotalAcciones() { return totalAcciones; }
}