package org.david.crm.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "secciones")
public class Seccion implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seccion_id")
    private Integer seccionId;
    
    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;
    
    public Seccion() {
    }
    
    public Integer getSeccionId() {
        return seccionId;
    }
    
    public void setSeccionId(Integer seccionId) {
        this.seccionId = seccionId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

