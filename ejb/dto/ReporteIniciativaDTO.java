package com.coop1.soficoop.pln.dto;

import com.coop1.soficoop.pln.entidades.Plnpaoinic;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ReporteIniciativaDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    // ── Campos existentes — NO se tocan ──
    private String nombreIniciativa;
    private String nombrePerspectiva;
    private String nombreObjetivo;
    private String nombreIndicador;
    private List<ReporteAccionDTO> acciones;
    private String periodoEvaluacion;
    // ✅ Campos nuevos para el resumen — no afectan el constructor existente
    private BigInteger idIniciativa;
    private int totalAcciones;
    private int totalPaos;

    // ── Constructor existente — NO se modifica ──
    public ReporteIniciativaDTO(Plnpaoinic inic) {
        this.nombreIniciativa = inic.getDescrip();
        this.acciones = new ArrayList<>();
        this.idIniciativa = inic.getIdiniciativa();

        if (inic.getIdindicadorcump() != null) {
            this.nombreIndicador = inic.getIdindicadorcump()
                    .getCodigoindicador()
                    + " " + inic.getIdindicadorcump().getDescrip();

            if (inic.getIdindicadorcump().getIdobjetivo() != null) {
                this.nombreObjetivo = inic.getIdindicadorcump()
                        .getIdobjetivo().getCodigooe()
                        + " " + inic.getIdindicadorcump()
                        .getIdobjetivo().getDescrip();

                if (inic.getIdindicadorcump().getIdobjetivo()
                        .getIdperspectiva() != null) {
                    this.nombrePerspectiva = inic.getIdindicadorcump()
                            .getIdobjetivo().getIdperspectiva()
                            .getPerspectivaMaestra().getNombre();
                }
            }
        }
    }

    // ── Getters existentes — NO se tocan ──
    public String getNombreIniciativa() {
        return nombreIniciativa;
    }

    public String getNombrePerspectiva() {
        return nombrePerspectiva;
    }

    public String getNombreObjetivo() {
        return nombreObjetivo;
    }

    public String getNombreIndicador() {
        return nombreIndicador;
    }

    public List<ReporteAccionDTO> getAcciones() {
        return acciones;
    }

    public String getPeriodoEvaluacion() {
        return periodoEvaluacion;
    }

    public void setPeriodoEvaluacion(String periodoEvaluacion) {
        this.periodoEvaluacion = periodoEvaluacion;
    }

    // ✅ Getters y Setters nuevos
    public BigInteger getIdIniciativa() {
        return idIniciativa;
    }

    public void setIdIniciativa(BigInteger idIniciativa) {
        this.idIniciativa = idIniciativa;
    }

    public int getTotalAcciones() {
        return totalAcciones;
    }

    public void setTotalAcciones(int totalAcciones) {
        this.totalAcciones = totalAcciones;
    }

    public int getTotalPaos() {
        return totalPaos;
    }

    public void setTotalPaos(int totalPaos) {
        this.totalPaos = totalPaos;
    }
}