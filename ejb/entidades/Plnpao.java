package com.coop1.soficoop.pln.entidades;

import com.coop1.banksys.rhu.entidades.Rhuempleado; 
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


@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnpao.findAll", query = "SELECT p FROM Plnpao p"),
    @NamedQuery(name = "Plnpao.findByAnio", query = "SELECT p FROM Plnpao p WHERE p.anio = :anio")})
public class Plnpao implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "IDPAO", nullable = false, precision = 22)
    private BigInteger idpao;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "NOMBREPAO", nullable = false, length = 255)
    private String nombrepao;

    @Basic(optional = false)
    @NotNull
    @Column(name = "ANIO", nullable = false)
    private Integer anio; 

    @Column(name = "IDACTA")
    private Integer idacta;


    @JoinColumn(name = "IDCOORDINADOR", referencedColumnName = "CODEMP")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Rhuempleado idcoordinador;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "USERCREA", nullable = false, length = 20)
    private String usercrea;

    @Basic(optional = false)
    @NotNull
    @Column(name = "FCHACREA", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchacrea;

    @Size(max = 20)
    @Column(name = "USERMOD", length = 20)
    private String usermod;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FCHAMOD")
    private Date fchamod;
    
    @JoinColumn(name = "IDPEP", referencedColumnName = "IDPEP")
    @ManyToOne
    private Plnpep idpep;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idpao", fetch = FetchType.LAZY)
    private List<Plnacciondeta> plnaccionpaoList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idpao", fetch = FetchType.LAZY)
    private List<Plnaccionsinplan> plnaccinoplanList;

    public Plnpao() {
    }

    public Plnpao(BigInteger idpao) {
        this.idpao = idpao;
    }

    public BigInteger getIdpao() {
        return idpao;
    }

    public void setIdpao(BigInteger idpao) {
        this.idpao = idpao;
    }

    public String getNombrepao() {
        return nombrepao;
    }

    public void setNombrepao(String nombrepao) {
        this.nombrepao = nombrepao;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getIdacta() {
        return idacta;
    }

    public void setIdacta(Integer idacta) {
        this.idacta = idacta;
    }

    public Rhuempleado getIdcoordinador() {
        return idcoordinador;
    }

    public void setIdcoordinador(Rhuempleado idcoordinador) {
        this.idcoordinador = idcoordinador;
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

    public Plnpep getIdpep() { 
        return idpep; 
    }
    
    public void setIdpep(Plnpep idpep) {
        this.idpep = idpep; 
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idpao != null ? idpao.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpao)) {
            return false;
        }
        Plnpao other = (Plnpao) object;
        if ((this.idpao == null && other.idpao != null) || (this.idpao != null && !this.idpao.equals(other.idpao))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnpao[ idpao=" + idpao + " ]";
    }
}