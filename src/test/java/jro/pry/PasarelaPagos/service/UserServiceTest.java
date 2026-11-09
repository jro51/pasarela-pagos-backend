package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.UserRequestDTO;
import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.dto.UserUpdateDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.UserRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnAllUser(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("example1@test.com");
        user1.setRole("USER");
        user1.setWallets(new ArrayList<>());

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("example2@test.com");
        user2.setRole("USER");
        user2.setWallets(new ArrayList<>());

        List<User> usersEntities = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(usersEntities);

        List<UserResponseDTO> response = userService.findAll();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.size());
        Assertions.assertEquals("example1@test.com", response.get(0).getEmail());
        Assertions.assertEquals("example2@test.com", response.get(1).getEmail());
    }

    @Test
    void shouldReturnUserWhenFindById(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("example@test.com");
        user1.setRole("USER");
        user1.setWallets(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        UserResponseDTO response = userService.findById(user1.getId());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("example@test.com", response.getEmail());
    }

    @Test
    void shouldCreateUserWithHashedPasswordAndUserRole(){
        UserRequestDTO request = new UserRequestDTO();
        request.setEmail("example@test.com");
        request.setPassword("123456");

        when(passwordEncoder.encode("123456")).thenReturn("password-hasheado");
        when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(walletRepository.save(ArgumentMatchers.any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            wallet.setId(10L);
            return wallet;
        });

        UserResponseDTO response = userService.create(request);

        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("example@test.com", response.getEmail());
        Assertions.assertEquals("USER", response.getRole());
        Assertions.assertEquals(1, response.getWallets().size());
        Assertions.assertEquals(10L, response.getWallets().get(0).getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        Assertions.assertEquals("password-hasheado", userCaptor.getValue().getPassword());
        Assertions.assertEquals("USER", userCaptor.getValue().getRole());
    }

    @Test
    void shouldUpdateUserAndHashNewPassword(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("example@test.com");
        user1.setPassword("hash-viejo");
        user1.setRole("USER");
        user1.setWallets(new ArrayList<>());

        UserUpdateDTO request = new UserUpdateDTO();
        request.setEmail("exampleTest@test.com");
        request.setPassword("222222");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(passwordEncoder.encode("222222")).thenReturn("hash-nuevo");
        when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.update(request, 1L);

        Assertions.assertEquals("exampleTest@test.com", result.getEmail());
        Assertions.assertEquals("USER", result.getRole());
        Assertions.assertEquals("hash-nuevo", user1.getPassword());
        verify(passwordEncoder).encode("222222");
    }

    @Test
    void shouldKeepPasswordWhenUpdateDoesNotProvideOne(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("example@test.com");
        user1.setPassword("hash-viejo");
        user1.setRole("USER");
        user1.setWallets(new ArrayList<>());

        UserUpdateDTO request = new UserUpdateDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(request, 1L);

        Assertions.assertEquals("hash-viejo", user1.getPassword());
        verify(passwordEncoder, never()).encode(ArgumentMatchers.anyString());
    }

    @Test
    void shouldDeleteUserSuccessfully(){
        User user1 = new User();
        user1.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        userService.delete(user1.getId());

        verify(userRepository, times(1)).delete(user1);
    }
}
