package com.coop1.soficoop.pln.dto;
import java.util.ArrayList;
import java.util.List;

public class ResumenObjetivoDTO {
    private String codigooe;
    private String descrip;
    private List<ResumenIndicadorDTO> plnpepindicumplList = new ArrayList<>();

    public ResumenObjetivoDTO(String cod, String desc) {
        this.codigooe = cod;
        this.descrip = desc;
    }
    
    public int getCantidadIndicadores() {
        return plnpepindicumplList.size();
    }
    
    public int getCantidadEstrategias() {
        int total = 0;
        for (ResumenIndicadorDTO ind : plnpepindicumplList) {
            total += ind.getCantidadEstrategias();
        }
        return total;
    }
    
    public int getTotalFilas() {
        if (plnpepindicumplList.isEmpty()) return 1;
        int total = 0;
        for (ResumenIndicadorDTO ind : plnpepindicumplList) total += ind.getTotalFilas();
        return total;
    }
    public String getCodigooe() { return codigooe; }
    public String getDescrip() { return descrip; }
    public List<ResumenIndicadorDTO> getPlnpepindicumplList() { return plnpepindicumplList; }
}