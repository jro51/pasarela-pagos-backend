package jro.pry.PasarelaPagos.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class RestAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);

    @Test
    void shouldReturnUnauthorizedWhenNoTokenWasProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("no token"));

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
        Assertions.assertTrue(response.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        Assertions.assertEquals(401, body.get("status").asInt());
        Assertions.assertEquals("Unauthorized", body.get("error").asText());
        Assertions.assertEquals("UNAUTHORIZED", body.get("code").asText());
        Assertions.assertEquals("No autorizado", body.get("message").asText());
        Assertions.assertEquals(0, body.get("fieldErrors").size());
    }

    @Test
    void shouldReturnUnauthorizedWithCustomMessageForInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE, "Token inválido o expirado");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("invalid token"));

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        Assertions.assertEquals(401, body.get("status").asInt());
        Assertions.assertEquals("UNAUTHORIZED", body.get("code").asText());
        Assertions.assertEquals("Token inválido o expirado", body.get("message").asText());
    }
}
