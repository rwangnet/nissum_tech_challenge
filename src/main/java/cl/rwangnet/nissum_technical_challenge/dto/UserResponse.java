package cl.rwangnet.nissum_technical_challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para respuestas que contienen información de usuario.
 * 
 * Representa la información completa de un usuario que se retorna
 * en las respuestas de la API, incluyendo metadatos de auditoría
 * y token JWT. No incluye información sensible como contraseñas.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private List<PhoneResponse> phones;
    private LocalDateTime created;
    private LocalDateTime modified;
    private LocalDateTime lastLogin;
    private String token;
    private Boolean isActive;
}