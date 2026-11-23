package cl.zaror.alertas.exception;

// La foto recibida no se puede guardar: vacia, demasiado grande o que no es una imagen.
public class FotoInvalidaException extends RuntimeException {
    public FotoInvalidaException(String mensaje) {
        super(mensaje);
    }
}
