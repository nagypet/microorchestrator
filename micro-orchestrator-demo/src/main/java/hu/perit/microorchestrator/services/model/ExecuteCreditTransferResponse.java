package hu.perit.microorchestrator.services.model;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ExecuteCreditTransferResponse
{
    // Submission time
    private final ZonedDateTime timestamp;
}
