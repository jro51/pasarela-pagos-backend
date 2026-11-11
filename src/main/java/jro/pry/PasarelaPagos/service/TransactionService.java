package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.PageResponse;
import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.dto.TransactionResponseDTO;
import jro.pry.PasarelaPagos.exception.BusinessException;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.TransactionRepository;
import jro.pry.PasarelaPagos.repository.UserRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponseDTO processTransaction(TransactionRequestDTO transactionRequest, Long authenticatedUserId) {
        Wallet sourceWallet = walletRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una billetera para este usuario"));

        String targetEmail = transactionRequest.getTargetEmail().trim();
        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con ese email"));

        if (Objects.equals(targetUser.getId(), authenticatedUserId)) {
            throw new BusinessException("No puedes transferirte a ti mismo");
        }

        Wallet targetWallet = walletRepository.findByUserId(targetUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una billetera para el destinatario"));

        if (!Objects.equals(sourceWallet.getCurrency(), targetWallet.getCurrency())) {
            throw new BusinessException("Las billeteras deben tener la misma moneda");
        }

        if (sourceWallet.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new BusinessException("Fondos insuficientes");
        }

        BigDecimal sourceWalletDiscount = sourceWallet.getBalance().subtract(transactionRequest.getAmount());
        BigDecimal targetWalletAdd = targetWallet.getBalance().add(transactionRequest.getAmount());

        sourceWallet.setBalance(sourceWalletDiscount);
        targetWallet.setBalance(targetWalletAdd);

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(UUID.randomUUID().toString());
        transaction.setSourceWalletId(sourceWallet.getId());
        transaction.setTargetWalletId(targetWallet.getId());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(normalizeDescription(transactionRequest.getDescription()));

        Transaction savedTransaction = transactionRepository.save(transaction);

        return toResponse(savedTransaction, sourceWallet, targetUser.getEmail());
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponseDTO> findMyTransactions(Long authenticatedUserId, Pageable pageable) {
        Wallet myWallet = walletRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró una billetera para este usuario"));

        Page<Transaction> transactions = transactionRepository.findByWalletId(myWallet.getId(), pageable);

        Set<Long> counterpartyWalletIds = transactions.getContent().stream()
                .map(transaction -> Objects.equals(transaction.getSourceWalletId(), myWallet.getId())
                        ? transaction.getTargetWalletId()
                        : transaction.getSourceWalletId())
                .collect(Collectors.toSet());

        Map<Long, Wallet> counterpartyWallets = counterpartyWalletIds.isEmpty()
                ? Map.of()
                : walletRepository.findAllWithUserByIds(counterpartyWalletIds).stream()
                        .collect(Collectors.toMap(Wallet::getId, Function.identity()));

        return PageResponse.from(transactions.map(transaction -> {
            boolean outgoing = Objects.equals(transaction.getSourceWalletId(), myWallet.getId());
            Long counterpartyWalletId = outgoing ? transaction.getTargetWalletId() : transaction.getSourceWalletId();
            Wallet counterpartyWallet = counterpartyWallets.get(counterpartyWalletId);
            String counterpartyEmail = counterpartyWallet != null && counterpartyWallet.getUser() != null
                    ? counterpartyWallet.getUser().getEmail()
                    : null;
            return toResponse(transaction, myWallet, counterpartyEmail);
        }));
    }

    private TransactionResponseDTO toResponse(Transaction transaction, Wallet myWallet, String counterpartyEmail) {
        boolean outgoing = Objects.equals(transaction.getSourceWalletId(), myWallet.getId());

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getAmount(),
                myWallet.getCurrency(),
                transaction.getStatus(),
                outgoing ? "OUT" : "IN",
                transaction.getSourceWalletId(),
                transaction.getTargetWalletId(),
                counterpartyEmail,
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
