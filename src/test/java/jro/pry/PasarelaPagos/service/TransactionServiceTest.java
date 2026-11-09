package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.exception.BusinessException;
import jro.pry.PasarelaPagos.exception.ConflictException;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.TransactionRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldThrowConflictWhenReferenceAlreadyExists() {
        TransactionRequestDTO request = buildRequest();
        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001"))
                .thenReturn(Optional.of(new Transaction()));

        ConflictException exception = Assertions.assertThrows(ConflictException.class,
                () -> transactionService.processTransaction(request));

        Assertions.assertEquals("Ya existe una transacción con esta referencia", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenSourceWalletDoesNotExist() {
        TransactionRequestDTO request = buildRequest();
        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request));

        Assertions.assertEquals("No existe la billetera de origen", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenTargetWalletDoesNotExist() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = new Wallet();
        sourceWallet.setId(1L);
        sourceWallet.setBalance(BigDecimal.valueOf(100.00));

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request));

        Assertions.assertEquals("No existe la billetera de destino", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenBalanceIsInsufficient() {
        TransactionRequestDTO request = buildRequest();

        Wallet sourceWallet = new Wallet();
        sourceWallet.setId(1L);
        sourceWallet.setBalance(BigDecimal.valueOf(20.00));

        Wallet targetWallet = new Wallet();
        targetWallet.setId(2L);
        targetWallet.setBalance(BigDecimal.ZERO);

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request));

        Assertions.assertEquals("Fondos insuficientes", exception.getMessage());
    }

    private TransactionRequestDTO buildRequest() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setSourceWalletId(1L);
        request.setTargetWalletId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setTransactionReference("REF-TRANSACTION-001");
        return request;
    }
}
