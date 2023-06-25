package hu.perit.microorchestrator.services.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto
{
    private final String iban;
    private final String ownersName;
    private final BigDecimal balance;
}
