package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

public interface GiroService
{
    Long save(CreditTransferRequest request);

    void execute(Long transactionId) throws CreditTransferException, ResourceNotFoundException;
}