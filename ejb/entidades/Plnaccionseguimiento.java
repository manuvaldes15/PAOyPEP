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
@Table(name = "PLNACCIONSEGUIMIENTO")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnaccionseguimiento.findAll", query = "SELECT p FROM Plnaccionseguimiento p"),
    @NamedQuery(name = "Plnaccionseguimiento.findByIdseguimiento", query = "SELECT p FROM Plnaccionseguimiento p WHERE p.idseguimiento = :idseguimiento"),
    @NamedQuery(name = "Plnaccionseguimiento.findByEstado", query = "SELECT p FROM Plnaccionseguimiento p WHERE p.estado = :estado")})
public class Plnaccionseguimiento implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idseguimiento;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(nullable = false, length = 20)
    private String estado;
    @Size(max = 1000)
    @Column(length = 1000)
    private String accireque;
    @Size(max = 1000)
    @Column(length = 1000)
    private String compromiso;
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

    public Plnaccionseguimiento() {
    }

    public Plnaccionseguimiento(BigInteger idseguimiento) {
        this.idseguimiento = idseguimiento;
    }

    public Plnaccionseguimiento(BigInteger idseguimiento, String estado, String usercrea, Date fchacrea) {
        this.idseguimiento = idseguimiento;
        this.estado = estado;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdseguimiento() {
        return idseguimiento;
    }

    public void setIdseguimiento(BigInteger idseguimiento) {
        this.idseguimiento = idseguimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAccireque() {
        return accireque;
    }

    public void setAccireque(String accireque) {
        this.accireque = accireque;
    }

    public String getCompromiso() {
        return compromiso;
    }

    public void setCompromiso(String compromiso) {
        this.compromiso = compromiso;
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
        hash += (idseguimiento != null ? idseguimiento.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnaccionseguimiento)) {
            return false;
        }
        Plnaccionseguimiento other = (Plnaccionseguimiento) object;
        if ((this.idseguimiento == null && other.idseguimiento != null) || (this.idseguimiento != null && !this.idseguimiento.equals(other.idseguimiento))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnaccionseguimiento[ idseguimiento=" + idseguimiento + " ]";
    }
    
}
