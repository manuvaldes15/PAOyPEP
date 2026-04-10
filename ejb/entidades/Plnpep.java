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
    @NamedQuery(name = "Plnpep.findAll", query = "SELECT p FROM Plnpep p"),
    @NamedQuery(name = "Plnpep.findByAnioini", query = "SELECT p FROM Plnpep p WHERE p.anioini = :anioini AND p.aniofin = :aniofin "),
    @NamedQuery(name = "Plnpep.findByEstado", query = "SELECT p FROM Plnpep p WHERE p.estado = :estado")})
public class Plnpep implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private BigInteger idpep;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private short anioini;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private short aniofin;
    
    private Integer numacta;
    
    private Integer ptoacta;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchacta;
    
    private short estado;
    
    @Size(max = 500)
    @Column(length = 500)
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idpep", fetch = FetchType.LAZY)
    private List<Plnperspectivadeta> plnpepperspList;

    public Plnpep() {
    }

    public Plnpep(BigInteger idpep) {
        this.idpep = idpep;
    }

    public Plnpep(BigInteger idpep, short anioini, short aniofin, String usercrea, Date fchacrea) {
        this.idpep = idpep;
        this.anioini = anioini;
        this.aniofin = aniofin;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdpep() {
        return idpep;
    }

    public void setIdpep(BigInteger idpep) {
        this.idpep = idpep;
    }

    public short getAnioini() {
        return anioini;
    }

    public void setAnioini(short anioini) {
        this.anioini = anioini;
    }

    public short getAniofin() {
        return aniofin;
    }

    public void setAniofin(short aniofin) {
        this.aniofin = aniofin;
    }

    public Integer getNumacta() {
        return numacta; 
    }
    public void setNumacta(Integer numacta) {
        this.numacta = numacta; 
    }
    
    public Integer getPtoacta() {
        return ptoacta;
    }

    public void setPtoacta(Integer ptoacta) {
        this.ptoacta = ptoacta;
    }

    public Date getFchacta() {
        return fchacta;
    }

    public void setFchacta(Date fchacta) {
        this.fchacta = fchacta;
    }

    public short getEstado() {
        return estado;
    }

    public void setEstado(short estado) {
        this.estado = estado;
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
    public List<Plnperspectivadeta> getPlnpepperspList() {
        return plnpepperspList;
    }

    public void setPlnpepperspList(List<Plnperspectivadeta> plnpepperspList) {
        this.plnpepperspList = plnpepperspList;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idpep != null ? idpep.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpep)) {
            return false;
        }
        Plnpep other = (Plnpep) object;
        if ((this.idpep == null && other.idpep != null) || (this.idpep != null && !this.idpep.equals(other.idpep))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnpep[ idpep=" + idpep + " ]";
    }
    
}
