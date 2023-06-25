package hu.perit.microorchestrator.services.impl.credittransferservice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.services.api.*;
import hu.perit.microorchestrator.services.model.*;
import hu.perit.spvitamin.core.StackTracer;
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
class CreditTransferServiceImplTest
{
    @Autowired
    private CreditTransferService creditTransferService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private LimitService limitService;

    @Autowired
    private GiroService giroService;

    @BeforeEach
    void setup() throws ResourceNotFoundException
    {
        CustomerDto customerDto = this.customerService.getCustomerByUsername("bank");

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .username(customerDto.getUsername())
                .anonymous(false)
                .authorities(Collections.emptyList())
                .userId(customerDto.getId())
                .build();
        this.authorizationService.setAuthenticatedUser(authenticatedUser);
    }

    @Test
    void testCheckOK() throws CreditTransferException, ResourceNotFoundException
    {
        CreditTransferRequest request = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, new BigDecimal("120"));
        CheckCreditTransferResponse response = this.creditTransferService.checkCreditTransfer(request);
        assertThat(response.getTransactionHash()).isNotNull();
    }


    @Test
    void testCheckWrongIban()
    {
        CreditTransferRequest request = new CreditTransferRequest(Constants.PETER_IBAN_2, Constants.PETER_IBAN_1, new BigDecimal("120"));
        assertThatThrownBy(() -> this.creditTransferService.checkCreditTransfer(request)).isInstanceOf(CreditTransferException.class);
    }

    @Test
    void testCheckNoCoverage()
    {
        CreditTransferRequest request = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, new BigDecimal("10000000"));
        assertThatThrownBy(() -> this.creditTransferService.checkCreditTransfer(request)).isInstanceOf(CreditTransferException.class);
    }


    @Test
    void testExecuteOK() throws CreditTransferException, ResourceNotFoundException
    {
        CreditTransferRequest checkRequest = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, new BigDecimal("120"));
        CheckCreditTransferResponse checkResponse = this.creditTransferService.checkCreditTransfer(checkRequest);
        assertThat(checkResponse.getTransactionHash()).isNotNull();
        ExecuteCreditTransferResponse executeResponse = this.creditTransferService.executeCreditTransfer(new ExecuteCreditTransferRequest(checkResponse.getTransactionHash()));
        log.debug(executeResponse.toString());
        assertThat(executeResponse.getTimestamp()).isNotNull();
        log.debug("Balance of {}: {}", Constants.BANK_IBAN, this.accountService.getBalance(Constants.BANK_IBAN).toString());
        log.debug("Balance of {}: {}", Constants.PETER_IBAN_1, this.accountService.getBalance(Constants.PETER_IBAN_1).toString());
        log.debug("Remaining limit of {}: {}", Constants.BANK_USER_ID, this.limitService.getRemainingLimit(Constants.BANK_USER_ID).toString());
        log.debug("Remaining limit of {}: {}", Constants.PETER_USER_ID, this.limitService.getRemainingLimit(Constants.PETER_USER_ID).toString());
        this.giroService.dumpTransactions();
    }

    @Test
    void testExecuteWithGiroException() throws CreditTransferException, ResourceNotFoundException
    {
        CreditTransferRequest checkRequest = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, new BigDecimal("120"));
        checkRequest.setForceGiroException(Boolean.TRUE);
        CheckCreditTransferResponse checkResponse = this.creditTransferService.checkCreditTransfer(checkRequest);
        assertThat(checkResponse.getTransactionHash()).isNotNull();
        try
        {
            ExecuteCreditTransferResponse executeResponse = this.creditTransferService.executeCreditTransfer(new ExecuteCreditTransferRequest(checkResponse.getTransactionHash()));
            log.debug(executeResponse.toString());
            assertThat(executeResponse.getTimestamp()).isNotNull();
        }
        catch (Exception e)
        {
            log.error(StackTracer.toString(e));
        }
        log.debug("Balance of {}: {}", Constants.BANK_IBAN, this.accountService.getBalance(Constants.BANK_IBAN).toString());
        log.debug("Balance of {}: {}", Constants.PETER_IBAN_1, this.accountService.getBalance(Constants.PETER_IBAN_1).toString());
        log.debug("Remaining limit of {}: {}", Constants.BANK_USER_ID, this.limitService.getRemainingLimit(Constants.BANK_USER_ID).toString());
        log.debug("Remaining limit of {}: {}", Constants.PETER_USER_ID, this.limitService.getRemainingLimit(Constants.PETER_USER_ID).toString());
        this.giroService.dumpTransactions();
    }
}
