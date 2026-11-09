package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
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

    //Given - When - Then

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient(){
        //Given
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setSourceWalletId(1L);
        request.setTargetWalletId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setTransactionReference("REF-TRANSACTION-001");

        Wallet sourceWallet = new Wallet();
        sourceWallet.setId(1L);
        sourceWallet.setBalance(BigDecimal.valueOf(20.00));

        Wallet targetWallet = new Wallet();
        targetWallet.setId(2L);
        targetWallet.setBalance(BigDecimal.valueOf(0.00));

        //When
        when(transactionRepository.findByTransactionReference("REF-TRANSACTION-001")).thenReturn(Optional.empty());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(targetWallet));

        //Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
           transactionService.processTransaction(request);
        });

        Assertions.assertEquals("Fondos insuficientes", exception.getMessage());
    }
}