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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author ENVY360
 */
@Entity
@Table(name = "PLNPERSPECTIVADETA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnperspectivadeta.findAll", query = "SELECT p FROM Plnperspectivadeta p")})
public class Plnperspectivadeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "IDPERSPECTIVA", nullable = false) 
    private BigInteger idperspectiva;

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

    @Column(name = "FCHAMOD")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchamod;

    @JoinColumn(name = "IDPEP", referencedColumnName = "IDPEP", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpep idpep;

    @JoinColumn(name = "IDMAESTRO", referencedColumnName = "IDMAESTRO", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER) 
    private Plnperspectiva perspectivaMaestra;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idperspectiva", fetch = FetchType.LAZY)
    private List<Plnpepobjetivo> plnpepobjetivoList;

    public Plnperspectivadeta() {
    }

    public Plnperspectivadeta(BigInteger idperspectiva) {
        this.idperspectiva = idperspectiva;
    }

    public BigInteger getIdperspectiva() { return idperspectiva; }
    public void setIdperspectiva(BigInteger idperspectiva) { this.idperspectiva = idperspectiva; }

    public String getUsercrea() { return usercrea; }
    public void setUsercrea(String usercrea) { this.usercrea = usercrea; }

    public Date getFchacrea() { return fchacrea; }
    public void setFchacrea(Date fchacrea) { this.fchacrea = fchacrea; }

    public String getUsermod() { return usermod; }
    public void setUsermod(String usermod) { this.usermod = usermod; }

    public Date getFchamod() { return fchamod; }
    public void setFchamod(Date fchamod) { this.fchamod = fchamod; }

    public Plnpep getIdpep() { return idpep; }
    public void setIdpep(Plnpep idpep) { this.idpep = idpep; }

    public Plnperspectiva getPerspectivaMaestra() { return perspectivaMaestra; }
    public void setPerspectivaMaestra(Plnperspectiva perspectivaMaestra) { this.perspectivaMaestra = perspectivaMaestra; }

    @XmlTransient
    @JsonIgnore
    public List<Plnpepobjetivo> getPlnpepobjetivoList() { return plnpepobjetivoList; }
    public void setPlnpepobjetivoList(List<Plnpepobjetivo> plnpepobjetivoList) { this.plnpepobjetivoList = plnpepobjetivoList; }

    public String getNombre() {
        if (perspectivaMaestra != null) {
            return perspectivaMaestra.getNombre();
        }
        return "";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idperspectiva != null ? idperspectiva.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnperspectivadeta)) return false;
        Plnperspectivadeta other = (Plnperspectivadeta) object;
        if ((this.idperspectiva == null && other.idperspectiva != null) || (this.idperspectiva != null && !this.idperspectiva.equals(other.idperspectiva))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnperspectivadeta[ idperspectiva=" + idperspectiva + " ]";
    }
    
public Integer getTotalFilasPerspectiva() {
    int total = 0;
    if (this.plnpepobjetivoList != null && !this.plnpepobjetivoList.isEmpty()) {
        for (Plnpepobjetivo obj : this.plnpepobjetivoList) {
            total += obj.getTotalFilas(); // Usa el método que ya creaste
        }
    }
    return total > 0 ? total : 1;
}
    
}