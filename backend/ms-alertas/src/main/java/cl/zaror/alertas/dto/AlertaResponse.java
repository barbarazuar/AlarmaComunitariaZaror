package cl.zaror.alertas.dto;

import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;

// Lo que devuelve la API. Separado de la entidad para no exponer la tabla directamente.
public record AlertaResponse(
        Long id,
        String vecinoNombre,
        Long usuarioId,
        String ubicacion,
        Long fechaHora,
        String tipoEmergencia,
        String descripcion,
        EstadoAlerta estado
) {
    public static AlertaResponse desde(Alerta a) {
        return new AlertaResponse(a.getId(), a.getVecinoNombre(), a.getUsuarioId(), a.getUbicacion(),
                a.getFechaHora(), a.getTipoEmergencia(), a.getDescripcion(), a.getEstado());
    }
}
