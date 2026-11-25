package cl.zaror.alertas.model;

import jakarta.persistence.*;

// Foto de una alerta. Vive en su propia tabla para que listar alertas no cargue las imagenes.
// La llave es el id de la alerta: una alerta tiene como maximo una foto.
@Entity
@Table(name = "fotos_alerta")
public class FotoAlerta {

    @Id
    private Long alertaId;

    // Ej: "image/jpeg"
    @Column(nullable = false, length = 60)
    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] datos;

    public FotoAlerta() {
    }

    public FotoAlerta(Long alertaId, String contentType, byte[] datos) {
        this.alertaId = alertaId;
        this.contentType = contentType;
        this.datos = datos;
    }

    public Long getAlertaId() { return alertaId; }
    public String getContentType() { return contentType; }
    public byte[] getDatos() { return datos; }
}
