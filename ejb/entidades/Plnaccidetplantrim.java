package com.coop1.soficoop.pln.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ENVY360
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnaccidetplantrim.findAll", query = "SELECT p FROM Plnaccidetplantrim p"),
    @NamedQuery(name = "Plnaccidetplantrim.findByMesini", query = "SELECT p FROM Plnaccidetplantrim p WHERE p.mesini = :mesini AND p.mesfin = :mesfin "),
    @NamedQuery(name = "Plnaccidetplantrim.findByTrim", query = "SELECT p FROM Plnaccidetplantrim p WHERE p.trimes = :trimes")})
public class Plnaccidetplantrim implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger iddetalleplantrim;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private short mesini;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private short mesfin;
    private Short trimes;
    @Size(max = 1000)
    @Column(length = 1000)
    private String activiprogra;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(nullable = false, length = 20)
    private String usercrea;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchacrea;
    @Size(max = 20)
    @Column(length = 20)
    private String usermod;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchamod;
    @JoinColumn(name = "IDACCIONPAO", referencedColumnName = "IDACCIONPAO", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnacciondeta idaccionpao;
    
    
    
// ✅ Campos persistentes nuevos
@Column(name = "CUMPLIPCT", precision = 5, scale = 2)
private BigDecimal cumplipct = BigDecimal.ZERO;

// ✅ Campos transitorios (solo para mostrar en la vista, no se guardan en BD)
@Transient
private BigDecimal pesoPonderado = BigDecimal.ZERO;  // ej: 20.0%

@Transient
private BigDecimal aportePonderado = BigDecimal.ZERO; // ej: 16.0% (peso × cumplipct / 100)

@Transient
private String periodoDescripcion = ""; // ej: "I TRIM (Ene-Mar)"

@Transient
private int totalMeses = 0; // meses que abarca esta planificación

// ── Getters/Setters ──
public BigDecimal getCumplipct() {
    return cumplipct != null ? cumplipct : BigDecimal.ZERO;
}
public void setCumplipct(BigDecimal v) { this.cumplipct = v; }

public BigDecimal getPesoPonderado() { return pesoPonderado; }
public void setPesoPonderado(BigDecimal v) { this.pesoPonderado = v; }

public BigDecimal getAportePonderado() { return aportePonderado; }
public void setAportePonderado(BigDecimal v) { this.aportePonderado = v; }

public String getPeriodoDescripcion() { return periodoDescripcion; }
public void setPeriodoDescripcion(String v) { this.periodoDescripcion = v; }

public int getTotalMeses() { return totalMeses; }
public void setTotalMeses(int v) { this.totalMeses = v; }

    public Plnaccidetplantrim() {
    }

    public Plnaccidetplantrim(BigInteger iddetalleplantrim) {
        this.iddetalleplantrim = iddetalleplantrim;
    }

    public Plnaccidetplantrim(BigInteger iddetalleplantrim, short mesini, short mesfin, String usercrea, Date fchacrea) {
        this.iddetalleplantrim = iddetalleplantrim;
        this.mesini = mesini;
        this.mesfin = mesfin;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIddetalleplantrim() {
        return iddetalleplantrim;
    }

    public void setIddetalleplantrim(BigInteger iddetalleplantrim) {
        this.iddetalleplantrim = iddetalleplantrim;
    }

    public short getMesini() {
        return mesini;
    }

    public void setMesini(short mesini) {
        this.mesini = mesini;
    }

    public short getMesfin() {
        return mesfin;
    }

    public void setMesfin(short mesfin) {
        this.mesfin = mesfin;
    }

    public Short getTrim() {
        return trimes;
    }

    public void setTrim(Short trim) {
        this.trimes = trim;
    }

    public String getActiviprogra() {
        return activiprogra;
    }

    public void setActiviprogra(String activiprogra) {
        this.activiprogra = activiprogra;
    }

    public String getUsercrea() {
        return usercrea;
    }

    public void setUsercrea(String usercrea) {
        this.usercrea = usercrea;
    }

    public Date getFchacrea() {
        return fchacrea;
    }

    public void setFchacrea(Date fchacrea) {
        this.fchacrea = fchacrea;
    }

    public String getUsermod() {
        return usermod;
    }

    public void setUsermod(String usermod) {
        this.usermod = usermod;
    }

    public Date getFchamod() {
        return fchamod;
    }

    public void setFchamod(Date fchamod) {
        this.fchamod = fchamod;
    }

    public Plnacciondeta getIdaccionpao() {
        return idaccionpao;
    }

    public void setIdaccionpao(Plnacciondeta idaccionpao) {
        this.idaccionpao = idaccionpao;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (iddetalleplantrim != null ? iddetalleplantrim.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnaccidetplantrim)) {
            return false;
        }
        Plnaccidetplantrim other = (Plnaccidetplantrim) object;
        if ((this.iddetalleplantrim == null && other.iddetalleplantrim != null) || (this.iddetalleplantrim != null && !this.iddetalleplantrim.equals(other.iddetalleplantrim))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnaccidetplantrim[ iddetalleplantrim=" + iddetalleplantrim + " ]";
    }
}
