package jro.pry.PasarelaPagos.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "El email del destinatario no es válido")
    @Size(max = 150, message = "El email del destinatario no puede superar los 150 caracteres")
    private String targetEmail;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Digits(integer = 13, fraction = 2, message = "El monto debe tener máximo 13 enteros y 2 decimales")
    private BigDecimal amount;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

}
