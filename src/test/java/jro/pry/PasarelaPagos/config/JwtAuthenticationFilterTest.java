package jro.pry.PasarelaPagos.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jro.pry.PasarelaPagos.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldMarkInvalidTokenAndContinueWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] chainCalled = {false};
        FilterChain filterChain = (req, res) -> chainCalled[0] = true;

        when(jwtUtil.extractEmail("invalid-token")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, filterChain);

        Assertions.assertEquals("Token inválido o expirado",
                request.getAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE));
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        Assertions.assertTrue(chainCalled[0]);
    }

    @Test
    void shouldMarkInvalidTokenWhenUserDoesNotExist() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] chainCalled = {false};
        FilterChain filterChain = (req, res) -> chainCalled[0] = true;

        when(jwtUtil.extractEmail("valid-token")).thenReturn("missing@example.com");
        when(userDetailsService.loadUserByUsername("missing@example.com"))
                .thenThrow(new UsernameNotFoundException("not found"));

        filter.doFilter(request, response, filterChain);

        Assertions.assertEquals("Token inválido o expirado",
                request.getAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE));
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        Assertions.assertTrue(chainCalled[0]);
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] chainCalled = {false};
        FilterChain filterChain = (req, res) -> chainCalled[0] = true;

        UserDetails userDetails = User.withUsername("user@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtUtil.extractEmail("valid-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid-token", "user@example.com")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Assertions.assertEquals("user@example.com",
                SecurityContextHolder.getContext().getAuthentication().getName());
        Assertions.assertNull(request.getAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE));
        Assertions.assertTrue(chainCalled[0]);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenNoTokenIsProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] chainCalled = {false};
        FilterChain filterChain = (req, res) -> chainCalled[0] = true;

        filter.doFilter(request, response, filterChain);

        Assertions.assertNull(request.getAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE));
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        Assertions.assertTrue(chainCalled[0]);
    }
}
