package cl.zaror.alertas.exception;

import java.util.Map;

// Formato unico de error para que la app siempre lea lo mismo
public record ErrorResponse(int status, String mensaje, Map<String, String> errores) {
}
