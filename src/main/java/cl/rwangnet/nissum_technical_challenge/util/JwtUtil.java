package cl.rwangnet.nissum_technical_challenge.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para manejo de tokens JWT (JSON Web Tokens).
 * 
 * Proporciona funcionalidades completas para la gestión de tokens JWT incluyendo:
 * - Generación de tokens con claims personalizados
 * - Validación de tokens y verificación de expiración
 * - Extracción de información del usuario desde el token
 * - Manejo seguro de claves de firma usando HMAC-SHA
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Component
public class JwtUtil {

    /** Clave secreta para firma de tokens JWT desde application.properties */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración en milisegundos desde application.properties */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Genera la clave de firma para tokens JWT.
     * 
     * Convierte el secreto configurado en una SecretKey usando HMAC-SHA256
     * para garantizar la integridad y autenticidad de los tokens.
     * 
     * @return SecretKey para firma de tokens JWT
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un nuevo token JWT para un usuario.
     * 
     * Crea un token con el email como subject y timestamps automáticos
     * de emisión y expiración basados en la configuración.
     * 
     * @param email Email del usuario para incluir en el token
     * @return String con el token JWT firmado
     */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    /**
     * Construye y firma un token JWT con claims específicos.
     * 
     * @param claims Map con claims adicionales a incluir en el token
     * @param subject Subject del token (generalmente el email del usuario)
     * @return String con el token JWT completo y firmado
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el username (email) del token JWT.
     * 
     * @param token Token JWT del cual extraer el username
     * @return String con el email del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     * 
     * @param token Token JWT del cual extraer la expiración
     * @return Date con la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token usando un resolver function.
     * 
     * @param <T> Tipo del claim a extraer
     * @param token Token JWT del cual extraer el claim
     * @param claimsResolver Function para resolver el claim específico
     * @return Valor del claim solicitado
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT verificando su firma.
     * 
     * @param token Token JWT a parsear
     * @return Claims con toda la información del token
     * @throws io.jsonwebtoken.JwtException si el token es inválido o está corrupto
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si un token JWT ha expirado.
     * 
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false en caso contrario
     * @throws io.jsonwebtoken.ExpiredJwtException si el token ha expirado
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida un token JWT contra un email específico.
     * 
     * Verifica que el token contenga el email correcto y no haya expirado.
     * 
     * @param token Token JWT a validar
     * @param email Email esperado en el token
     * @return true si el token es válido para el email dado
     */
    public Boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }

    /**
     * Verifica la validez general de un token JWT.
     * 
     * Método seguro que captura excepciones y retorna false si hay
     * cualquier problema con el token (expirado, malformado, etc.).
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido, false en cualquier error
     */
    public Boolean isValidToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}