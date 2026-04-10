package com.coop1.soficoop.pln.dto;
import java.util.ArrayList;
import java.util.List;
import com.coop1.soficoop.pln.entidades.Plnperspectiva; // Solo para el dummy

public class ResumenPepDTO {
    private String nombrePerspectiva;
    private List<ResumenObjetivoDTO> plnpepobjetivoList = new ArrayList<>();

    public ResumenPepDTO(String nombre) { this.nombrePerspectiva = nombre; }

    public int getTotalFilasPerspectiva() {
        if (plnpepobjetivoList.isEmpty()) return 1;
        int total = 0;
        for (ResumenObjetivoDTO obj : plnpepobjetivoList) total += obj.getTotalFilas();
        return total;
    }
    
    // KPI 1: Cantidad de Objetivos
    public int getTotalObjetivos() {
        return plnpepobjetivoList.size();
    }

    // KPI 2: Cantidad de Indicadores (Suma de todos los objetivos)
    public int getTotalIndicadores() {
        int total = 0;
        for (ResumenObjetivoDTO obj : plnpepobjetivoList) {
            total += obj.getCantidadIndicadores();
        }
        return total;
    }

    // KPI 3: Cantidad de Estrategias (Suma total de la jerarquía)
    public int getTotalEstrategias() {
        int total = 0;
        for (ResumenObjetivoDTO obj : plnpepobjetivoList) {
            total += obj.getCantidadEstrategias();
        }
        return total;
    }
    
    
    // Dummy para compatibilidad con XHTML
    public Plnperspectiva getPerspectivaMaestra() {
        Plnperspectiva p = new Plnperspectiva();
        p.setNombre(this.nombrePerspectiva);
        return p;
    }
    public List<ResumenObjetivoDTO> getPlnpepobjetivoList() { return plnpepobjetivoList; }
}