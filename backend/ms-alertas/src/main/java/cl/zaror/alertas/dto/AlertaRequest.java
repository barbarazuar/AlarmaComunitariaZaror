package cl.zaror.alertas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos para crear (POST) o editar completa (PUT) una alerta.
// fechaHora es opcional: si no viene, el servidor pone la hora actual.
public record AlertaRequest(
        @NotBlank(message = "El nombre del vecino es obligatorio")
        @Size(max = 80, message = "El nombre es muy largo")
        String vecinoNombre,

        Long usuarioId,

        @NotBlank(message = "La ubicacion es obligatoria")
        @Size(max = 120, message = "La ubicacion es muy larga")
        String ubicacion,

        Long fechaHora,

        @NotBlank(message = "El tipo de emergencia es obligatorio")
        @Size(max = 60, message = "El tipo de emergencia es muy largo")
        String tipoEmergencia,

        @Size(max = 300, message = "La descripcion no puede pasar de 300 caracteres")
        String descripcion
) {
}
