package jro.pry.PasarelaPagos.controller;

import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.service.WalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @Test
    void shouldReturnAuthenticatedUserWallet() {
        User principal = new User();
        principal.setId(7L);

        WalletResponseDTO expected = new WalletResponseDTO(10L, new BigDecimal("2500.00"), "USD");
        when(walletService.findByIdUser(7L)).thenReturn(expected);

        ResponseEntity<WalletResponseDTO> response = walletController.getMyWallet(principal);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertSame(expected, response.getBody());
        verify(walletService).findByIdUser(7L);
    }
}
