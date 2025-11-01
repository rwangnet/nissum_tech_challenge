package cl.rwangnet.nissum_technical_challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para datos de teléfono en solicitudes de registro.
 * 
 * Representa la información de un teléfono que será asociado
 * a un usuario durante el registro. Incluye validaciones
 * para garantizar que todos los campos sean proporcionados.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneRequest {

    @Schema(description = "Número de teléfono", example = "12345678")
    @NotBlank(message = "El número de teléfono es obligatorio")
    private String number;

    @Schema(description = "Código de ciudad", example = "2")
    @NotBlank(message = "El código de ciudad es obligatorio")
    private String citycode;

    @Schema(description = "Código de país", example = "56")
    @NotBlank(message = "El código de país es obligatorio")
    private String contrycode;
}