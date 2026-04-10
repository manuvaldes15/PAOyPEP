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
    @NamedQuery(name = "Plnpepobjetivo.findAll", query = "SELECT p FROM Plnpepobjetivo p"),
    @NamedQuery(name = "Plnpepobjetivo.findByCodigooe", query = "SELECT p FROM Plnpepobjetivo p WHERE p.codigooe = :codigooe"),
    @NamedQuery(name = "Plnpepobjetivo.findByEstado", query = "SELECT p FROM Plnpepobjetivo p WHERE p.estado = :estado")})
public class Plnpepobjetivo implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idobjetivo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(nullable = false, length = 20)
    private String codigooe;
    @Column(length = 1)
    private Integer estado;
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
    @JoinColumn(name = "IDPERSPECTIVA", referencedColumnName = "IDPERSPECTIVA", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnperspectivadeta idperspectiva;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idobjetivo", fetch = FetchType.LAZY)
    private List<Plnpepindicumpl> plnpepindicumplList;

    public Plnpepobjetivo() {
    }

    public Plnpepobjetivo(BigInteger idestrategico) {
        this.idobjetivo = idestrategico;
    }

    public Plnpepobjetivo(BigInteger idestrategico, String codigooe, String descrip, String usercrea, Date fchacrea) {
        this.idobjetivo = idestrategico;
        this.codigooe = codigooe;
        this.descrip = descrip;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdobjetivo() {
        return idobjetivo;
    }

    public void setIdobjetivo(BigInteger idobjetivo) {
        this.idobjetivo = idobjetivo;
    }

    public String getCodigooe() {
        return codigooe;
    }

    public void setCodigooe(String codigooe) {
        this.codigooe = codigooe;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
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

    public Plnperspectivadeta getIdperspectiva() {
        return idperspectiva;
    }

    public void setIdperspectiva(Plnperspectivadeta idperspectiva) {
        this.idperspectiva = idperspectiva;
    }

    @XmlTransient
    @JsonIgnore
    public List<Plnpepindicumpl> getPlnpepindicumplList() {
        return plnpepindicumplList;
    }

    public void setPlnpepindicumplList(List<Plnpepindicumpl> plnpepindicumplList) {
        this.plnpepindicumplList = plnpepindicumplList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idobjetivo != null ? idobjetivo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpepobjetivo)) {
            return false;
        }
        Plnpepobjetivo other = (Plnpepobjetivo) object;
        if ((this.idobjetivo == null && other.idobjetivo != null) || (this.idobjetivo != null && !this.idobjetivo.equals(other.idobjetivo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnpepobjetivo[ idestrategico=" + idobjetivo + " ]";
    }

    public Integer getTotalFilas() {
        int total = 0;
        if (this.plnpepindicumplList != null && !this.plnpepindicumplList.isEmpty()) {
            for (Plnpepindicumpl ind : this.plnpepindicumplList) {
                if (ind.getPlnpeplinestrList() != null && !ind.getPlnpeplinestrList().isEmpty()) {
                    total += ind.getPlnpeplinestrList().size();
                } else {
                    total += 1; 
                }
            }
        }
        return total > 0 ? total : 1;
    }
}
