package jro.pry.PasarelaPagos.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldPrefixRoleOnlyOnce() {
        User user = new User();

        user.setRole("USER");
        Assertions.assertEquals("ROLE_USER", user.getAuthorities().iterator().next().getAuthority());

        user.setRole("ROLE_ADMIN");
        Assertions.assertEquals("ROLE_ADMIN", user.getAuthorities().iterator().next().getAuthority());
    }
}
