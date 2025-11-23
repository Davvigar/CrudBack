package org.david.crm.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
public class Factura implements Serializable {
    
    @Id
    @Column(name = "factura_id", length = 50)
    private String facturaId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comercial_id")
    private Comercial comercial;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private Estado estado = Estado.pendiente;
    
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "total_iva", precision = 12, scale = 2)
    private BigDecimal totalIva;
    
    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;
    
    public enum Estado {
        pendiente, pagada
    }
    
    @PrePersist
    public void prePersist() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }
    
    public Factura() {
    }
    
    public String getFacturaId() {
        return facturaId;
    }
    
    public void setFacturaId(String facturaId) {
        this.facturaId = facturaId;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Comercial getComercial() {
        return comercial;
    }
    
    public void setComercial(Comercial comercial) {
        this.comercial = comercial;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }
    
    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
    
    public Estado getEstado() {
        return estado;
    }
    
    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTotalIva() {
        return totalIva;
    }
    
    public void setTotalIva(BigDecimal totalIva) {
        this.totalIva = totalIva;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}

