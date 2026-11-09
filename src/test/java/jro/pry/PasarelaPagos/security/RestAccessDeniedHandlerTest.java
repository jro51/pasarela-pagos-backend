package jro.pry.PasarelaPagos.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class RestAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler(objectMapper);

    @Test
    void shouldReturnForbiddenAsJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("forbidden"));

        Assertions.assertEquals(403, response.getStatus());

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        Assertions.assertEquals(403, body.get("status").asInt());
        Assertions.assertEquals("Forbidden", body.get("error").asText());
        Assertions.assertEquals("No tiene permisos para realizar esta accion", body.get("message").asText());
    }
}
