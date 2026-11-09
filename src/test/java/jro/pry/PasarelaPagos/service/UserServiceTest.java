package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.UserRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldCreatedUserSuccessfully(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("example@test.com");
        user1.setRole("USER");

        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(user1);

        when(walletRepository.save(ArgumentMatchers.any(Wallet.class))).thenReturn(new Wallet());

        User createdUser = userService.create(user1);

        Assertions.assertNotNull(createdUser);
        Assertions.assertEquals("example@test.com", createdUser.getEmail());
    }

    @Test
    void shouldUpdateUserSuccessfully(){
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("example@test.com");
        user1.setPassword("123456");
        user1.setRole("USER");

        User userUpdate = new User();
        userUpdate.setEmail("exampleTest@test.com");
        userUpdate.setPassword("22222");
        userUpdate.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(user1);

        User result = userService.update(userUpdate, user1.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals("exampleTest@test.com", result.getEmail());
        Assertions.assertEquals("22222", result.getPassword());
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