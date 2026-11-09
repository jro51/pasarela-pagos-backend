package jro.pry.PasarelaPagos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionRequestDTO {
    private Long sourceWalletId;
    private Long targetWalletId;
    private String transactionReference;
    private BigDecimal amount;

}
