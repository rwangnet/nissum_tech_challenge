package cl.rwangnet.nissum_technical_challenge.service;

import cl.rwangnet.nissum_technical_challenge.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import jakarta.validation.ValidationException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio centralizado para manejo de errores y respuestas HTTP.
 * 
 * Proporciona métodos estandarizados para el manejo de diferentes tipos
 * de errores en la aplicación, garantizando respuestas consistentes y
 * códigos de estado HTTP apropiados.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Service
public class ErrorHandlingService {

    /**
     * Maneja errores cuando un usuario ya existe en el sistema.
     * 
     * @param ex Excepción de usuario ya existente
     * @return ResponseEntity con código 409 (Conflict) y mensaje de error
     */
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Maneja errores de validación de Bean Validation.
     * 
     * Extrae todos los errores de campo y los concatena en un mensaje
     * legible para el cliente.
     * 
     * @param bindingResult Resultado de validación con errores de campo
     * @return ResponseEntity con código 400 (Bad Request) y mensajes de validación
     */
    public ResponseEntity<Map<String, String>> handleValidationErrors(BindingResult bindingResult) {
        String errors = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity
                .badRequest()
                .body(Map.of("mensaje", errors));
    }

    /**
     * Maneja excepciones de validación generales.
     * 
     * @param ex Excepción de validación
     * @return ResponseEntity con código 400 (Bad Request) y mensaje de error
     */
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Maneja errores genéricos no específicos.
     * 
     * Proporciona una respuesta segura sin exponer detalles internos
     * del sistema en producción.
     * 
     * @param ex Excepción genérica
     * @return ResponseEntity con código 500 (Internal Server Error)
     */
    public ResponseEntity<Map<String, String>> handleGenericError(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno del servidor"));
    }

    /**
     * Maneja errores de solicitud incorrecta con mensaje personalizado.
     * 
     * @param message Mensaje de error específico
     * @return ResponseEntity con código 400 (Bad Request)
     */
    public ResponseEntity<Map<String, String>> handleBadRequest(String message) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("mensaje", message));
    }

    /**
     * Maneja errores de acceso prohibido (403).
     * 
     * @param message Mensaje de error opcional
     * @return ResponseEntity con código 403 (Forbidden)
     */
    public ResponseEntity<Map<String, String>> handleForbidden(String message) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensaje", message != null ? message : "Acceso prohibido"));
    }

    /**
     * Maneja errores de autenticación requerida (401).
     * 
     * @param message Mensaje de error opcional
     * @return ResponseEntity con código 401 (Unauthorized)
     */
    public ResponseEntity<Map<String, String>> handleUnauthorized(String message) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensaje", message != null ? message : "No autorizado - token requerido"));
    }
}