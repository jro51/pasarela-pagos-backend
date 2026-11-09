package jro.pry.PasarelaPagos.service;

import jro.pry.PasarelaPagos.dto.WalletResponseDTO;
import jro.pry.PasarelaPagos.exception.ResourceNotFoundException;
import jro.pry.PasarelaPagos.model.Wallet;
import jro.pry.PasarelaPagos.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletResponseDTO findByIdUser(Long id){
        Wallet walletExist = walletRepository.findByUserId(id).orElseThrow(() -> {
            throw new ResourceNotFoundException("No se encontró billetera para este usuario");
        });

        return new WalletResponseDTO(walletExist.getId(), walletExist.getBalance(), walletExist.getCurrency());
    }
}
