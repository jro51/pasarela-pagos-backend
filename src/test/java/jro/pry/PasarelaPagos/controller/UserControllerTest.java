package jro.pry.PasarelaPagos.controller;

import jro.pry.PasarelaPagos.dto.UserRequestDTO;
import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnAuthenticatedUserProfile() {
        User principal = new User();
        principal.setId(7L);
        principal.setEmail("alice@demo.com");
        principal.setRole("USER");

        UserResponseDTO expected = new UserResponseDTO(
                7L,
                "alice@demo.com",
                "USER",
                List.of(new WalletResponseDTO(10L, new BigDecimal("2500.00"), "USD"))
        );
        when(userService.findById(7L)).thenReturn(expected);

        ResponseEntity<UserResponseDTO> response = userController.findCurrentUser(principal);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertSame(expected, response.getBody());
        verify(userService).findById(7L);
    }

    @Test
    void shouldCreateUser() {
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("new@demo.com");
        request.setPassword("Demo1234!");

        UserResponseDTO expected = new UserResponseDTO(
                8L,
                "new@demo.com",
                "USER",
                List.of()
        );
        when(userService.create(request)).thenReturn(expected);

        ResponseEntity<UserResponseDTO> response = userController.createUser(request);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertSame(expected, response.getBody());
        verify(userService).create(request);
    }
}
