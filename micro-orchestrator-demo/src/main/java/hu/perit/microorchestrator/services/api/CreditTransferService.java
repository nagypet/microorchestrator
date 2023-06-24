package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.db.microorchestratordb.table.CreditTransferEntity;
import hu.perit.microorchestrator.services.model.CreditTransferException;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

/**
 * This service stores the transaction in the database
 */

public interface CreditTransferService
{
    Long save(CreditTransferRequest request);

    void execute(Long transactionId) throws CreditTransferException, ResourceNotFoundException;

    CreditTransferEntity findById(Long transactionId) throws ResourceNotFoundException;

    // For unit testing only
    void dumpTransactions();
}
