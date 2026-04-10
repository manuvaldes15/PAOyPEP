package com.coop1.soficoop.pln.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportePerspectivaDTO implements Serializable {
    private String nombrePerspectiva;
    private List<ReporteIniciativaDTO> listaIniciativas;

    public ReportePerspectivaDTO(String nombre) {
        this.nombrePerspectiva = nombre;
        this.listaIniciativas = new ArrayList<>();
    }

    // Getters y Setters
    public String getNombrePerspectiva() { return nombrePerspectiva; }
    public void setNombrePerspectiva(String nombrePerspectiva) { this.nombrePerspectiva = nombrePerspectiva; }
    public List<ReporteIniciativaDTO> getListaIniciativas() { return listaIniciativas; }
    public void setListaIniciativas(List<ReporteIniciativaDTO> listaIniciativas) { this.listaIniciativas = listaIniciativas; }
}