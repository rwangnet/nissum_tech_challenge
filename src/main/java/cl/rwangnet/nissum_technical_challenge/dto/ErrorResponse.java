package cl.rwangnet.nissum_technical_challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String mensaje;

    public static ErrorResponse of(String mensaje) {
        return ErrorResponse.builder()
                .mensaje(mensaje)
                .build();
    }
}