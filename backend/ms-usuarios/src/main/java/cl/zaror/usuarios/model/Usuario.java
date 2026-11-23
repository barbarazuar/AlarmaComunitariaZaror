package cl.zaror.usuarios.model;

import jakarta.persistence.*;

// @Entity = esta clase es una tabla en la base de datos
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la BD genera el id (1, 2, 3...)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120) // no se repiten correos
    private String correo;

    @Column(nullable = false)
    private String claveHash; // nunca la clave tal cual, solo el hash BCrypt

    @Column(nullable = false, length = 150)
    private String direccion;

    // JPA necesita un constructor vacio
    public Usuario() {
    }

    public Usuario(String nombre, String correo, String claveHash, String direccion) {
        this.nombre = nombre;
        this.correo = correo;
        this.claveHash = claveHash;
        this.direccion = direccion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
