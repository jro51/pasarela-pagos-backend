package jro.pry.PasarelaPagos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.model.User;
import jro.pry.PasarelaPagos.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Billeteras", description = "Consulta de saldos y monedas")
@SecurityRequirement(name = "BearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Obtener mi billetera")
    @GetMapping("/me")
    public ResponseEntity<WalletResponseDTO> getMyWallet(@AuthenticationPrincipal User user){
        return ResponseEntity.status(HttpStatus.OK).body(walletService.findByIdUser(user.getId()));
    }

    @Operation(summary = "Obtener billetera por usuario (dueño o ADMIN)")
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(walletService.findByIdUser(id));
    }
}
