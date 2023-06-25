package hu.perit.microorchestrator.services.impl.customersetvice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService
{
    private static final Map<String, CustomerDto> CUSTOMER_MAP = new HashMap<>();

    static
    {
        CUSTOMER_MAP.put("bank", new CustomerDto("bank", "Bank", Constants.BANK_USER_ID, List.of(Constants.BANK_IBAN)));
        CUSTOMER_MAP.put("peter", new CustomerDto("peter", "Peter", 10002L, List.of(Constants.PETER_IBAN_1, Constants.PETER_IBAN_2)));
        CUSTOMER_MAP.put("alice", new CustomerDto("alice", "Alice", 10003L, List.of("HU1234000010003001")));
        CUSTOMER_MAP.put("john", new CustomerDto("john", "John", 10004L, List.of("HU1234000010004001")));
        CUSTOMER_MAP.put("csaba", new CustomerDto("csaba", "Csaba", 10005L, List.of("HU1234000010005001")));
        CUSTOMER_MAP.put("szabolcs", new CustomerDto("szabolcs", "Szabolcs", 10006L, List.of("HU1234000010006001")));
    }


    @Override
    public CustomerDto getCustomerByUsername(String username) throws ResourceNotFoundException
    {
        if (!CUSTOMER_MAP.containsKey(username))
        {
            throw new ResourceNotFoundException(MessageFormat.format("Customer not foundb by name {0}", username));
        }

        return CUSTOMER_MAP.get(username);
    }

    @Override
    public Collection<String> getUsernames()
    {
        return CUSTOMER_MAP.keySet();
    }
}
