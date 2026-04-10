package com.coop1.soficoop.pln.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumenIndicadorDTO {

    private String codigoindicador;
    private String descrip;
    private List<EstrategiaSimpleDTO> plnpeplinestrList = new ArrayList<>();

    public ResumenIndicadorDTO(String cod, String desc) {
        this.codigoindicador = cod;
        this.descrip = desc;
    }

    public int getCantidadEstrategias() {
        return plnpeplinestrList.size();
    }

    public int getTotalFilas() {
        return Math.max(1, plnpeplinestrList.size());
    }

    public String getCodigoindicador() {
        return codigoindicador;
    }

    public String getDescrip() {
        return descrip;
    }

    public List<EstrategiaSimpleDTO> getPlnpeplinestrList() {
        return plnpeplinestrList;
    }

    public static class EstrategiaSimpleDTO {

        private String descrip;
        private Integer anio;

        public EstrategiaSimpleDTO(String d, Integer a) {
            this.descrip = d;
            this.anio = a;
        }

        public String getDescrip() {
            return descrip;
        }

        public Integer getAnio() {
            return anio;
        }
    }
}