package hu.perit.microorchestrator.services.impl.accountservice;

import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(accountsWithBalance.getAccounts()).isNotEmpty();
    }

    @Test
    void testGetBalance() throws ResourceNotFoundException
    {
        String iban = "HU1234000010002001";
        BigDecimal balance = this.accountService.getBalance(iban);
        log.debug("Balance of {}: {}", iban, balance);
    }

    @Test
    void testGetBalanceWhenInvalidIban()
    {
        String iban = "invalid";
        assertThatThrownBy(() -> this.accountService.getBalance(iban)).isInstanceOf(ResourceNotFoundException.class);
    }

}
