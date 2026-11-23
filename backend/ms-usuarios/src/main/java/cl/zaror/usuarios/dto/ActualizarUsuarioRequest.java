package cl.zaror.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Solo se puede cambiar nombre y direccion. El correo identifica la cuenta.
public record ActualizarUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 150, message = "La direccion es muy larga")
        String direccion
) {
}
