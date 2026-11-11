package jro.pry.PasarelaPagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jro.pry.PasarelaPagos.config.JwtUtil;
import jro.pry.PasarelaPagos.dto.JwtResponseDTO;
import jro.pry.PasarelaPagos.dto.LoginRequestDTO;
import jro.pry.PasarelaPagos.dto.UserRequestDTO;
import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Operation(
            summary = "Registrar usuario",
            description = "Crea un usuario con rol USER y una billetera USD en 0.00. Devuelve un JWT.")
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO createdUser = userService.create(request);
        String token = jwtUtil.generateToken(createdUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponseDTO(token));
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // Autenticamos al usuario con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Si la autenticación es exitosa, generamos el token JWT
        String token = jwtUtil.generateToken(request.getEmail());

        return ResponseEntity.ok(new JwtResponseDTO(token));
    }
}
