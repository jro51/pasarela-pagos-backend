package jro.pry.PasarelaPagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
        Long id,
        String reference,
        BigDecimal amount,
        String currency,
        String status,
        String direction,
        Long sourceWalletId,
        Long targetWalletId,
        String counterpartyEmail,
        String description,
        LocalDateTime createdAt
) {
}
