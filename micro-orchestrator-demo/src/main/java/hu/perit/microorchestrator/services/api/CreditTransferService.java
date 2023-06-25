package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.services.model.CheckCreditTransferResponse;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.microorchestrator.services.model.ExecuteCreditTransferRequest;
import hu.perit.microorchestrator.services.model.ExecuteCreditTransferResponse;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

public interface CreditTransferService
{
    CheckCreditTransferResponse checkCreditTransfer(CreditTransferRequest request) throws ResourceNotFoundException, CreditTransferException;

    ExecuteCreditTransferResponse executeCreditTransfer(ExecuteCreditTransferRequest request) throws CreditTransferException, ResourceNotFoundException;
}
