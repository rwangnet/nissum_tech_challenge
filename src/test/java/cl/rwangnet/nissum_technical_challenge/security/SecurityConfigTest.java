package cl.rwangnet.nissum_technical_challenge.security;

import cl.rwangnet.nissum_technical_challenge.config.SecurityConfig;
import cl.rwangnet.nissum_technical_challenge.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para la configuración de seguridad.
 * 
 * Valida la configuración de Spring Security incluyendo:
 * - Configuración del encoder de contraseñas BCrypt
 * - Validación de fortaleza y seguridad de contraseñas
 * - Comportamiento thread-safe del encoder
 * - Manejo de casos límite y errores
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Prueba la creación correcta del bean PasswordEncoder.
     * 
     * Verifica que:
     * - Se cree una instancia de BCryptPasswordEncoder
     * - El bean no sea nulo
     * - Se use la implementación correcta para encriptación segura
     */
    @Test
    @DisplayName("Debería crear PasswordEncoder bean correctamente")
    void shouldCreatePasswordEncoderBean() {
        SecurityConfig securityConfig = new SecurityConfig();
        
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.getClass().getSimpleName()).isEqualTo("BCryptPasswordEncoder");
    }

    /**
     * Prueba el funcionamiento básico del PasswordEncoder.
     * 
     * Verifica que:
     * - Se genere un hash no nulo y diferente de la contraseña original
     * - El hash comience con "$2a$" (formato BCrypt)
     * - Se valide correctamente la contraseña original contra el hash
     * - Se rechace una contraseña incorrecta
     */
    @Test
    @DisplayName("PasswordEncoder debería funcionar correctamente")
    void passwordEncoderShouldWorkCorrectly() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        boolean doesNotMatch = passwordEncoder.matches("wrongPassword", encodedPassword);
        
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$");
        assertThat(matches).isTrue();
        assertThat(doesNotMatch).isFalse();
    }

    /**
     * Prueba que BCrypt genere hashes diferentes para la misma contraseña.
     * 
     * Verifica que:
     * - Dos encriptaciones de la misma contraseña produzcan hashes diferentes
     * - Ambos hashes sean válidos para verificar la contraseña original
     * - Se garantice la seguridad mediante salt aleatorio en cada encriptación
     */
    @Test
    @DisplayName("PasswordEncoder debería generar diferentes hashes para la misma contraseña")
    void passwordEncoderShouldGenerateDifferentHashesForSamePassword() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "samePassword";
        
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);
        
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordEncoder.matches(password, hash1)).isTrue();
        assertThat(passwordEncoder.matches(password, hash2)).isTrue();
    }

    /**
     * Prueba la inyección de dependencias del JwtAuthenticationFilter en SecurityConfig.
     * 
     * Verifica que:
     * - El filtro JWT se pueda inyectar correctamente via reflection
     * - La dependencia no sea nula después de la inyección
     * - La referencia coincida con el objeto mock inyectado
     */
    @Test
    @DisplayName("SecurityConfig debería tener JwtAuthenticationFilter inyectado")
    void securityConfigShouldHaveJwtAuthenticationFilterInjected() {
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "jwtAuthenticationFilter", jwtAuthenticationFilter);
        
        Object injectedFilter = ReflectionTestUtils.getField(securityConfig, "jwtAuthenticationFilter");
        
        assertThat(injectedFilter).isNotNull();
        assertThat(injectedFilter).isEqualTo(jwtAuthenticationFilter);
    }

    /**
     * Prueba el manejo de casos extremos con contraseñas vacías y nulas.
     * 
     * Verifica que:
     * - Se puedan encriptar contraseñas vacías sin errores
     * - Las contraseñas vacías se validen correctamente
     * - Se lance IllegalArgumentException al intentar encriptar null
     * - Se mantenga la seguridad incluso con valores extremos
     */
    @Test
    @DisplayName("Debería validar configuración con contraseñas vacías y nulas")
    void shouldValidateConfigurationWithEmptyAndNullPasswords() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        String emptyPasswordHash = passwordEncoder.encode("");
        assertThat(emptyPasswordHash).isNotNull();
        assertThat(passwordEncoder.matches("", emptyPasswordHash)).isTrue();
        assertThat(passwordEncoder.matches("notEmpty", emptyPasswordHash)).isFalse();
        
        try {
            passwordEncoder.encode(null);
            assertThat(false).as("Debería lanzar excepción con contraseña null").isTrue();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    /**
     * Prueba el manejo de contraseñas en el límite máximo de BCrypt (72 bytes).
     * 
     * Verifica que:
     * - Se puedan encriptar contraseñas de exactamente 72 caracteres
     * - La validación funcione correctamente con contraseñas al límite
     * - No se produzcan errores con el tamaño máximo permitido
     */
    @Test
    @DisplayName("PasswordEncoder debería manejar contraseñas dentro del límite de BCrypt (72 bytes)")
    void passwordEncoderShouldHandlePasswordsWithinBCryptLimit() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String maxLengthPassword = "a".repeat(72);
        
        String encodedPassword = passwordEncoder.encode(maxLengthPassword);
        boolean matches = passwordEncoder.matches(maxLengthPassword, encodedPassword);
        
        assertThat(encodedPassword).isNotNull();
        assertThat(matches).isTrue();
    }

    /**
     * Prueba que se rechacen contraseñas que excedan el límite de BCrypt (73+ bytes).
     * 
     * Verifica que:
     * - Se lance IllegalArgumentException con contraseñas de más de 72 bytes
     * - El mensaje de error contenga información sobre el límite
     * - Se mantenga la integridad de BCrypt respetando sus limitaciones
     */
    @Test
    @DisplayName("PasswordEncoder debería rechazar contraseñas que excedan el límite de BCrypt")
    void passwordEncoderShouldRejectPasswordsExceedingBCryptLimit() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String tooLongPassword = "a".repeat(73);
        
        try {
            passwordEncoder.encode(tooLongPassword);
            assertThat(false).as("Debería lanzar excepción con contraseña muy larga").isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("password cannot be more than 72 bytes");
        }
    }

    /**
     * Prueba el manejo de contraseñas con caracteres especiales y Unicode.
     * 
     * Verifica que:
     * - Se encripten correctamente contraseñas con símbolos especiales
     * - Se manejen caracteres Unicode (acentos, ñ, etc.)
     * - La validación funcione con caracteres no ASCII
     * - Se preserve la integridad con codificación de caracteres
     */
    @Test
    @DisplayName("PasswordEncoder debería manejar contraseñas con caracteres especiales")
    void passwordEncoderShouldHandleSpecialCharacters() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~áéíóúñ";
        
        String encodedSpecialPassword = passwordEncoder.encode(specialPassword);
        boolean matches = passwordEncoder.matches(specialPassword, encodedSpecialPassword);
        
        assertThat(encodedSpecialPassword).isNotNull();
        assertThat(matches).isTrue();
    }

    /**
     * Prueba la seguridad de concurrencia (thread-safety) del PasswordEncoder.
     * 
     * Verifica que:
     * - Múltiples hilos puedan usar el encoder simultáneamente sin conflictos
     * - Cada hilo genere hashes únicos y válidos
     * - No ocurran condiciones de carrera o corrupción de datos
     * - Se mantenga la integridad en entornos multi-threaded
     */
    @Test
    @DisplayName("PasswordEncoder debería ser thread-safe")
    void passwordEncoderShouldBeThreadSafe() throws InterruptedException {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password = "threadSafeTest";
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        String[] results = new String[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = passwordEncoder.encode(password);
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        for (String result : results) {
            assertThat(result).isNotNull();
            assertThat(passwordEncoder.matches(password, result)).isTrue();
        }
        
        for (int i = 0; i < threadCount; i++) {
            for (int j = i + 1; j < threadCount; j++) {
                assertThat(results[i]).isNotEqualTo(results[j]);
            }
        }
    }

    /**
     * Prueba la instanciación básica del bean SecurityConfig.
     * 
     * Verifica que:
     * - Se pueda crear una instancia de SecurityConfig sin errores
     * - El objeto creado no sea nulo
     * - La clase se instancie correctamente como bean de Spring
     */
    @Test
    @DisplayName("SecurityConfig bean debería ser instanciable")
    void securityConfigBeanShouldBeInstantiable() {
        SecurityConfig securityConfig = new SecurityConfig();
        
        assertThat(securityConfig).isNotNull();
        assertThat(securityConfig.getClass()).isEqualTo(SecurityConfig.class);
    }
}