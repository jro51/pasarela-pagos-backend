package jro.pry.PasarelaPagos.controller;

import jro.pry.PasarelaPagos.dto.PageResponse;
import jro.pry.PasarelaPagos.dto.TransactionResponseDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void shouldReturnAuthenticatedUserTransactionHistory() {
        User principal = new User();
        principal.setId(7L);
        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<TransactionResponseDTO> expected = new PageResponse<>(List.of(), 0, 10, 0, 0);
        when(transactionService.findMyTransactions(7L, pageable)).thenReturn(expected);

        ResponseEntity<PageResponse<TransactionResponseDTO>> response =
                transactionController.findMyTransactions(principal, pageable);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertSame(expected, response.getBody());
        verify(transactionService).findMyTransactions(7L, pageable);
    }
}
