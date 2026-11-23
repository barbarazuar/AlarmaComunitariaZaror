package cl.zaror.alertas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    // Foto vacia, muy grande o que no es una imagen
    @ExceptionHandler(FotoInvalidaException.class)
    public ResponseEntity<ErrorResponse> fotoInvalida(FotoInvalidaException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), Map.of());
    }

    // Errores de @Valid: devuelve campo -> mensaje
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errores.putIfAbsent(err.getField(), err.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Datos invalidos", errores);
    }

    // JSON mal formado o un estado que no existe (ej: "estado": "CERRADA")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es valido", Map.of());
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensaje, errores));
    }
}
