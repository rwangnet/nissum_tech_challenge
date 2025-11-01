package cl.rwangnet.nissum_technical_challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para respuestas que contienen información de teléfono.
 * 
 * Representa la información de un teléfono que se retorna
 * en las respuestas de la API, incluyendo su ID único
 * y todos los datos del teléfono.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneResponse {

    private UUID id;
    private String number;
    private String citycode;
    private String contrycode;
}