package hu.perit.microorchestrator.services.impl.customersetvice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.core.typehelpers.LongUtils;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService
{
    private static final List<CustomerDto> CUSTOMERS = new ArrayList<>();

    static
    {
        CUSTOMERS.add(new CustomerDto("bank", "Bank", Constants.BANK_USER_ID, List.of(Constants.BANK_IBAN)));
        CUSTOMERS.add(new CustomerDto("peter", "Peter", 10002L, List.of(Constants.PETER_IBAN_1, Constants.PETER_IBAN_2)));
        CUSTOMERS.add(new CustomerDto("alice", "Alice", 10003L, List.of("HU1234000010003001")));
        CUSTOMERS.add(new CustomerDto("john", "John", 10004L, List.of("HU1234000010004001")));
        CUSTOMERS.add(new CustomerDto("csaba", "Csaba", 10005L, List.of("HU1234000010005001")));
        CUSTOMERS.add(new CustomerDto("szabolcs", "Szabolcs", 10006L, List.of("HU1234000010006001")));
    }


    @Override
    public CustomerDto getCustomerByUsername(String username) throws ResourceNotFoundException
    {
        CustomerDto customerDto = CUSTOMERS.stream().filter(i -> StringUtils.equals(i.getUsername(), username)).findFirst().orElse(null);
        if (customerDto == null)
        {
            throw new ResourceNotFoundException(MessageFormat.format("Customer not foundb by name {0}", username));
        }

        return customerDto;
    }

    @Override
    public CustomerDto getCustomerById(Long userId) throws ResourceNotFoundException
    {
        CustomerDto customerDto = CUSTOMERS.stream().filter(i -> LongUtils.equals(i.getId(), userId)).findFirst().orElse(null);
        if (customerDto == null)
        {
            throw new ResourceNotFoundException(MessageFormat.format("Customer not foundb by id {0}", userId));
        }

        return customerDto;
    }

    @Override
    public List<CustomerDto> getCustomers()
    {
        return CUSTOMERS;
    }
}
