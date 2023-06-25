package hu.perit.microorchestrator.services.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditTransferRequest
{
    private final String debitorIban;
    private final String creditorIban;
    private final BigDecimal amount;
    private Boolean forceGiroException;
}
