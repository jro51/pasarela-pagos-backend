package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.exception.BusinessException;
import jro.pry.PasarelaPagos.exception.ConflictException;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.TransactionRepository;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

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
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("Ya existe una transacción con esta referencia", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenSourceAndTargetWalletAreTheSame() {
        TransactionRequestDTO request = buildRequest();
        request.setTargetWalletId(request.getSourceWalletId());

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("La billetera de origen y destino deben ser diferentes", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenSourceWalletDoesNotExist() {
        TransactionRequestDTO request = buildRequest();
        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No existe la billetera de origen", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenTargetWalletDoesNotExist() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = wallet(1L, user(1L), "100.00", "USD");

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No existe la billetera de destino", exception.getMessage());
    }

    @Test
    void shouldThrowAccessDeniedWhenSourceWalletBelongsToAnotherUser() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = wallet(1L, user(2L), "100.00", "USD");
        Wallet targetWallet = wallet(2L, user(3L), "0.00", "USD");

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        AccessDeniedException exception = Assertions.assertThrows(AccessDeniedException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No puedes transferir desde una billetera que no te pertenece", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenCurrenciesDoNotMatch() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = wallet(1L, user(1L), "100.00", "USD");
        Wallet targetWallet = wallet(2L, user(2L), "0.00", "EUR");

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("Las billeteras deben tener la misma moneda", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenBalanceIsInsufficient() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = wallet(1L, user(1L), "20.00", "USD");
        Wallet targetWallet = wallet(2L, user(2L), "0.00", "USD");

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("Fondos insuficientes", exception.getMessage());
    }

    @Test
    void shouldProcessTransactionWhenRulesAreValid() {
        TransactionRequestDTO request = buildRequest();
        request.setAmount(new BigDecimal("40.00"));

        Wallet sourceWallet = wallet(1L, user(1L), "100.00", "USD");
        Wallet targetWallet = wallet(2L, user(2L), "50.00", "USD");

        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transaction = transactionService.processTransaction(request, 1L);

        Assertions.assertEquals(0, new BigDecimal("60.00").compareTo(sourceWallet.getBalance()));
        Assertions.assertEquals(0, new BigDecimal("90.00").compareTo(targetWallet.getBalance()));
        Assertions.assertEquals("REF-TRANSACTION-001", transaction.getTransactionReference());
        Assertions.assertEquals("SUCCESS", transaction.getStatus());
        Assertions.assertEquals(0, new BigDecimal("40.00").compareTo(transaction.getAmount()));

        verify(walletRepository).save(sourceWallet);
        verify(walletRepository).save(targetWallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    private TransactionRequestDTO buildRequest() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setSourceWalletId(1L);
        request.setTargetWalletId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setTransactionReference("REF-TRANSACTION-001");
        return request;
    }

    private Wallet wallet(Long id, User user, String balance, String currency) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal(balance));
        wallet.setCurrency(currency);
        return wallet;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
