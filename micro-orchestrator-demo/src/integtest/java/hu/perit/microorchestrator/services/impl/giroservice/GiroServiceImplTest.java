package hu.perit.microorchestrator.services.impl.giroservice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.api.GiroService;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@ActiveProfiles({"default", "spvitamin-defaults"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class GiroServiceImplTest
{
    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private GiroService giroService;

    @Test
    void saveAndExecute() throws ResourceNotFoundException, CreditTransferException
    {
        log.debug("------------------- saveAndExecute ------------------------------------------------------------------");
        Long transactionId = this.giroService.save(new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, new BigDecimal(10)));
        this.giroService.execute(transactionId);
        BigDecimal balanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal balancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        Assertions.assertThat(balanceBank).isEqualTo("999990.00");
        Assertions.assertThat(balancePeter).isEqualTo("10.00");
    }
}
