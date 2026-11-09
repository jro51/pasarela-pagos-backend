package jro.pry.PasarelaPagos.exception;

import jro.pry.PasarelaPagos.dto.ErrorResponseDTO;
import jro.pry.PasarelaPagos.dto.LoginRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnBadRequestWithValidationMessages() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new LoginRequestDTO(), "loginRequestDTO");
        bindingResult.rejectValue("email", "Email", "El email no tiene un formato válido");

        MethodParameter methodParameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodArgumentNotValidException(exception);

        Assertions.assertEquals(400, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().getMessage().contains("email"));
        Assertions.assertTrue(response.getBody().getMessage().contains("El email no tiene un formato válido"));
    }
}
