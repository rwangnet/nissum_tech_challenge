package cl.rwangnet.nissum_technical_challenge.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase JwtUtil.
 * 
 * Valida todas las funcionalidades relacionadas con la generación,
 * validación y extracción de información de tokens JWT incluyendo
 * casos de éxito y escenarios de error.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm";
    private final Long testExpiration = 3600000L;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    /**
     * Prueba la generación correcta de tokens JWT.
     * 
     * Verifica que:
     * - El token generado no sea nulo ni vacío
     * - Tenga el formato JWT correcto (contiene puntos)
     * - Sea válido según las validaciones internas
     * - Contenga el email correcto como subject
     */
    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken(testEmail);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        
        assertTrue(jwtUtil.isValidToken(token));
        assertEquals(testEmail, jwtUtil.extractUsername(token));
    }

    /**
     * Prueba la extracción correcta del username (email) desde un token JWT.
     * 
     * Verifica que el email extraído del token coincida exactamente
     * con el email usado para generar el token.
     */
    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken(testEmail);

        String extractedEmail = jwtUtil.extractUsername(token);

        assertEquals(testEmail, extractedEmail);
    }

    /**
     * Prueba la extracción correcta de la fecha de expiración del token.
     * 
     * Verifica que:
     * - La fecha de expiración no sea nula
     * - Sea una fecha futura (posterior al momento de generación)
     * - Esté aproximadamente a 1 hora de distancia (con tolerancia de 5 segundos)
     */
    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        String token = jwtUtil.generateToken(testEmail);
        Date beforeGeneration = new Date();

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(beforeGeneration));
        
        long expectedExpiration = beforeGeneration.getTime() + 3600000;
        long actualExpiration = expiration.getTime();
        long difference = Math.abs(expectedExpiration - actualExpiration);
        assertTrue(difference < 5000);
    }

    /**
     * Prueba la validación de expiración para tokens válidos (no expirados).
     * 
     * Verifica que un token recién generado no esté marcado como expirado.
     */
    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        String validToken = jwtUtil.generateToken("testuser");
        
        assertFalse(jwtUtil.isTokenExpired(validToken));
    }
    
    private String createExpiredToken() {
                Long originalExpiration = testExpiration;
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        
        String token = jwtUtil.generateToken("test@example.com");
        
        ReflectionTestUtils.setField(jwtUtil, "expiration", originalExpiration);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return token;
    }

    /**
     * Prueba el comportamiento con tokens expirados.
     * 
     * Verifica que al intentar verificar la expiración de un token
     * que ya ha expirado, se lance la excepción ExpiredJwtException.
     */
    @Test
    void isTokenExpired_WithExpiredToken_ShouldThrowExpiredException() {
        String expiredToken = createExpiredToken();
        
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.isTokenExpired(expiredToken);
        });
    }

    /**
     * Prueba la validación exitosa de token con email correcto.
     * 
     * Verifica que un token válido sea correctamente validado
     * cuando se proporciona el email correcto que coincide
     * con el subject del token.
     */
    @Test
    void validateToken_WithValidTokenAndCorrectEmail_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(testEmail);

        Boolean isValid = jwtUtil.validateToken(token, testEmail);

        assertTrue(isValid);
    }

    /**
     * Prueba la validación fallida de token con email incorrecto.
     * 
     * Verifica que un token válido sea rechazado cuando se
     * proporciona un email diferente al que fue usado para
     * generar el token.
     */
    @Test
    void validateToken_WithValidTokenAndIncorrectEmail_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(testEmail);
        String wrongEmail = "wrong@example.com";

        Boolean isValid = jwtUtil.validateToken(token, wrongEmail);

        assertFalse(isValid);
    }

    /**
     * Prueba la validación de tokens expirados.
     * 
     * Verifica que al intentar validar un token que ha expirado
     * se lance la excepción ExpiredJwtException en lugar de
     * retornar false.
     */
    @Test
    void validateToken_WithExpiredToken_ShouldThrowExpiredJwtException() {
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 1L);
        
        String token = shortExpirationJwtUtil.generateToken(testEmail);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            shortExpirationJwtUtil.validateToken(token, testEmail);
        });
    }

    /**
     * Prueba la validación general de tokens válidos.
     * 
     * Verifica que el método isValidToken retorne true
     * para tokens correctamente generados y no expirados.
     */
    @Test
    void isValidToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(testEmail);

        Boolean isValid = jwtUtil.isValidToken(token);

        assertTrue(isValid);
    }

    /**
     * Prueba el manejo de tokens malformados.
     * 
     * Verifica que el método isValidToken retorne false
     * de manera segura cuando se proporciona un token
     * con formato inválido, sin lanzar excepciones.
     */
    @Test
    void isValidToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "invalid.token.format";

        Boolean isValid = jwtUtil.isValidToken(malformedToken);

        assertFalse(isValid);
    }

    /**
     * Prueba el manejo de tokens nulos.
     * 
     * Verifica que el método isValidToken maneje de manera
     * segura los valores null, retornando false sin lanzar
     * NullPointerException.
     */
    @Test
    void isValidToken_WithNullToken_ShouldReturnFalse() {
        Boolean isValid = jwtUtil.isValidToken(null);

        assertFalse(isValid);
    }

    /**
     * Prueba el manejo de tokens vacíos.
     * 
     * Verifica que el método isValidToken retorne false
     * cuando se proporciona una cadena vacía como token.
     */
    @Test
    void isValidToken_WithEmptyToken_ShouldReturnFalse() {
        Boolean isValid = jwtUtil.isValidToken("");

        assertFalse(isValid);
    }

    /**
     * Prueba la validación de tokens expirados mediante isValidToken.
     * 
     * Verifica que el método isValidToken retorne false para
     * tokens que han expirado, manejando la excepción internamente
     * y proporcionando una respuesta segura.
     */
    @Test
    void isValidToken_WithExpiredToken_ShouldReturnFalse() {
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 1L);
        
        String token = shortExpirationJwtUtil.generateToken(testEmail);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isValid = jwtUtil.isValidToken(token);

        assertFalse(isValid);
    }

    /**
     * Prueba la extracción de claims personalizados.
     * 
     * Verifica que el método extractClaim pueda extraer
     * información específica del token usando un resolver
     * function personalizado (en este caso, la fecha de emisión).
     */
    @Test
    void extractClaim_WithCustomClaim_ShouldWork() {
        String token = jwtUtil.generateToken(testEmail);

        Date issuedAt = jwtUtil.extractClaim(token, claims -> claims.getIssuedAt());

        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()));
    }

    /**
     * Prueba el manejo de errores al extraer username de tokens inválidos.
     * 
     * Verifica que se lance MalformedJwtException cuando se intenta
     * extraer el username de un token con formato inválido.
     */
    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token";

        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void extractExpiration_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token";

        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractExpiration(invalidToken);
        });
    }

    /**
     * Prueba la generación de tokens únicos para diferentes usuarios.
     * 
     * Verifica que:
     * - Emails diferentes generen tokens diferentes
     * - Cada token contenga el email correcto como subject
     * - Los tokens sean únicos incluso generados al mismo tiempo
     */
    @Test
    void generateToken_WithDifferentEmails_ShouldCreateDifferentTokens() {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        String token1 = jwtUtil.generateToken(email1);
        String token2 = jwtUtil.generateToken(email2);

        assertNotEquals(token1, token2);
        assertEquals(email1, jwtUtil.extractUsername(token1));
        assertEquals(email2, jwtUtil.extractUsername(token2));
    }

    /**
     * Prueba la generación de tokens únicos para el mismo usuario.
     * 
     * Verifica que múltiples llamadas al método generateToken
     * con el mismo email generen tokens diferentes debido a
     * diferentes timestamps de emisión (iat claim).
     */
    @Test
    void generateToken_CalledMultipleTimes_ShouldCreateDifferentTokens() {
        String token1 = jwtUtil.generateToken(testEmail);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtUtil.generateToken(testEmail);

        assertNotEquals(token1, token2);
        assertEquals(testEmail, jwtUtil.extractUsername(token1));
        assertEquals(testEmail, jwtUtil.extractUsername(token2));
    }
}