package com.coop1.soficoop.pln.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
    @NamedQuery(name = "Plnperspectiva.findAll", query = "SELECT p FROM Plnperspectiva p"),
    @NamedQuery(name = "Plnperspectiva.findByIdmaestro", query = "SELECT p FROM Plnperspectiva p WHERE p.idmaestro = :idmaestro"),
    @NamedQuery(name = "Plnperspectiva.findByNombre", query = "SELECT p FROM Plnperspectiva p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Plnperspectiva.findByEstado", query = "SELECT p FROM Plnperspectiva p WHERE p.estado = :estado")})
public class Plnperspectiva implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idmaestro;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;
    private Short estado;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String usercrea;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchacrea;
    @Size(max = 50)
    @Column(length = 50)
    private String usermod;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fchamod;

    public Plnperspectiva() {
    }

    public Plnperspectiva(BigInteger idmaestro) {
        this.idmaestro = idmaestro;
    }

    public Plnperspectiva(BigInteger idmaestro, String nombre, String usercrea, Date fchacrea) {
        this.idmaestro = idmaestro;
        this.nombre = nombre;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdmaestro() {
        return idmaestro;
    }

    public void setIdmaestro(BigInteger idmaestro) {
        this.idmaestro = idmaestro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Short getEstado() {
        return estado;
    }

    public void setEstado(Short estado) {
        this.estado = estado;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idmaestro != null ? idmaestro.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnperspectiva)) {
            return false;
        }
        Plnperspectiva other = (Plnperspectiva) object;
        if ((this.idmaestro == null && other.idmaestro != null) || (this.idmaestro != null && !this.idmaestro.equals(other.idmaestro))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnperspectiva[ idmaestro=" + idmaestro + " ]";
    }
    
}
