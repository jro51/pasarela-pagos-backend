package jro.pry.PasarelaPagos.controller;

import jro.pry.PasarelaPagos.dto.TransactionRequestDTO;
import jro.pry.PasarelaPagos.model.Transaction;
import jro.pry.PasarelaPagos.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin("*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> processTransaction(@RequestBody TransactionRequestDTO transactionRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.processTransaction(transactionRequestDTO));
    }
}
