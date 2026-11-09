package jro.pry.PasarelaPagos.exception;

import jro.pry.PasarelaPagos.dto.ErrorResponseDTO;
import jro.pry.PasarelaPagos.dto.LoginRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundForResourceNotFound() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Usuario no encontrado"));

        Assertions.assertEquals(404, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Usuario no encontrado", response.getBody().getMessage());
    }

    @Test
    void shouldReturnConflictForConflictException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleConflictException(
                new ConflictException("Ya existe una transacción con esta referencia"));

        Assertions.assertEquals(409, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Ya existe una transacción con esta referencia", response.getBody().getMessage());
    }

    @Test
    void shouldReturnConflictForDataIntegrityViolation() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrityViolationException(
                new DataIntegrityViolationException("duplicate key"));

        Assertions.assertEquals(409, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Ya existe un registro con esos datos", response.getBody().getMessage());
    }

    @Test
    void shouldReturnBadRequestForBusinessException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleBusinessException(
                new BusinessException("Fondos insuficientes"));

        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Fondos insuficientes", response.getBody().getMessage());
    }

    @Test
    void shouldReturnUnauthorizedForAuthenticationException() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleAuthenticationException(
                new BadCredentialsException("bad credentials"));

        Assertions.assertEquals(401, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Credenciales inválidas", response.getBody().getMessage());
    }

    @Test
    void shouldReturnForbiddenWithAccessDeniedMessage() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleAccessDeniedException(
                new AccessDeniedException("No puedes transferir desde una billetera que no te pertenece"));

        Assertions.assertEquals(403, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("No puedes transferir desde una billetera que no te pertenece",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnBadRequestWithValidationMessages() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new LoginRequestDTO(), "loginRequestDTO");
        bindingResult.rejectValue("email", "Email", "El email no tiene un formato válido");

        MethodParameter methodParameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodArgumentNotValidException(exception);

        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().getMessage().contains("email"));
        Assertions.assertTrue(response.getBody().getMessage().contains("El email no tiene un formato válido"));
    }
}
