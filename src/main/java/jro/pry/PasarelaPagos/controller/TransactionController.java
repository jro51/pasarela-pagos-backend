package jro.pry.PasarelaPagos.controller;

import jakarta.validation.Valid;
import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> processTransaction(@Valid @RequestBody TransactionRequestDTO transactionRequestDTO,
                                                          @AuthenticationPrincipal User user){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.processTransaction(transactionRequestDTO, user.getId()));
    }
}
