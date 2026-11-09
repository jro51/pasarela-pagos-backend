package jro.pry.PasarelaPagos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    //Tiene que ser "user" porque asi es el nombre del atributo al cual se le está relacionando en
    //la clase Wallet (JPA utiliza reflexion de codigo o bueno Java Reflection)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Wallet> wallets;

    public User() {
    }

    public User(Long id, String email, String password, String role, List<Wallet> wallets) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.wallets = wallets;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = (role == null || role.isBlank()) ? "USER" : role;
        if (roleName.startsWith("ROLE_")) {
            roleName = roleName.substring(5);
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
