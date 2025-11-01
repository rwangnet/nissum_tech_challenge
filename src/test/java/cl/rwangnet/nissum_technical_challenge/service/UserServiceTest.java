package cl.rwangnet.nissum_technical_challenge.service;

import cl.rwangnet.nissum_technical_challenge.dto.PhoneRequest;
import cl.rwangnet.nissum_technical_challenge.dto.UserRegistrationRequest;
import cl.rwangnet.nissum_technical_challenge.dto.UserResponse;
import cl.rwangnet.nissum_technical_challenge.exception.UserAlreadyExistsException;
import cl.rwangnet.nissum_technical_challenge.model.User;
import cl.rwangnet.nissum_technical_challenge.repository.UserRepository;
import cl.rwangnet.nissum_technical_challenge.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase UserService.
 * 
 * Valida la lógica de negocio del servicio de usuarios incluyendo
 * registro de usuarios, validaciones, manejo de excepciones y
 * integración con repositorios y servicios externos.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        PhoneRequest phoneRequest = PhoneRequest.builder()
                .number("1234567")
                .citycode("1")
                .contrycode("57")
                .build();

        validRequest = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("hunter2")
                .phones(List.of(phoneRequest))
                .build();
    }

    /**
     * Prueba el registro exitoso de un nuevo usuario.
     * 
     * Verifica que:
     * - Se valide que el email no esté registrado previamente
     * - Se encripte la contraseña correctamente
     * - Se genere un token JWT
     * - Se creen las entidades User y Phone correctamente
     * - Se retorne un UserResponse con todos los datos esperados
     * - Se configuren correctamente los metadatos (timestamps, estado activo)
     */
    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt_token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(java.util.UUID.randomUUID());
            user.setCreated(java.time.LocalDateTime.now());
            user.setModified(java.time.LocalDateTime.now());
            user.setLastLogin(java.time.LocalDateTime.now());
            user.setIsActive(true);
            return user;
        });

        UserResponse response = userService.registerUser(validRequest);

        assertNotNull(response);
        assertEquals(validRequest.getName(), response.getName());
        assertEquals(validRequest.getEmail(), response.getEmail());
        assertEquals("jwt_token", response.getToken());
        assertTrue(response.getIsActive());
        assertNotNull(response.getId());
        assertNotNull(response.getCreated());
        assertNotNull(response.getModified());
        assertNotNull(response.getLastLogin());
        assertEquals(1, response.getPhones().size());

        verify(userRepository).existsByEmail(validRequest.getEmail());
        verify(passwordEncoder).encode(validRequest.getPassword());
        verify(jwtUtil).generateToken(validRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Prueba el manejo de error cuando el email ya está registrado.
     * 
     * Verifica que:
     * - Se lance UserAlreadyExistsException cuando el email ya existe
     * - Se retorne el mensaje de error correcto
     * - No se ejecuten operaciones de encriptación, generación de token o persistencia
     * - Solo se verifique la existencia del email en el repositorio
     */
    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(validRequest)
        );

        assertEquals("El correo ya registrado", exception.getMessage());
        verify(userRepository).existsByEmail(validRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}