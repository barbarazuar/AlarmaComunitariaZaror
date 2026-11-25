package cl.zaror.alertas.dto;

import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;

// Lo que devuelve la API. Separado de la entidad para no exponer la tabla directamente.
// tieneFoto avisa a la app si puede descargar la imagen con GET /api/alertas/{id}/foto.
public record AlertaResponse(
        Long id,
        String vecinoNombre,
        Long usuarioId,
        String ubicacion,
        Long fechaHora,
        String tipoEmergencia,
        String descripcion,
        EstadoAlerta estado,
        boolean tieneFoto
) {
    // Constructor sin foto, para los casos en que la alerta no tiene imagen
    public AlertaResponse(Long id, String vecinoNombre, Long usuarioId, String ubicacion, Long fechaHora,
                          String tipoEmergencia, String descripcion, EstadoAlerta estado) {
        this(id, vecinoNombre, usuarioId, ubicacion, fechaHora, tipoEmergencia, descripcion, estado, false);
    }

    public static AlertaResponse desde(Alerta a, boolean tieneFoto) {
        return new AlertaResponse(a.getId(), a.getVecinoNombre(), a.getUsuarioId(), a.getUbicacion(),
                a.getFechaHora(), a.getTipoEmergencia(), a.getDescripcion(), a.getEstado(), tieneFoto);
    }

    public static AlertaResponse desde(Alerta a) {
        return desde(a, false);
    }
}
