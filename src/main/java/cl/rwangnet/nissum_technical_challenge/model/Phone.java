package cl.rwangnet.nissum_technical_challenge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;

/**
 * Entidad JPA que representa un teléfono asociado a un usuario.
 * 
 * Mapea la tabla 'phones' y mantiene una relación Many-to-One con User.
 * Cada teléfono incluye número, código de ciudad y código de país.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "phones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String number;

    @Column(name = "city_code", nullable = false)
    private String cityCode;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}