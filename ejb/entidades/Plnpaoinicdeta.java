package com.coop1.soficoop.pln.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "PLNPAOINICDETA")
public class Plnpaoinicdeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "IDINICDETA")
    private BigInteger idinicdeta;

    @JoinColumn(name = "IDINICIATIVA", referencedColumnName = "IDINICIATIVA")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnpaoinic idiniciativa;

    @JoinColumn(name = "IDACCIONPAO", referencedColumnName = "IDACCIONPAO")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Plnacciondeta idacciondeta; 

    public Plnpaoinicdeta() {}

    public BigInteger getIdinicdeta() { return idinicdeta; }
    public void setIdinicdeta(BigInteger idinicdeta) { this.idinicdeta = idinicdeta; }

    public Plnpaoinic getIdiniciativa() { return idiniciativa; }
    public void setIdiniciativa(Plnpaoinic idiniciativa) { this.idiniciativa = idiniciativa; }

    public Plnacciondeta getIdacciondeta() { return idacciondeta; }
    public void setIdacciondeta(Plnacciondeta idacciondeta) { this.idacciondeta = idacciondeta; }
    
    @Override
    public int hashCode() {
        return (idinicdeta != null ? idinicdeta.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Plnpaoinicdeta)) return false;
        Plnpaoinicdeta other = (Plnpaoinicdeta) object;
        return (this.idinicdeta != null || other.idinicdeta == null) && (this.idinicdeta == null || this.idinicdeta.equals(other.idinicdeta));
    }
}