package cl.zaror.alertas.model;

import jakarta.persistence.*;

// @Entity = esta clase es una tabla en la base de datos
@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String vecinoNombre;

    // id del usuario en ms-usuarios. No es llave foranea porque cada microservicio tiene su propia BD.
    private Long usuarioId;

    @Column(nullable = false, length = 120)
    private String ubicacion;

    // Milisegundos desde 1970, igual que en la app (System.currentTimeMillis())
    @Column(nullable = false)
    private Long fechaHora;

    @Column(nullable = false, length = 60)
    private String tipoEmergencia;

    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING) // se guarda como texto "ACTIVA" y no como numero
    @Column(nullable = false, length = 20)
    private EstadoAlerta estado = EstadoAlerta.ACTIVA;

    public Alerta() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVecinoNombre() { return vecinoNombre; }
    public void setVecinoNombre(String vecinoNombre) { this.vecinoNombre = vecinoNombre; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Long getFechaHora() { return fechaHora; }
    public void setFechaHora(Long fechaHora) { this.fechaHora = fechaHora; }
    public String getTipoEmergencia() { return tipoEmergencia; }
    public void setTipoEmergencia(String tipoEmergencia) { this.tipoEmergencia = tipoEmergencia; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoAlerta getEstado() { return estado; }
    public void setEstado(EstadoAlerta estado) { this.estado = estado; }
}
