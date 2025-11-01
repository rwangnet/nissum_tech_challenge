package cl.rwangnet.nissum_technical_challenge.security;

import cl.rwangnet.nissum_technical_challenge.filter.JwtAuthenticationFilter;
import cl.rwangnet.nissum_technical_challenge.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el filtro de autenticación JWT.
 * 
 * Valida el comportamiento del filtro en diferentes escenarios:
 * - Tokens válidos e inválidos
 * - Headers de autorización correctos e incorrectos  
 * - Configuración del contexto de seguridad de Spring
 * - Manejo de errores y casos extremos
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validToken = "valid.jwt.token";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "jwtUtil", jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Prueba que el filtro permita requests sin header Authorization.
     * 
     * Verifica que:
     * - El filtro continúe con la cadena de filtros
     * - No se intente extraer información del token
     * - El contexto de seguridad permanezca sin autenticación
     */
    @Test
    @DisplayName("Debería procesar request sin Authorization header")
    void shouldProcessRequestWithoutAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Prueba el manejo de headers Authorization sin prefijo Bearer.
     * 
     * Verifica que:
     * - Se ignore headers que no tengan el formato "Bearer token"
     * - El filtro continúe normalmente sin procesar el token
     * - No se establezca autenticación en el contexto de seguridad
     */
    @Test
    @DisplayName("Debería procesar request con Authorization header sin Bearer prefix")
    void shouldProcessRequestWithoutBearerPrefix() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Prueba el procesamiento exitoso de requests con tokens JWT válidos.
     * 
     * Verifica que:
     * - Se extraiga correctamente el username del token
     * - Se valide el token contra el username extraído
     * - Se establezca la autenticación en el SecurityContext
     * - El filtro continúe con la cadena de filtros normalmente
     */
    @Test
    @DisplayName("Debería procesar request con token JWT válido")
    void shouldProcessRequestWithValidJwtToken() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(validToken)).thenReturn(testEmail);
        when(jwtUtil.validateToken(validToken, testEmail)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(validToken);
        verify(jwtUtil).validateToken(validToken, testEmail);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(testEmail);
    }

    /**
     * Prueba el manejo de requests con tokens JWT inválidos.
     * 
     * Verifica que:
     * - Se extraiga el username del token (aunque sea inválido)
     * - La validación del token falle apropiadamente
     * - NO se establezca autenticación en el SecurityContext
     * - El filtro continúe sin errores permitiendo otros mecanismos de auth
     */
    @Test
    @DisplayName("Debería procesar request con token JWT inválido")
    void shouldProcessRequestWithInvalidJwtToken() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(validToken)).thenReturn(testEmail);
        when(jwtUtil.validateToken(validToken, testEmail)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(validToken);
        verify(jwtUtil).validateToken(validToken, testEmail);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Prueba el comportamiento cuando el usuario ya está autenticado.
     * 
     * Verifica que:
     * - Se detecte que ya existe autenticación en el SecurityContext
     * - Se extraiga el username del token para verificación
     * - NO se realice validación adicional del token (optimización)
     * - Se preserve la autenticación existente sin modificaciones
     */
    @Test
    @DisplayName("Debería procesar request cuando el usuario ya está autenticado")
    void shouldProcessRequestWhenUserAlreadyAuthenticated() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(validToken)).thenReturn(testEmail);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(validToken);
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }

    /**
     * Prueba el manejo robusto de excepciones durante la extracción del username.
     * 
     * Verifica que:
     * - Se capture y maneje apropiadamente excepciones del JwtUtil
     * - NO se establezca autenticación cuando hay errores de parsing
     * - El filtro continúe sin interrumpir la cadena de filtros
     * - La aplicación no falle ante tokens malformados o corrompidos
     */
    @Test
    @DisplayName("Debería manejar excepción al extraer username del token")
    void shouldHandleExceptionWhenExtractingUsername() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(validToken)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(validToken);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Prueba el manejo de casos extremos con token vacío después de "Bearer ".
     * 
     * Verifica que:
     * - Se maneje apropiadamente el header "Bearer " sin token
     * - Se capture la excepción al intentar procesar token vacío
     * - NO se establezca autenticación con tokens vacíos
     * - El filtro continúe sin errores ante inputs malformados
     */
    @Test
    @DisplayName("Debería procesar request con token vacío después de Bearer")
    void shouldProcessRequestWithEmptyTokenAfterBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtUtil.extractUsername("")).thenThrow(new RuntimeException("Empty token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername("");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Prueba el manejo de headers Authorization con solo "Bearer" sin token.
     * 
     * Verifica que:
     * - Se detecte apropiadamente la ausencia de token después de "Bearer"
     * - NO se intente procesar un token inexistente
     * - NO se establezca autenticación cuando falta el token
     * - Se mantenga la robustez ante headers malformados
     */
    @Test
    @DisplayName("Debería procesar request con Bearer pero sin token")
    void shouldProcessRequestWithBearerButNoToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Prueba la limpieza del SecurityContext cuando ocurren excepciones durante la validación.
     * 
     * Verifica que:
     * - Se capture y maneje excepciones durante la validación del token
     * - Se mantenga limpio el SecurityContext ante errores de validación
     * - NO se establezca autenticación parcial o inconsistente
     * - El filtro sea resiliente ante fallas del servicio de validación
     */
    @Test
    @DisplayName("Debería limpiar SecurityContext cuando hay excepción en validación")
    void shouldClearSecurityContextOnValidationException() throws ServletException, IOException {
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(validToken)).thenReturn(testEmail);
        when(jwtUtil.validateToken(validToken, testEmail)).thenThrow(new RuntimeException("Validation error"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(validToken);
        verify(jwtUtil).validateToken(validToken, testEmail);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}