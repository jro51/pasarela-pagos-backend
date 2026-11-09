package jro.pry.PasarelaPagos.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserResponseDTO {

    private Long id;
    private String email;
    private String role;
    private List<WalletResponseDTO> wallets;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String email, String role, List<WalletResponseDTO> wallets) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.wallets = wallets;
    }

}
