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
import javax.persistence.Transient;


/**
 *
 * @author ENVY360
 */
@Entity
@Table(name = "PLNACCIONDETA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plnacciondeta.findAll", query = "SELECT p FROM Plnacciondeta p")})
public class Plnacciondeta implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false, precision = 0, scale = -127)
    private BigInteger idaccionpao;
    @Basic(optional = false)
    @NotNull
@Size(min = 1, max = 1000)  // ← Cambiar
@Column(length = 1000)      // ← Cambiar
private String descrip;
    @Column(precision = 12, scale = 2)
    private BigDecimal presupropues;
    @Column(precision = 12, scale = 2)
    private BigDecimal presuaprob;
    @Column(precision = 12, scale = 2)
    private BigDecimal valorproyec;
    @Column(precision = 12, scale = 2)
    private BigDecimal valorejecu;
@Size(max = 1000)           // ← Cambiar
@Column(length = 1000)      // ← Cambiar
private String medioverifi;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idaccionpao", fetch = FetchType.LAZY)
    private List<Plnaccioneval> plnaccievalList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idaccionpao", fetch = FetchType.LAZY)
    private List<Plnaccionseguimiento> plnacciseguiList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idaccionpao", fetch = FetchType.LAZY)
    private List<Plnaccidetplantrim> plnaccidetplantrimList;
    @JoinColumn(name = "IDESTRATEGIA", referencedColumnName = "IDESTRATEGIA", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpeplinestr idestrategia;
    @JoinColumn(name = "IDPAO", referencedColumnName = "IDPAO", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpao idpao;
    @Transient
    private Plnaccioneval ultimaEvaluacion;
// + Getter y Setter

    public Plnacciondeta() {
    }

    public Plnacciondeta(BigInteger idaccionpao) {
        this.idaccionpao = idaccionpao;
    }

    public Plnacciondeta(BigInteger idaccionpao, String descrip, String usercrea, Date fchacrea) {
        this.idaccionpao = idaccionpao;
        this.descrip = descrip;
        this.usercrea = usercrea;
        this.fchacrea = fchacrea;
    }

    public BigInteger getIdaccionpao() {
        return idaccionpao;
    }

    public void setIdaccionpao(BigInteger idaccionpao) {
        this.idaccionpao = idaccionpao;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    public BigDecimal getPresupropues() {
        return presupropues;
    }

    public void setPresupropues(BigDecimal presupropues) {
        this.presupropues = presupropues;
    }

    public BigDecimal getPresuaprob() {
        return presuaprob;
    }

    public void setPresuaprob(BigDecimal presuaprob) {
        this.presuaprob = presuaprob;
    }

    public BigDecimal getValorproyec() {
        return valorproyec;
    }

    public void setValorproyec(BigDecimal valorproyec) {
        this.valorproyec = valorproyec;
    }

    public BigDecimal getValorejecu() {
        return valorejecu;
    }

    public void setValorejecu(BigDecimal valorejecu) {
        this.valorejecu = valorejecu;
    }

    public String getMedioverifi() {
        return medioverifi;
    }

    public void setMedioverifi(String medioverifi) {
        this.medioverifi = medioverifi;
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
    public List<Plnaccioneval> getPlnaccievalList() {
        return plnaccievalList;
    }

    public void setPlnaccievalList(List<Plnaccioneval> plnaccievalList) {
        this.plnaccievalList = plnaccievalList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Plnaccionseguimiento> getPlnaccionseguimientoList() {
        return plnacciseguiList;
    }

    public void setPlnaccionseguimientoList(List<Plnaccionseguimiento> plnacciseguiList) {
        this.plnacciseguiList = plnacciseguiList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Plnaccidetplantrim> getPlnaccidetplantrimList() {
        return plnaccidetplantrimList;
    }

    public void setPlnaccidetplantrimList(List<Plnaccidetplantrim> plnaccidetplantrimList) {
        this.plnaccidetplantrimList = plnaccidetplantrimList;
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
        hash += (idaccionpao != null ? idaccionpao.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Plnacciondeta)) {
            return false;
        }
        Plnacciondeta other = (Plnacciondeta) object;
        if ((this.idaccionpao == null && other.idaccionpao != null) || (this.idaccionpao != null && !this.idaccionpao.equals(other.idaccionpao))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.coop1.soficoop.pln.entidades.Plnacciondeta[ idaccionpao=" + idaccionpao + " ]";
    }
    
    
    @Transient
    private boolean editando = false;

    public boolean isEditando() {
        return editando;
    }

    public void setEditando(boolean editando) {
        this.editando = editando;
    }

    public Plnaccioneval getUltimaEvaluacion() {
        return ultimaEvaluacion;
    }

    public void setUltimaEvaluacion(Plnaccioneval ultimaEvaluacion) {
        this.ultimaEvaluacion = ultimaEvaluacion;
    }
    
    

    
}
