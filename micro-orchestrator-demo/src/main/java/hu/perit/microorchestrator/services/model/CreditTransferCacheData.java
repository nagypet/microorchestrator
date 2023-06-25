package hu.perit.microorchestrator.services.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditTransferCacheData
{
    private final Long userId;
    private final Long giroId;
    private final BigDecimal amount;
    private final Boolean forceGiroException;
}
