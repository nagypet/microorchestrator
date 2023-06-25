package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

public interface AccountService
{
    Accounts getCustomerAccountsWithBalance() throws ResourceNotFoundException;
}
