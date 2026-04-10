package com.coop1.soficoop.pln.entidades;

import com.coop1.banksys.rhu.entidades.Rhuempleado;
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
    @NamedQuery(name = "Plnpaoinic.findAll", query = "SELECT p FROM Plnpaoinic p")})
public class Plnpaoinic implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idiniciativa;
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
    @JoinColumn(name = "IDINDICADORCUMP", referencedColumnName = "IDINDICADORCUMP", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpepindicumpl idindicadorcump;
    
    @JoinColumn(name = "CODCOORDINADOR", referencedColumnName = "CODEMP")
    @ManyToOne
    private Rhuempleado idcoordinador;

    public Plnpaoinic() {
    }

    public Plnpaoinic(BigInteger idiniciativa) {
        this.idiniciativa = idiniciativa;
    }

    public Plnpaoinic(BigInteger idiniciativa, String descrip, String usercrea, Date fchacrea) {
        this.idiniciativa = idiniciativa;
        this.descrip = descrip;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdiniciativa() {
        return idiniciativa;
    }

    public void setIdiniciativa(BigInteger idiniciativa) {
        this.idiniciativa = idiniciativa;
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

    public Plnpepindicumpl getIdindicadorcump() {
        return idindicadorcump;
    }

    public void setIdindicadorcump(Plnpepindicumpl idindicadorcump) {
        this.idindicadorcump = idindicadorcump;
    }

    public Rhuempleado getIdcoordinador() {
    return idcoordinador;
}

public void setIdcoordinador(Rhuempleado idcoordinador) {
    this.idcoordinador = idcoordinador;
}

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idiniciativa != null ? idiniciativa.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpaoinic)) {
            return false;
        }
        Plnpaoinic other = (Plnpaoinic) object;
        if ((this.idiniciativa == null && other.idiniciativa != null) || (this.idiniciativa != null && !this.idiniciativa.equals(other.idiniciativa))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnpaoinic[ idiniciativa=" + idiniciativa + " ]";
    }
}
