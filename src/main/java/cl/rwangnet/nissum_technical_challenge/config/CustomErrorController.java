package cl.rwangnet.nissum_technical_challenge.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
        
        HttpStatus status = HttpStatus.valueOf(statusCode != null ? statusCode : 500);
        
        String message = switch (status.value()) {
            case 400 -> "Solicitud inválida - verifique los datos enviados";
            case 401 -> "No autorizado - token JWT requerido";
            case 403 -> "Acceso prohibido - token JWT inválido o endpoint protegido";
            case 404 -> "Recurso no encontrado";
            case 409 -> "Conflicto - el recurso ya existe";
            case 500 -> "Error interno del servidor";
            default -> errorMessage != null ? errorMessage : "Error en la aplicación";
        };

        return ResponseEntity.status(status)
                .body(Map.of("mensaje", message));
    }
}