package jro.pry.PasarelaPagos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class WalletResponseDTO {
    private Long id;
    private BigDecimal balance;
    private String currency;

    public WalletResponseDTO() {
    }

    public WalletResponseDTO(Long id, BigDecimal balance, String currency) {
        this.id = id;
        this.balance = balance;
        this.currency = currency;
    }

}
