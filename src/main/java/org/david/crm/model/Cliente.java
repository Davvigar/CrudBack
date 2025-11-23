package org.david.crm.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente implements Serializable {
    
    @Id // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto incremento
    @Column(name = "cliente_id")
    private Integer clienteId;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "nombre", length = 150)
    private String nombre;
    
    @Column(name = "apellidos", length = 150)
    private String apellidos;
    
    @Column(name = "edad")
    private Integer edad;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @Column(name = "telefono", length = 30)
    private String telefono;
    
    @Column(name = "direccion", length = 300)
    private String direccion;
    
    @ManyToOne(fetch = FetchType.LAZY) // varios clientes pueden estar asociados a un comercial (el comecial solo carga cuando se carga un cliente)
    @JoinColumn(name = "comercial_id")
    private Comercial comercial;
    
    public Cliente() {
    }
    
    public Integer getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
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
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public Integer getEdad() {
        return edad;
    }
    
    public void setEdad(Integer edad) {
        this.edad = edad;
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
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public Comercial getComercial() {
        return comercial;
    }
    
    public void setComercial(Comercial comercial) {
        this.comercial = comercial;
    }
}

