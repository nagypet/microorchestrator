package hu.perit.microorchestrator.services.impl.customersetvice;

import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService
{
    private static final Map<String, CustomerDto> CUSTOMER_MAP = new HashMap<>();

    static
    {
        CUSTOMER_MAP.put("admin", new CustomerDto("admin", "Admin", 10001L, Collections.emptyList()));
        CUSTOMER_MAP.put("peter", new CustomerDto("peter", "Peter", 10002L, List.of("HU1234000010002001", "HU1234000010002002")));
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
}
