package jro.pry.PasarelaPagos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 36)
    private String transactionReference;

    @Column(name = "source_wallet_id", nullable = false)
    private Long sourceWalletId;

    @Column(name = "target_wallet_id", nullable = false)
    private Long targetWalletId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, SUCCESS, FAILED

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Transaction() {
    }

    public Transaction(Long id, String transactionReference, Long sourceWalletId, Long targetWalletId, BigDecimal amount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.sourceWalletId = sourceWalletId;
        this.targetWalletId = targetWalletId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

}
