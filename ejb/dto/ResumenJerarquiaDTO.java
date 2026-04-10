package com.coop1.soficoop.pln.dto;
import java.io.Serializable;
/**
 *
 * @author ENVY360
 */
public class ResumenJerarquiaDTO implements Serializable {
    private String perspectiva;
    private String objetivo;
    private String indicador;
    private String estrategia;
    private String accion; // Nombre de la acción (Planificada o No)

    public ResumenJerarquiaDTO() {
        this.perspectiva = "Sin Perspectiva vinculada";
        this.objetivo = "Sin OE vinculado";
        this.indicador = "Sin Indicador de Cumplimiento vinculado";
        this.estrategia = "Sin Linea Estrategica vinculada";
        this.accion = "";
    }

    // Getters y Setters
    public String getPerspectiva() { 
        return perspectiva; 
    }
    public void setPerspectiva(String perspectiva) {
        this.perspectiva = perspectiva; 
    }
    public String getObjetivo() {
        return objetivo; 
    }
    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo; 
    }
    public String getIndicador() { 
        return indicador; 
    }
    public void setIndicador(String indicador) { 
        this.indicador = indicador; 
    }
    public String getEstrategia() {
        return estrategia; 
    }
    public void setEstrategia(String estrategia) { 
        this.estrategia = estrategia; 
    }
    public String getAccion() { 
        return accion; 
    }
    public void setAccion(String accion) { 
        this.accion = accion; 
    }
}
