package cl.rwangnet.nissum_technical_challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para solicitudes de registro de usuario.
 * 
 * Contiene todos los datos necesarios para crear un nuevo usuario
 * incluyendo validaciones de Bean Validation para garantizar
 * la integridad de los datos de entrada.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {

    @Schema(description = "Nombre completo del usuario", example = "Juan Rodríguez")
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Schema(description = "Correo electrónico del usuario", example = "juan.rodriguez@dominio.cl")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
             message = "El correo debe seguir el formato: usuario@dominio.cl")
    private String email;

    @Schema(description = "Contraseña del usuario (mínimo 8 caracteres, debe incluir mayúscula, minúscula, número y carácter especial)", 
            example = "HunTer2023!")
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "La contraseña debe contener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
    private String password;

    @Schema(description = "Lista de teléfonos del usuario")
    @NotEmpty(message = "Debe incluir al menos un teléfono")
    private List<PhoneRequest> phones;
}