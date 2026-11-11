package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.PageResponse;
import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.dto.TransactionResponseDTO;
import jro.pry.PasarelaPagos.exception.BusinessException;
import jro.pry.PasarelaPagos.exception.ConflictException;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.TransactionRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction processTransaction(TransactionRequestDTO transactionRequest, Long authenticatedUserId){
        if (Objects.equals(transactionRequest.getSourceWalletId(), transactionRequest.getTargetWalletId())) {
            throw new BusinessException("La billetera de origen y destino deben ser diferentes");
        }

        if(transactionRepository.findByTransactionReference(transactionRequest.getTransactionReference()).isPresent()){
            throw new ConflictException("Ya existe una transacción con esta referencia");
        }

        Wallet sourceWalletExist = walletRepository.findById(transactionRequest.getSourceWalletId())
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException("No existe la billetera de origen");
                });

        Wallet targetWalletExist = walletRepository.findById(transactionRequest.getTargetWalletId())
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException("No existe la billetera de destino");
                });

        if (sourceWalletExist.getUser() == null
                || !Objects.equals(sourceWalletExist.getUser().getId(), authenticatedUserId)) {
            throw new AccessDeniedException("No puedes transferir desde una billetera que no te pertenece");
        }

        if (!Objects.equals(sourceWalletExist.getCurrency(), targetWalletExist.getCurrency())) {
            throw new BusinessException("Las billeteras deben tener la misma moneda");
        }

        if(sourceWalletExist.getBalance().compareTo(transactionRequest.getAmount()) >= 0){
            BigDecimal sourceWalletDiscount = sourceWalletExist.getBalance().subtract(transactionRequest.getAmount());
            BigDecimal targetWalletAdd = targetWalletExist.getBalance().add(transactionRequest.getAmount());

            sourceWalletExist.setBalance(sourceWalletDiscount);
            targetWalletExist.setBalance(targetWalletAdd);

            //Aqui se guardan los cambios
            walletRepository.save(sourceWalletExist);
            walletRepository.save(targetWalletExist);

            Transaction transaction = new Transaction();
            transaction.setTransactionReference(transactionRequest.getTransactionReference());
            transaction.setSourceWalletId(transactionRequest.getSourceWalletId());
            transaction.setTargetWalletId(transactionRequest.getTargetWalletId());
            transaction.setAmount(transactionRequest.getAmount());
            transaction.setStatus("SUCCESS");
            transaction.setDescription(normalizeDescription(transactionRequest.getDescription()));

            return transactionRepository.save(transaction);

        }else{
            throw new BusinessException("Fondos insuficientes");
        }
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

        return PageResponse.from(transactions.map(transaction ->
                toResponse(transaction, myWallet, counterpartyWallets)));
    }

    private TransactionResponseDTO toResponse(Transaction transaction, Wallet myWallet,
                                               Map<Long, Wallet> counterpartyWallets) {
        boolean outgoing = Objects.equals(transaction.getSourceWalletId(), myWallet.getId());
        Long counterpartyWalletId = outgoing ? transaction.getTargetWalletId() : transaction.getSourceWalletId();
        Wallet counterpartyWallet = counterpartyWallets.get(counterpartyWalletId);

        String counterpartyEmail = counterpartyWallet != null && counterpartyWallet.getUser() != null
                ? counterpartyWallet.getUser().getEmail()
                : null;

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
