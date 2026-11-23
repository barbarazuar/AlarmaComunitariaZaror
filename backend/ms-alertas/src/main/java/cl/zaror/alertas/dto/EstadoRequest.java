package cl.zaror.alertas.dto;

import cl.zaror.alertas.model.EstadoAlerta;
import jakarta.validation.constraints.NotNull;

// Body del PATCH para cambiar solo el estado, ej: {"estado": "ATENDIDA"}
public record EstadoRequest(
        @NotNull(message = "El estado es obligatorio (ACTIVA o ATENDIDA)")
        EstadoAlerta estado
) {
}
