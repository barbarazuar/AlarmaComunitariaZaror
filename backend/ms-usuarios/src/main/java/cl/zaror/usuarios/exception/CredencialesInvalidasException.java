package cl.zaror.usuarios.exception;

// Se transforma en respuesta 401
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o clave incorrectos");
    }
}
