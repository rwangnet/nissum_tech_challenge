package cl.rwangnet.nissum_technical_challenge.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad específica para el entorno de testing.
 * Proporciona una configuración de seguridad simplificada que permite el acceso
 * sin restricciones a todos los endpoints durante la ejecución de pruebas unitarias.
 * 
 * @author Ricardo Wangnet
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    /**
     * Configura una cadena de filtros de seguridad permisiva para testing.
     * Desactiva CSRF, configuración de sesiones sin estado y permite acceso
     * a todos los endpoints sin autenticación para facilitar la ejecución de pruebas.
     * 
     * @param http el objeto HttpSecurity para configurar la seguridad web
     * @return SecurityFilterChain configurada para el entorno de testing
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}