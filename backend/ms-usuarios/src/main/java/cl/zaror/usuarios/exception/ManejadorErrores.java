package cl.zaror.usuarios.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Atrapa las excepciones de todos los controladores y las convierte en JSON con su codigo HTTP
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), Map.of());
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> duplicado(CorreoDuplicadoException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), Map.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage(), Map.of());
    }

    // Errores de @Valid: devuelve campo -> mensaje
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errores.putIfAbsent(err.getField(), err.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Datos invalidos", errores);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensaje, errores));
    }
}
