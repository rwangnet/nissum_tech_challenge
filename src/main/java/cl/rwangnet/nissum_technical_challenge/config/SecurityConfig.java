package cl.rwangnet.nissum_technical_challenge.config;

import cl.rwangnet.nissum_technical_challenge.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security.
 * 
 * Define la configuración completa de seguridad para la aplicación incluyendo:
 * - Deshabilitación de CSRF para APIs REST
 * - Configuración stateless con JWT
 * - Rutas públicas y protegidas
 * - Integración del filtro de autenticación JWT
 * - Configuración de encoders de contraseña
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Filtro personalizado para autenticación JWT */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura el encoder de contraseñas usando BCrypt.
     * 
     * BCrypt es un algoritmo de hash adaptativo diseñado específicamente
     * para contraseñas que incluye salt automático y es resistente a
     * ataques de fuerza bruta y rainbow tables.
     * 
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * Establece la configuración completa de seguridad incluyendo:
     * - Deshabilitación de CSRF (no necesario para APIs REST stateless)
     * - Política de sesión STATELESS para JWT
     * - Rutas públicas: registro, documentación, consola H2
     * - Rutas protegidas: todos los demás endpoints
     * - Integración del filtro JWT antes del filtro de autenticación estándar
     * - Configuración de headers para permitir frames (H2 console)
     * 
     * @param http Configurador de seguridad HTTP
     * @return SecurityFilterChain configurada
     * @throws Exception si hay errores en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                .requestMatchers("/favicon.ico", "/error").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}