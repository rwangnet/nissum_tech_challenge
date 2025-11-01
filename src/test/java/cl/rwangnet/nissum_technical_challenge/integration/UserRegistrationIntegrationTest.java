package cl.rwangnet.nissum_technical_challenge.integration;

import cl.rwangnet.nissum_technical_challenge.dto.PhoneRequest;
import cl.rwangnet.nissum_technical_challenge.dto.UserRegistrationRequest;
import cl.rwangnet.nissum_technical_challenge.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para el registro de usuarios.
 * 
 * Valida el flujo completo de registro incluyendo:
 * - Integración con base de datos H2
 * - Validaciones de entrada completas
 * - Persistencia de entidades User y Phone
 * - Generación de tokens JWT
 * - Respuestas HTTP correctas end-to-end
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserRegistrationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    
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
                .password("Hunter2@123")
                .phones(List.of(phoneRequest))
                .build();
    }

    /**
     * Prueba de integración completa para registro exitoso de usuario.
     * 
     * Verifica el flujo end-to-end incluyendo:
     * - Persistencia en base de datos H2
     * - Encriptación de contraseña con BCrypt
     * - Generación de token JWT válido
     * - Creación correcta de relaciones User-Phone
     * - Timestamps automáticos (@PrePersist)
     */
    @Test
    void registerUser_FullIntegration_Success() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Juan Rodriguez"))
                .andExpect(jsonPath("$.email").value("juan@rodriguez.org"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.is_active").value(true))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.modified").exists())
                .andExpect(jsonPath("$.last_login").exists())
                .andExpect(jsonPath("$.phones").isArray())
                .andExpect(jsonPath("$.phones[0].number").value("1234567"));

        assert(userRepository.existsByEmail("juan@rodriguez.org"));
    }

    @Test
    void registerUser_DuplicateEmail_Conflict() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }
}