package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.UserRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> findAll(){

        return userRepository.findAll().stream().map(user -> {
            List<WalletResponseDTO> walletDTOs = user.getWallets().stream()
                    .map(w -> new WalletResponseDTO(w.getId(), w.getBalance(), w.getCurrency())).toList();

            return new UserResponseDTO(user.getId(), user.getEmail(), user.getRole(), walletDTOs);
        }).toList();
    }

    public UserResponseDTO findById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<WalletResponseDTO> walletDTOs = user.getWallets().stream()
                .map(w -> new WalletResponseDTO(w.getId(), w.getBalance(), w.getCurrency())).toList();

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getRole(), walletDTOs);
    }

    public User create(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(user);

        Wallet newWallet = new Wallet();
        newWallet.setUser(newUser);
        newWallet.setBalance(BigDecimal.ZERO);
        newWallet.setCurrency("USD");
        walletRepository.save(newWallet);

        return newUser;
    }

    public User update(User user, Long id){
        User userExist = userRepository.findById(id).orElseThrow(() -> {
            throw new RuntimeException("No se encontró al usuario");
        });

        userExist.setEmail(user.getEmail());
        userExist.setPassword(user.getPassword());
        userExist.setRole(user.getRole());

        return userRepository.save(userExist);
    }

    public void delete(Long id){
        User userExist = userRepository.findById(id).orElseThrow(() -> {
            throw new RuntimeException("No se encontró al usuario");
        });
        userRepository.delete(userExist);
    }
}
