package jro.pry.PasarelaPagos.controller;

import jro.pry.PasarelaPagos.config.JwtUtil;
import jro.pry.PasarelaPagos.dto.JwtResponseDTO;
import jro.pry.PasarelaPagos.dto.UserRequestDTO;
import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldRegisterUserAndReturnToken() {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("new@demo.com");
        request.setPassword("Demo1234!");

        UserResponseDTO createdUser = new UserResponseDTO(
                9L,
                "new@demo.com",
                "USER",
                List.of()
        );
        when(userService.create(request)).thenReturn(createdUser);
        when(jwtUtil.generateToken("new@demo.com")).thenReturn("jwt-token");

        ResponseEntity<JwtResponseDTO> response = authController.register(request);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("jwt-token", response.getBody().getToken());
        verify(userService).create(request);
    }
}
