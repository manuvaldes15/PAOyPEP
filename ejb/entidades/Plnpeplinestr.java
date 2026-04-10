package com.coop1.soficoop.pln.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author ENVY360
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnpeplinestr.findAll", query = "SELECT p FROM Plnpeplinestr p"),
    @NamedQuery(name = "Plnpeplinestr.findByAnio", query = "SELECT p FROM Plnpeplinestr p WHERE p.anio = :anio")})
public class Plnpeplinestr implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idestrategia;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(nullable = false, length = 500)
    private String descrip;
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
    private Short anio;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idestrategia", fetch = FetchType.LAZY)
    private List<Plnacciondeta> plnaccionpaoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idestrategia", fetch = FetchType.LAZY)
    private List<Plnaccionsinplan> plnaccinoplanList;
    @JoinColumn(name = "IDINDICADORCUMP", referencedColumnName = "IDINDICADORCUMP", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpepindicumpl idindicadorcump; 

    public Plnpeplinestr() {
    }

    public Plnpeplinestr(BigInteger idestrategia) {
        this.idestrategia = idestrategia;
    }

    public Plnpeplinestr(BigInteger idestrategia, String descrip, String usercrea, Date fchacrea) {
        this.idestrategia = idestrategia;
        this.descrip = descrip;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdestrategia() {
        return idestrategia;
    }

    public void setIdestrategia(BigInteger idestrategia) {
        this.idestrategia = idestrategia;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
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

    public Short getAnio() {
        return anio;
    }

    public void setAnio(Short anio) {
        this.anio = anio;
    }

    @XmlTransient
    @JsonIgnore
    public List<Plnacciondeta> getPlnaccionpaoList() {
        return plnaccionpaoList;
    }

    public void setPlnaccionpaoList(List<Plnacciondeta> plnaccionpaoList) {
        this.plnaccionpaoList = plnaccionpaoList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Plnaccionsinplan> getPlnaccinoplanList() {
        return plnaccinoplanList;
    }

    public void setPlnaccinoplanList(List<Plnaccionsinplan> plnaccinoplanList) {
        this.plnaccinoplanList = plnaccinoplanList;
    }

    public Plnpepindicumpl getIdindicadorcump() {
        return idindicadorcump;
    }

    public void setIdindicadorcump(Plnpepindicumpl idindicadorcump) {
        this.idindicadorcump = idindicadorcump;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idestrategia != null ? idestrategia.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpeplinestr)) {
            return false;
        }
        Plnpeplinestr other = (Plnpeplinestr) object;
        if ((this.idestrategia == null && other.idestrategia != null) || (this.idestrategia != null && !this.idestrategia.equals(other.idestrategia))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnpeplinestr[ idestrategia=" + idestrategia + " ]";
    }
    
}
