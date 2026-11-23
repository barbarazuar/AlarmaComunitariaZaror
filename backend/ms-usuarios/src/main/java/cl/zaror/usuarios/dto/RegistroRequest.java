package cl.zaror.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lo que manda la app al registrarse. Las anotaciones validan antes de llegar al servicio.
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        String correo,

        @NotBlank(message = "La clave es obligatoria")
        @Size(min = 6, max = 60, message = "La clave debe tener entre 6 y 60 caracteres")
        String clave,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 150, message = "La direccion es muy larga")
        String direccion
) {
}
