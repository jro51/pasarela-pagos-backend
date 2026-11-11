package jro.pry.PasarelaPagos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private ErrorCode code;
    private String message;
    private Map<String, String> fieldErrors;

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, ErrorCode code, String message) {
        this(timestamp, status, error, code, message, Map.of());
    }

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, ErrorCode code, String message,
                            Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? Map.of() : fieldErrors;
    }

}
