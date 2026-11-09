package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void shouldReturnWalletResponseWhenUserExists() {
        Long userId = 1L;

        Wallet wallet = new Wallet();
        wallet.setId(13L);
        wallet.setBalance(new BigDecimal(50.00));
        wallet.setCurrency("USD");

        //Simulamos que el userId si tiene su billetera y retorna el Wallet que creamos
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponseDTO response = walletService.findByIdUser(userId);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(13L, response.getId());
        Assertions.assertEquals(new BigDecimal(50.00), response.getBalance());
        Assertions.assertEquals("USD", response.getCurrency());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound(){
        Long userId = 10L;

        //Simulamos que no encuentra nada con ese id
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
           walletService.findByIdUser(userId);
        });

        Assertions.assertEquals("No se encontró billetera para este usuario", exception.getMessage());
    }
}