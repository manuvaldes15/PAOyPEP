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
    @NamedQuery(name = "Plnpepindicumpl.findAll", query = "SELECT p FROM Plnpepindicumpl p"),
    @NamedQuery(name = "Plnpepindicumpl.findByCodigoindicador", query = "SELECT p FROM Plnpepindicumpl p WHERE p.codigoindicador = :codigoindicador")})
public class Plnpepindicumpl implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idindicadorcump;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(nullable = false, length = 10)
    private String codigoindicador;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idindicadorcump", fetch = FetchType.LAZY)
    private List<Plnpaoinic> plnpaoinicList;
    @JoinColumn(name = "IDOBJETIVO", referencedColumnName = "IDOBJETIVO", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpepobjetivo idobjetivo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idindicadorcump", fetch = FetchType.LAZY)
    private List<Plnpeplinestr> plnpeplinestrList;
    
    public Plnpepindicumpl() {
    }

    public Plnpepindicumpl(BigInteger idindicadorcump) {
        this.idindicadorcump = idindicadorcump;
    }

    public Plnpepindicumpl(BigInteger idindicadorcump, String codigoindicador, String descrip, String usercrea, Date fchacrea) {
        this.idindicadorcump = idindicadorcump;
        this.codigoindicador = codigoindicador;
        this.descrip = descrip;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdindicadorcump() {
        return idindicadorcump;
    }

    public void setIdindicadorcump(BigInteger idindicadorcump) {
        this.idindicadorcump = idindicadorcump;
    }

    public String getCodigoindicador() {
        return codigoindicador;
    }

    public void setCodigoindicador(String codigoindicador) {
        this.codigoindicador = codigoindicador;
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

    @XmlTransient
    @JsonIgnore
    public List<Plnpaoinic> getPlnpaoinicList() {
        return plnpaoinicList;
    }

    public void setPlnpaoinicList(List<Plnpaoinic> plnpaoinicList) {
        this.plnpaoinicList = plnpaoinicList;
    }

    public Plnpepobjetivo getIdobjetivo() {
        return idobjetivo;
    }

    public void setIdobjetivo(Plnpepobjetivo idobjetivo) {
        this.idobjetivo = idobjetivo;
    }

    @XmlTransient
    @JsonIgnore
    public List<Plnpeplinestr> getPlnpeplinestrList() {
        return plnpeplinestrList;
    }

    public void setPlnpeplinestrList(List<Plnpeplinestr> plnpeplinestrList) {
        this.plnpeplinestrList = plnpeplinestrList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idindicadorcump != null ? idindicadorcump.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpepindicumpl)) {
            return false;
        }
        Plnpepindicumpl other = (Plnpepindicumpl) object;
        if ((this.idindicadorcump == null && other.idindicadorcump != null) || (this.idindicadorcump != null && !this.idindicadorcump.equals(other.idindicadorcump))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnpepindicumpl[ idindicadorcump=" + idindicadorcump + " ]";
    }
    
}
