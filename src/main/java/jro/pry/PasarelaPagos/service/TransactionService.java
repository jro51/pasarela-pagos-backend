package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.exception.BusinessException;
import jro.pry.PasarelaPagos.exception.ConflictException;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.TransactionRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

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

            return transactionRepository.save(transaction);

        }else{
            throw new BusinessException("Fondos insuficientes");
        }
    }

}
