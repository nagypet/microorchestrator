package hu.perit.microorchestrator.services.impl.accountservice;

import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

@ActiveProfiles({"default", "spvitamin-defaults"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class AccountServiceImplTest
{
    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthorizationService authorizationService;

    @BeforeEach
    void setup() throws ResourceNotFoundException
    {
        CustomerDto customerDto = this.customerService.getCustomerByUsername("peter");

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .username(customerDto.getUsername())
                .anonymous(false)
                .authorities(Collections.emptyList())
                .userId(customerDto.getId())
                .build();
        this.authorizationService.setAuthenticatedUser(authenticatedUser);
    }

    @Test
    void testGetCustomerAccountsWithBalance() throws ResourceNotFoundException
    {
        Accounts accountsWithBalance = this.accountService.getCustomerAccountsWithBalance();
        log.debug(accountsWithBalance.toString());
        Assertions.assertThat(accountsWithBalance.getAccounts()).isNotEmpty();
    }
}
