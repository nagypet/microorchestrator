package hu.perit.microorchestrator.services.model;

import lombok.Data;

@Data
public class ExecuteCreditTransferRequest
{
    private final String transactionHash;
}
