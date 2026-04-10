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


/**
 *
 * @author ENVY360
 */
@Entity
@Table(name = "PLNACCIONSINPLAN")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnaccionsinplan.findAll", query = "SELECT p FROM Plnaccionsinplan p"),
    @NamedQuery(name = "Plnaccionsinplan.findByPerieval", query = "SELECT p FROM Plnaccionsinplan p WHERE p.perieval = :perieval")})
public class Plnaccionsinplan implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idaccinoplan;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(nullable = false, length = 500)
    private String descrip;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(nullable = false, length = 500)
    private String proyec;
    @Size(max = 100)
    @Column(length = 100)
    private String perieval;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(nullable = false, length = 500)
    private String realiz;
    @Basic(optional = false)
    @NotNull
    @Column(precision = 10, scale = 4)
    private BigDecimal cumplipct;
    @Size(max = 4000)
    @Column(length = 4000)
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
    @JoinColumn(name = "IDESTRATEGIA", referencedColumnName = "IDESTRATEGIA", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpeplinestr idestrategia;
    @JoinColumn(name = "IDPAO", referencedColumnName = "IDPAO", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpao idpao;

    public Plnaccionsinplan() {
    }

    public Plnaccionsinplan(BigInteger idaccinoplan) {
        this.idaccinoplan = idaccinoplan;
    }

    public Plnaccionsinplan(BigInteger idaccinoplan, String descrip, String proyec, String realiz, BigDecimal cumplipct, String usercrea, Date fchacrea) {
        this.idaccinoplan = idaccinoplan;
        this.descrip = descrip;
        this.proyec = proyec;
        this.realiz = realiz;
        this.cumplipct = cumplipct;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdaccinoplan() {
        return idaccinoplan;
    }

    public void setIdaccinoplan(BigInteger idaccinoplan) {
        this.idaccinoplan = idaccinoplan;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    public String getProyec() {
        return proyec;
    }

    public void setProyec(String proyec) {
        this.proyec = proyec;
    }

    public String getPerieval() {
        return perieval;
    }

    public void setPerieval(String perieval) {
        this.perieval = perieval;
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

    public Plnpeplinestr getIdestrategia() {
        return idestrategia;
    }

    public void setIdestrategia(Plnpeplinestr idestrategia) {
        this.idestrategia = idestrategia;
    }

    public Plnpao getIdpao() {
        return idpao;
    }

    public void setIdpao(Plnpao idpao) {
        this.idpao = idpao;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idaccinoplan != null ? idaccinoplan.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnaccionsinplan)) {
            return false;
        }
        Plnaccionsinplan other = (Plnaccionsinplan) object;
        if ((this.idaccinoplan == null && other.idaccinoplan != null) || (this.idaccinoplan != null && !this.idaccinoplan.equals(other.idaccinoplan))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnaccionsinplan[ idaccinoplan=" + idaccinoplan + " ]";
    }
    
}
