package com.coop1.soficoop.pln.dto;

import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import com.coop1.soficoop.pln.entidades.Plnpepindicumpl;
import com.coop1.soficoop.pln.entidades.Plnpeplinestr;
import com.coop1.soficoop.pln.entidades.Plnpepobjetivo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// CAMBIO: Nombre de clase con Mayúscula inicial (Convención Java)
public class ResumenPaoDTO implements Serializable {

    private String nombreEstrategia;
    private String nombrePerspectiva;
    private String nombreObjetivo;
    private String nombreIndicador;
    private List<Plnacciondeta> acciones;

    // CONSTRUCTOR QUE RECIBE LA ENTIDAD ESTRATEGIA
    public ResumenPaoDTO(Plnpeplinestr estrategia) {
        this.nombreEstrategia = (estrategia.getDescrip() != null) ? estrategia.getDescrip() : "S/D";
        this.acciones = new ArrayList<Plnacciondeta>();

        // Lógica para subir en la jerarquía y llenar los padres
        if (estrategia.getIdindicadorcump() != null) {
            Plnpepindicumpl ind = estrategia.getIdindicadorcump();
            String codInd = (ind.getCodigoindicador() != null) ? ind.getCodigoindicador() : "";
            this.nombreIndicador = codInd + " - " + ind.getDescrip();

            if (ind.getIdobjetivo() != null) {
                Plnpepobjetivo obj = ind.getIdobjetivo();
                String codObj = (obj.getCodigooe() != null) ? obj.getCodigooe() : "";
                this.nombreObjetivo = codObj + " - " + obj.getDescrip();

                if (obj.getIdperspectiva() != null && obj.getIdperspectiva().getPerspectivaMaestra() != null) {
                    this.nombrePerspectiva = obj.getIdperspectiva().getPerspectivaMaestra().getNombre();
                } else {
                    this.nombrePerspectiva = "SIN PERSPECTIVA";
                }
            } else {
                this.nombreObjetivo = "SIN OBJETIVO";
                this.nombrePerspectiva = "---";
            }
        } else {
            this.nombreIndicador = "SIN INDICADOR";
            this.nombreObjetivo = "---";
            this.nombrePerspectiva = "---";
        }
    }

    // Getters y Setters
    public String getNombreEstrategia() { return nombreEstrategia; }
    public void setNombreEstrategia(String nombreEstrategia) { this.nombreEstrategia = nombreEstrategia; }
    public String getNombrePerspectiva() { return nombrePerspectiva; }
    public void setNombrePerspectiva(String nombrePerspectiva) { this.nombrePerspectiva = nombrePerspectiva; }
    public String getNombreObjetivo() { return nombreObjetivo; }
    public void setNombreObjetivo(String nombreObjetivo) { this.nombreObjetivo = nombreObjetivo; }
    public String getNombreIndicador() { return nombreIndicador; }
    public void setNombreIndicador(String nombreIndicador) { this.nombreIndicador = nombreIndicador; }
    public List<Plnacciondeta> getAcciones() { return acciones; }
    public void setAcciones(List<Plnacciondeta> acciones) { this.acciones = acciones; }
}