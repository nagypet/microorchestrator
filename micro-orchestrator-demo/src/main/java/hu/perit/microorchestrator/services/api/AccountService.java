package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.db.microorchestratordb.table.AccountEntity;
import hu.perit.microorchestrator.services.model.CreditTransferException;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.microorchestrator.services.model.LockMode;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

public interface AccountService
{
    void transfer(CreditTransferRequest request) throws ResourceNotFoundException, CreditTransferException;

    AccountEntity getAccountByIban(String iban, LockMode lockMode) throws ResourceNotFoundException;
}
