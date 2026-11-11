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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldThrowNotFoundWhenAuthenticatedUserHasNoWallet() {
        TransactionRequestDTO request = buildRequest();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No se encontró una billetera para este usuario", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowNotFoundWhenTargetEmailDoesNotExist() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No existe un usuario con ese email", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenTargetUserHasNoWallet() {
        TransactionRequestDTO request = buildRequest();
        Wallet sourceWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        User targetUser = user(2L, "bob@demo.com");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.of(targetUser));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No se encontró una billetera para el destinatario", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenTransferringToSelf() {
        TransactionRequestDTO request = buildRequest();
        User sameUser = user(1L, "alice@demo.com");
        Wallet sourceWallet = wallet(1L, sameUser, "100.00", "USD");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.of(sameUser));

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("No puedes transferirte a ti mismo", exception.getMessage());
        verify(walletRepository, times(1)).findByUserId(1L);
    }

    @Test
    void shouldThrowBusinessExceptionWhenCurrenciesDoNotMatch() {
        TransactionRequestDTO request = buildRequest();
        User targetUser = user(2L, "bob@demo.com");
        Wallet sourceWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        Wallet targetWallet = wallet(2L, targetUser, "50.00", "EUR");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.of(targetUser));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(targetWallet));

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("Las billeteras deben tener la misma moneda", exception.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenBalanceIsInsufficient() {
        TransactionRequestDTO request = buildRequest();
        User targetUser = user(2L, "bob@demo.com");
        Wallet sourceWallet = wallet(1L, user(1L, "alice@demo.com"), "20.00", "USD");
        Wallet targetWallet = wallet(2L, targetUser, "50.00", "USD");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.of(targetUser));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(targetWallet));

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> transactionService.processTransaction(request, 1L));

        Assertions.assertEquals("Fondos insuficientes", exception.getMessage());
    }

    @Test
    void shouldProcessTransactionAndReturnResponse() {
        TransactionRequestDTO request = buildRequest();
        request.setAmount(new BigDecimal("40.00"));
        User targetUser = user(2L, "bob@demo.com");
        Wallet sourceWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        Wallet targetWallet = wallet(2L, targetUser, "50.00", "USD");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.of(targetUser));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(targetWallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(10L);
            return transaction;
        });

        TransactionResponseDTO response = transactionService.processTransaction(request, 1L);

        Assertions.assertEquals(0, new BigDecimal("60.00").compareTo(sourceWallet.getBalance()));
        Assertions.assertEquals(0, new BigDecimal("90.00").compareTo(targetWallet.getBalance()));
        Assertions.assertEquals(10L, response.id());
        Assertions.assertNotNull(response.reference());
        Assertions.assertEquals(36, response.reference().length());
        Assertions.assertDoesNotThrow(() -> UUID.fromString(response.reference()));
        Assertions.assertEquals(0, new BigDecimal("40.00").compareTo(response.amount()));
        Assertions.assertEquals("USD", response.currency());
        Assertions.assertEquals("SUCCESS", response.status());
        Assertions.assertEquals("OUT", response.direction());
        Assertions.assertEquals(1L, response.sourceWalletId());
        Assertions.assertEquals(2L, response.targetWalletId());
        Assertions.assertEquals("bob@demo.com", response.counterpartyEmail());
        Assertions.assertEquals("Pago de prueba", response.description());
        Assertions.assertNotNull(response.createdAt());

        verify(walletRepository).save(sourceWallet);
        verify(walletRepository).save(targetWallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldNormalizeBlankDescriptionToNull() {
        TransactionRequestDTO request = buildRequest();
        request.setDescription("   ");
        User targetUser = user(2L, "bob@demo.com");
        Wallet sourceWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        Wallet targetWallet = wallet(2L, targetUser, "50.00", "USD");

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(sourceWallet));
        when(userRepository.findByEmail("bob@demo.com")).thenReturn(Optional.of(targetUser));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(targetWallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDTO response = transactionService.processTransaction(request, 1L);

        Assertions.assertNull(response.description());
    }

    @Test
    void shouldReturnOutgoingTransactionsForAuthenticatedUser() {
        Wallet myWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        Wallet counterpartyWallet = wallet(2L, user(2L, "bob@demo.com"), "50.00", "USD");
        Transaction transaction = transaction(10L, "REF-001", 1L, 2L, "40.00", "SUCCESS", "Pago de prueba");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(transaction), pageable, 1);

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(myWallet));
        when(transactionRepository.findByWalletId(1L, pageable)).thenReturn(page);
        when(walletRepository.findAllWithUserByIds(Set.of(2L))).thenReturn(List.of(counterpartyWallet));

        PageResponse<TransactionResponseDTO> response = transactionService.findMyTransactions(1L, pageable);

        Assertions.assertEquals(1, response.content().size());
        TransactionResponseDTO result = response.content().get(0);
        Assertions.assertEquals(10L, result.id());
        Assertions.assertEquals("REF-001", result.reference());
        Assertions.assertEquals(0, new BigDecimal("40.00").compareTo(result.amount()));
        Assertions.assertEquals("USD", result.currency());
        Assertions.assertEquals("SUCCESS", result.status());
        Assertions.assertEquals("OUT", result.direction());
        Assertions.assertEquals(1L, result.sourceWalletId());
        Assertions.assertEquals(2L, result.targetWalletId());
        Assertions.assertEquals("bob@demo.com", result.counterpartyEmail());
        Assertions.assertEquals("Pago de prueba", result.description());
        Assertions.assertEquals(LocalDateTime.of(2026, 11, 11, 12, 0), result.createdAt());
        Assertions.assertEquals(0, response.page());
        Assertions.assertEquals(10, response.size());
        Assertions.assertEquals(1, response.totalElements());
        Assertions.assertEquals(1, response.totalPages());
    }

    @Test
    void shouldReturnIncomingTransactionsForAuthenticatedUser() {
        Wallet myWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        Wallet counterpartyWallet = wallet(2L, user(2L, "bob@demo.com"), "50.00", "USD");
        Transaction transaction = transaction(11L, "REF-002", 2L, 1L, "25.00", "SUCCESS", "Reembolso");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of(transaction), pageable, 1);

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(myWallet));
        when(transactionRepository.findByWalletId(1L, pageable)).thenReturn(page);
        when(walletRepository.findAllWithUserByIds(Set.of(2L))).thenReturn(List.of(counterpartyWallet));

        PageResponse<TransactionResponseDTO> response = transactionService.findMyTransactions(1L, pageable);

        TransactionResponseDTO result = response.content().get(0);
        Assertions.assertEquals("IN", result.direction());
        Assertions.assertEquals("bob@demo.com", result.counterpartyEmail());
        Assertions.assertEquals(2L, result.sourceWalletId());
        Assertions.assertEquals(1L, result.targetWalletId());
        Assertions.assertEquals("Reembolso", result.description());
    }

    @Test
    void shouldReturnEmptyHistoryWhenUserHasNoTransactions() {
        Wallet myWallet = wallet(1L, user(1L, "alice@demo.com"), "100.00", "USD");
        Pageable pageable = PageRequest.of(0, 10);

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(myWallet));
        when(transactionRepository.findByWalletId(1L, pageable)).thenReturn(Page.<Transaction>empty(pageable));

        PageResponse<TransactionResponseDTO> response = transactionService.findMyTransactions(1L, pageable);

        Assertions.assertTrue(response.content().isEmpty());
        Assertions.assertEquals(0, response.totalElements());
        verify(walletRepository, never()).findAllWithUserByIds(any());
    }

    @Test
    void shouldThrowNotFoundWhenAuthenticatedUserHasNoWalletForHistory() {
        Pageable pageable = PageRequest.of(0, 10);
        when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> transactionService.findMyTransactions(99L, pageable));

        Assertions.assertEquals("No se encontró una billetera para este usuario", exception.getMessage());
        verify(transactionRepository, never()).findByWalletId(anyLong(), any(Pageable.class));
    }

    private TransactionRequestDTO buildRequest() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setTargetEmail("bob@demo.com");
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setDescription("Pago de prueba");
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

    private User user(Long id, String email) {
        User user = user(id);
        user.setEmail(email);
        return user;
    }

    private Transaction transaction(Long id, String reference, Long sourceWalletId, Long targetWalletId,
                                    String amount, String status, String description) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setTransactionReference(reference);
        transaction.setSourceWalletId(sourceWalletId);
        transaction.setTargetWalletId(targetWalletId);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setStatus(status);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.of(2026, 11, 11, 12, 0));
        return transaction;
    }
}
