package jro.pry.PasarelaPagos.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

class ValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldRejectTransactionWhenRequiredFieldsAreMissing() {
        TransactionRequestDTO request = new TransactionRequestDTO();

        Set<String> fields = invalidFields(validator.validate(request));

        Assertions.assertEquals(4, fields.size());
        Assertions.assertTrue(fields.containsAll(Set.of(
                "sourceWalletId", "targetWalletId", "transactionReference", "amount"
        )));
    }

    @Test
    void shouldRejectTransactionWithInvalidValues() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setSourceWalletId(0L);
        request.setTargetWalletId(-2L);
        request.setTransactionReference("   ");
        request.setAmount(new BigDecimal("10.999"));

        Set<String> fields = invalidFields(validator.validate(request));

        Assertions.assertTrue(fields.contains("sourceWalletId"));
        Assertions.assertTrue(fields.contains("targetWalletId"));
        Assertions.assertTrue(fields.contains("transactionReference"));
        Assertions.assertTrue(fields.contains("amount"));
    }

    @Test
    void shouldRejectTransactionReferenceLongerThan36Characters() {
        TransactionRequestDTO request = validTransaction();
        request.setTransactionReference("X".repeat(37));

        Set<String> fields = invalidFields(validator.validate(request));

        Assertions.assertEquals(Set.of("transactionReference"), fields);
    }

    @Test
    void shouldAcceptValidTransaction() {
        Assertions.assertTrue(validator.validate(validTransaction()).isEmpty());
    }

    @Test
    void shouldRejectLoginWithInvalidEmailAndBlankPassword() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("no-es-un-email");
        request.setPassword(" ");

        Set<String> fields = invalidFields(validator.validate(request));

        Assertions.assertEquals(Set.of("email", "password"), fields);
    }

    @Test
    void shouldAcceptValidLogin() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("user@example.com");
        request.setPassword("123456");

        Assertions.assertTrue(validator.validate(request).isEmpty());
    }

    private TransactionRequestDTO validTransaction() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setSourceWalletId(1L);
        request.setTargetWalletId(2L);
        request.setTransactionReference("REF-123");
        request.setAmount(new BigDecimal("10.50"));
        return request;
    }

    private Set<String> invalidFields(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
