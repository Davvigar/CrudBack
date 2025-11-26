package org.david.crm.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "comerciales")
public class Comercial implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comercial_id")
    private Integer comercialId;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "nombre", length = 150)
    private String nombre;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @Column(name = "telefono", length = 30)
    private String telefono;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol = Rol.comercial;
    
    // Locking optimista: previene Lost Updates
    @Version
    @Column(name = "version")
    private Integer version;
    
    public enum Rol {
        pseudoadmin, comercial
    }
    
    public Comercial() {
    }
    
    public Integer getComercialId() {
        return comercialId;
    }
    
    public void setComercialId(Integer comercialId) {
        this.comercialId = comercialId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
}

