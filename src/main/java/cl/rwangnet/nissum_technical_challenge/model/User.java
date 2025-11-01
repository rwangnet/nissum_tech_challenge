package cl.rwangnet.nissum_technical_challenge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa un usuario del sistema.
 * 
 * Mapea la tabla 'users' en la base de datos y contiene toda la información
 * del usuario incluyendo credenciales, metadatos de auditoría y relaciones
 * con teléfonos. Utiliza UUIDs como identificadores y timestamps automáticos.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Phone> phones;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(nullable = false)
    private LocalDateTime modified;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        created = now;
        modified = now;
        lastLogin = now;
        isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        modified = LocalDateTime.now();
    }
}