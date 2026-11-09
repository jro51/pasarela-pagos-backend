package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.UserRequestDTO;
import jro.pry.PasarelaPagos.dto.UserResponseDTO;
import jro.pry.PasarelaPagos.dto.UserUpdateDTO;
import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
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

    private static final String DEFAULT_ROLE = "USER";
    private static final String DEFAULT_CURRENCY = "USD";

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> findAll(){
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponseDTO findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return toResponse(user);
    }

    public UserResponseDTO create(UserRequestDTO request){
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(DEFAULT_ROLE);

        User newUser = userRepository.save(user);

        Wallet newWallet = new Wallet();
        newWallet.setUser(newUser);
        newWallet.setBalance(BigDecimal.ZERO);
        newWallet.setCurrency(DEFAULT_CURRENCY);
        Wallet savedWallet = walletRepository.save(newWallet);

        return new UserResponseDTO(
                newUser.getId(),
                newUser.getEmail(),
                newUser.getRole(),
                List.of(new WalletResponseDTO(savedWallet.getId(), savedWallet.getBalance(), savedWallet.getCurrency()))
        );
    }

    public UserResponseDTO update(UserUpdateDTO request, Long id){
        User userExist = userRepository.findById(id).orElseThrow(() -> {
            throw new ResourceNotFoundException("Usuario no encontrado");
        });

        userExist.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            userExist.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(userExist));
    }

    public void delete(Long id){
        User userExist = userRepository.findById(id).orElseThrow(() -> {
            throw new ResourceNotFoundException("Usuario no encontrado");
        });
        userRepository.delete(userExist);
    }

    private UserResponseDTO toResponse(User user) {
        List<WalletResponseDTO> walletDTOs = user.getWallets() == null
                ? List.of()
                : user.getWallets().stream()
                        .map(w -> new WalletResponseDTO(w.getId(), w.getBalance(), w.getCurrency()))
                        .toList();

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getRole(), walletDTOs);
    }
}
