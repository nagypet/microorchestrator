package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

public interface CustomerService
{
    CustomerDto getCustomerByUsername(String username) throws ResourceNotFoundException;
}
