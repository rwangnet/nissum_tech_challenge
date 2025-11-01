package cl.rwangnet.nissum_technical_challenge.service;

import cl.rwangnet.nissum_technical_challenge.dto.*;
import cl.rwangnet.nissum_technical_challenge.exception.UserAlreadyExistsException;
import cl.rwangnet.nissum_technical_challenge.model.Phone;
import cl.rwangnet.nissum_technical_challenge.model.User;
import cl.rwangnet.nissum_technical_challenge.repository.UserRepository;
import cl.rwangnet.nissum_technical_challenge.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para la gestión de usuarios.
 * 
 * Encapsula todas las operaciones relacionadas con usuarios incluyendo
 * registro, consulta, validación y eliminación. Maneja la conversión
 * entre entidades y DTOs, así como la integración con JWT y encriptación.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    /** Repositorio para operaciones de persistencia de usuarios */
    private final UserRepository userRepository;
    
    /** Encoder para encriptación de contraseñas usando BCrypt */
    private final PasswordEncoder passwordEncoder;
    
    /** Utilidad para generación y manejo de tokens JWT */
    private final JwtUtil jwtUtil;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * Realiza las siguientes operaciones:
     * - Valida que el email no esté registrado previamente
     * - Encripta la contraseña usando BCrypt
     * - Genera un token JWT para el usuario
     * - Crea las entidades User y Phone asociadas
     * - Persiste en base de datos con timestamps automáticos
     * 
     * @param request Datos del usuario a registrar
     * @return UserResponse con datos del usuario creado y token JWT
     * @throws UserAlreadyExistsException si el email ya existe en el sistema
     */
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El correo ya registrado");
        }

        List<Phone> phones = request.getPhones().stream()
                .map(phoneRequest -> Phone.builder()
                        .number(phoneRequest.getNumber())
                        .cityCode(phoneRequest.getCitycode())
                        .countryCode(phoneRequest.getContrycode())
                        .build())
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .token(token)
                .phones(phones)
                .build();

        phones.forEach(phone -> phone.setUser(user));

        User savedUser = userRepository.save(user);

        return convertToUserResponse(savedUser);
    }

    /**
     * Convierte una entidad User a UserResponse DTO.
     * 
     * Realiza el mapeo completo incluyendo la conversión de teléfonos
     * asociados y todos los metadatos del usuario.
     * 
     * @param user Entidad User a convertir
     * @return UserResponse con todos los datos del usuario
     */
    private UserResponse convertToUserResponse(User user) {
        List<PhoneResponse> phoneResponses = user.getPhones().stream()
                .map(phone -> PhoneResponse.builder()
                        .id(phone.getId())
                        .number(phone.getNumber())
                        .citycode(phone.getCityCode())
                        .contrycode(phone.getCountryCode())
                        .build())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phones(phoneResponses)
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.getIsActive())
                .build();
    }

    /**
     * Busca y retorna un usuario por su email.
     * 
     * @param email Email del usuario a buscar
     * @return UserResponse con datos completos del usuario
     * @throws RuntimeException si el usuario no existe
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<PhoneResponse> phoneResponses = user.getPhones().stream()
                .map(phone -> PhoneResponse.builder()
                        .id(phone.getId())
                        .number(phone.getNumber())
                        .citycode(phone.getCityCode())
                        .contrycode(phone.getCountryCode())
                        .build())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phones(phoneResponses)
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.getIsActive())
                .build();
    }

    /**
     * Obtiene la lista completa de usuarios registrados.
     * 
     * @return Lista de UserResponse con todos los usuarios del sistema
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    List<PhoneResponse> phoneResponses = user.getPhones().stream()
                            .map(phone -> PhoneResponse.builder()
                                    .id(phone.getId())
                                    .number(phone.getNumber())
                                    .citycode(phone.getCityCode())
                                    .contrycode(phone.getCountryCode())
                                    .build())
                            .collect(Collectors.toList());

                    return UserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phones(phoneResponses)
                            .created(user.getCreated())
                            .modified(user.getModified())
                            .lastLogin(user.getLastLogin())
                            .token(user.getToken())
                            .isActive(user.getIsActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Elimina un usuario del sistema por su email.
     * 
     * La eliminación es en cascada, removiendo también todos los teléfonos
     * asociados gracias a la configuración JPA.
     * 
     * @param email Email del usuario a eliminar
     * @throws RuntimeException si el usuario no existe
     */
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        userRepository.delete(user);
    }
}