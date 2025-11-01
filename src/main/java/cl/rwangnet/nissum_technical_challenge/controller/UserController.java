package cl.rwangnet.nissum_technical_challenge.controller;

import cl.rwangnet.nissum_technical_challenge.dto.UserRegistrationRequest;
import cl.rwangnet.nissum_technical_challenge.dto.UserResponse;
import cl.rwangnet.nissum_technical_challenge.service.UserService;
import cl.rwangnet.nissum_technical_challenge.service.ErrorHandlingService;
import cl.rwangnet.nissum_technical_challenge.exception.UserAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * 
 * Proporciona endpoints para el registro, consulta y eliminación de usuarios.
 * Incluye operaciones protegidas que requieren autenticación JWT y operaciones
 * públicas como el registro de nuevos usuarios.
 * 
 * @author Ricardo Wangnet
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API para la gestión de usuarios")
public class UserController {

    /** Servicio de lógica de negocio para operaciones de usuario */
    private final UserService userService;
    
    /** Servicio para manejo centralizado de errores y respuestas HTTP */
    private final ErrorHandlingService errorHandlingService;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * Endpoint público que permite crear una nueva cuenta de usuario. Valida
     * los datos de entrada, verifica que el correo no esté registrado previamente
     * y genera un token JWT para el usuario creado.
     * 
     * @param request Datos del usuario a registrar (nombre, email, contraseña, teléfonos)
     * @param bindingResult Resultado de la validación de Bean Validation
     * @return ResponseEntity con UserResponse (201) o mensaje de error (400/409)
     * @throws UserAlreadyExistsException si el correo ya está registrado
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return errorHandlingService.handleValidationErrors(bindingResult);
        }
        
        try {
            UserResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserAlreadyExistsException ex) {
            return errorHandlingService.handleUserAlreadyExists(ex);
        } catch (Exception ex) {
            return errorHandlingService.handleGenericError(ex);
        }
    }

    /**
     * Obtiene el perfil del usuario autenticado.
     * 
     * Endpoint protegido que requiere autenticación JWT. Retorna la información
     * completa del usuario basándose en el email extraído del token JWT.
     * 
     * @param authentication Contexto de autenticación de Spring Security
     * @return ResponseEntity con los datos del usuario autenticado
     */
    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil de usuario", 
               description = "Endpoint protegido que retorna la información del usuario autenticado. Requiere token JWT en el header Authorization.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "Token JWT requerido - use 'Bearer {token}' en Authorization header"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Obtiene la lista de todos los usuarios registrados.
     * 
     * Endpoint protegido diseñado principalmente para testing y administración.
     * Requiere autenticación JWT válida para acceder.
     * 
     * @return ResponseEntity con lista de todos los usuarios del sistema
     */
    @GetMapping("/all")
    @Operation(summary = "Listar todos los usuarios", 
               description = "Endpoint protegido para obtener lista de todos los usuarios (solo para testing). Requiere token JWT.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "Token JWT requerido - use 'Bearer {token}' en Authorization header")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Elimina la cuenta del usuario autenticado.
     * 
     * Endpoint protegido que permite al usuario eliminar su propia cuenta.
     * La eliminación es permanente e incluye todos los datos asociados como
     * teléfonos y información de perfil.
     * 
     * @param authentication Contexto de autenticación de Spring Security
     * @return ResponseEntity con mensaje de confirmación de eliminación
     */
    @DeleteMapping("/profile")
    @Operation(summary = "Eliminar cuenta de usuario", 
               description = "Endpoint protegido para eliminar la cuenta del usuario autenticado. Requiere token JWT.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "Token JWT requerido - use 'Bearer {token}' en Authorization header"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<String> deleteUserProfile(Authentication authentication) {
        String email = authentication.getName();
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok("Usuario eliminado exitosamente");
    }
}