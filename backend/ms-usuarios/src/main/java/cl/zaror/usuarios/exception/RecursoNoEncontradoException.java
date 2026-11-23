package cl.zaror.usuarios.exception;

// Se transforma en respuesta 404
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
