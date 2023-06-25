package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

import java.util.List;

public interface CustomerService
{
    CustomerDto getCustomerByUsername(String username) throws ResourceNotFoundException;

    CustomerDto getCustomerById(Long userId) throws ResourceNotFoundException;

    List<CustomerDto> getCustomers();
}
