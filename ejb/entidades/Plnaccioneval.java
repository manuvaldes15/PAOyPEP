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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.Transient;

/**
 *
 * @author ENVY360
 */
@Entity
@Table(name = "PLNACCIONEVAL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnaccioneval.findAll", query = "SELECT p FROM Plnaccioneval p"),
    @NamedQuery(name = "Plnaccioneval.findByFchaeval", query = "SELECT p FROM Plnaccioneval p WHERE p.fchaeval = :fchaeval"),
    @NamedQuery(name = "Plnaccioneval.findByPerieval", query = "SELECT p FROM Plnaccioneval p WHERE p.perieval = :perieval")})
public class Plnaccioneval implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idevaluacion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchaeval;
    @Size(max = 100)
    @Column(length = 100)
    private String perieval;
@Size(max = 1000)           // ← Cambiar
@Column(length = 1000)      // ← Cambiar
private String proyec;
@Size(max = 1000)           // ← Cambiar
@Column(length = 1000)      // ← Cambiar
private String realiz;
    @Column(precision = 10, scale = 4)
    private BigDecimal cumplipct;
@Size(max = 1000)           // ← Cambiar
@Column(length = 1000)      // ← Cambiar
private String descriprealiz;
@Size(max = 1000)           // ← Cambiar
@Column(length = 1000)      // ← Cambiar
private String observ;
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

    public Plnaccioneval() {
    }

    public Plnaccioneval(BigInteger idevaluacion) {
        this.idevaluacion = idevaluacion;
    }

    public Plnaccioneval(BigInteger idevaluacion, String usercrea, Date fchacrea) {
        this.idevaluacion = idevaluacion;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdevaluacion() {
        return idevaluacion;
    }

    public void setIdevaluacion(BigInteger idevaluacion) {
        this.idevaluacion = idevaluacion;
    }

    public Date getFchaeval() {
        return fchaeval;
    }

    public void setFchaeval(Date fchaeval) {
        this.fchaeval = fchaeval;
    }

    public String getPerieval() {
        return perieval;
    }

    public void setPerieval(String perieval) {
        this.perieval = perieval;
    }

    public String getProyec() {
        return proyec;
    }

    public void setProyec(String proyec) {
        this.proyec = proyec;
    }

    public String getRealiz() {
        return realiz;
    }

    public void setRealiz(String realiz) {
        this.realiz = realiz;
    }

    public BigDecimal getCumplipct() {
        return cumplipct;
    }

    public void setCumplipct(BigDecimal cumplipct) {
        this.cumplipct = cumplipct;
    }

    public String getDescriprealiz() {
        return descriprealiz;
    }

    public void setDescriprealiz(String descriprealiz) {
        this.descriprealiz = descriprealiz;
    }

    public String getObserv() {
        return observ;
    }

    public void setObserv(String observ) {
        this.observ = observ;
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
        hash += (idevaluacion != null ? idevaluacion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnaccioneval)) {
            return false;
        }
        Plnaccioneval other = (Plnaccioneval) object;
        if ((this.idevaluacion == null && other.idevaluacion != null) || (this.idevaluacion != null && !this.idevaluacion.equals(other.idevaluacion))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnaccioneval[ idevaluacion=" + idevaluacion + " ]";
    }
    
}
