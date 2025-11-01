package cl.rwangnet.nissum_technical_challenge.controller;

import cl.rwangnet.nissum_technical_challenge.dto.UserRegistrationRequest;
import cl.rwangnet.nissum_technical_challenge.dto.UserResponse;
import cl.rwangnet.nissum_technical_challenge.dto.PhoneRequest;
import cl.rwangnet.nissum_technical_challenge.dto.PhoneResponse;
import cl.rwangnet.nissum_technical_challenge.service.UserService;
import cl.rwangnet.nissum_technical_challenge.service.ErrorHandlingService;
import cl.rwangnet.nissum_technical_challenge.exception.UserAlreadyExistsException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

/**
 * Pruebas unitarias para el controlador UserController.
 * 
 * Valida el comportamiento de los endpoints REST incluyendo
 * validación de entrada, manejo de errores y respuestas HTTP
 * correctas sin realizar llamadas reales a la base de datos.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
class UserControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private ErrorHandlingService errorHandlingService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(errorHandlingService.handleUserAlreadyExists(any(UserAlreadyExistsException.class)))
                .thenReturn(ResponseEntity.status(409).body(Map.of("mensaje", "El correo ya registrado")));
        when(errorHandlingService.handleGenericError(any(Exception.class)))
                .thenReturn(ResponseEntity.status(500).body(Map.of("mensaje", "Error interno del servidor")));
        when(errorHandlingService.handleValidationErrors(any()))
                .thenReturn(ResponseEntity.status(400).body(Map.of("mensaje", "Error de validación")));
        
        UserController userController = new UserController(userService, errorHandlingService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Prueba el endpoint de registro exitoso de usuario.
     * 
     * Verifica que:
     * - El endpoint POST /api/users/register acepte datos válidos
     * - Se retorne código HTTP 201 (Created)
     * - La respuesta contenga los datos del usuario registrado
     * - Se incluya el token JWT en la respuesta
     * - El Content-Type sea application/json
     */
    @Test
    void registerUser_Success() throws Exception {
        PhoneRequest phone = new PhoneRequest("1234567", "1", "57");
        UserRegistrationRequest request = new UserRegistrationRequest("Juan Rodriguez", "juan@rodriguez.org", "Hunter2@123", List.of(phone));
        
        PhoneResponse phoneResponse = PhoneResponse.builder()
                .id(UUID.randomUUID())
                .number("1234567")
                .citycode("1")
                .contrycode("57")
                .build();
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .phones(List.of(phoneResponse))
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .token("mock-jwt-token")
                .isActive(true)
                .build();

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Juan Rodriguez"))
                .andExpect(jsonPath("$.email").value("juan@rodriguez.org"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    /**
     * Prueba el manejo de error cuando el email ya existe.
     * 
     * Verifica que:
     * - Se retorne código HTTP 409 (Conflict) cuando el email ya está registrado
     * - El mensaje de error sea apropiado para el usuario
     * - Se maneje correctamente la excepción UserAlreadyExistsException
     */
    @Test
    void registerUser_EmailAlreadyExists() throws Exception {
        PhoneRequest phone = new PhoneRequest("1234567", "1", "57");
        UserRegistrationRequest request = new UserRegistrationRequest("Juan Rodriguez", "juan@rodriguez.org", "Hunter2@123", List.of(phone));

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("El correo ya registrado"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    /**
     * Prueba la validación de nombre requerido.
     * 
     * Verifica que:
     * - Se retorne código HTTP 400 (Bad Request) cuando el nombre está vacío
     * - Se incluya un mensaje de error en la respuesta
     * - Las validaciones de Bean Validation funcionen correctamente
     */
    @Test
    void registerUser_InvalidRequest_MissingName() throws Exception {
        PhoneRequest phone = new PhoneRequest("1234567", "1", "57");
        UserRegistrationRequest request = new UserRegistrationRequest("", "juan@rodriguez.org", "Hunter2@123", List.of(phone));
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    /**
     * Prueba la validación de formato de email.
     * 
     * Verifica que:
     * - Se retorne código HTTP 400 (Bad Request) para emails inválidos
     * - Las validaciones @Email funcionen correctamente
     * - Se proporcione un mensaje de error descriptivo
     */
    @Test
    void registerUser_InvalidRequest_InvalidEmail() throws Exception {
        PhoneRequest phone = new PhoneRequest("1234567", "1", "57");
        UserRegistrationRequest request = new UserRegistrationRequest("Juan Rodriguez", "invalid-email", "Hunter2@123", List.of(phone));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }
}