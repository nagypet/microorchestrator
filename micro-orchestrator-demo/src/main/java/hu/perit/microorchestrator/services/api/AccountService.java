package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

import java.math.BigDecimal;

public interface AccountService
{
    Accounts getCustomerAccountsWithBalance(Long userId) throws ResourceNotFoundException;

    BigDecimal getBalance(String iban) throws ResourceNotFoundException;

    void transfer(CreditTransferRequest request) throws ResourceNotFoundException, CreditTransferException;
}
