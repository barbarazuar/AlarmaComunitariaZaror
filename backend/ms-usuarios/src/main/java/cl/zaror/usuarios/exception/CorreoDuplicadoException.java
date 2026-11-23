package cl.zaror.usuarios.exception;

// Se transforma en respuesta 409 (conflicto)
public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String correo) {
        super("Ya existe una cuenta con el correo " + correo);
    }
}
