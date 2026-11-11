package jro.pry.PasarelaPagos.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionRequestDTO {

    @NotNull(message = "La billetera de origen es obligatoria")
    @Positive(message = "El id de la billetera de origen debe ser positivo")
    private Long sourceWalletId;

    @NotNull(message = "La billetera de destino es obligatoria")
    @Positive(message = "El id de la billetera de destino debe ser positivo")
    private Long targetWalletId;

    @NotBlank(message = "La referencia de la transacción es obligatoria")
    @Size(max = 36, message = "La referencia de la transacción no puede superar los 36 caracteres")
    private String transactionReference;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Digits(integer = 13, fraction = 2, message = "El monto debe tener máximo 13 enteros y 2 decimales")
    private BigDecimal amount;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

}
