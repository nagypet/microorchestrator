/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    void testExecuteOK() throws Exception
    {
        BigDecimal origBalanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal origBalancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        BigDecimal origRemainingLimitBank = this.limitService.getRemainingLimit(Constants.BANK_USER_ID);
        BigDecimal origRemainingLimitPeter = this.limitService.getRemainingLimit(Constants.PETER_USER_ID);

        BigDecimal amount = new BigDecimal("120");
        CreditTransferRequest checkRequest = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, amount);
        CheckCreditTransferResponse checkResponse = this.creditTransferService.checkCreditTransfer(checkRequest);
        assertThat(checkResponse.getTransactionHash()).isNotNull();
        ExecuteCreditTransferResponse executeResponse = this.creditTransferService.executeCreditTransfer(new ExecuteCreditTransferRequest(checkResponse.getTransactionHash()));
        log.debug(executeResponse.toString());
        assertThat(executeResponse.getTimestamp()).isNotNull();

        BigDecimal balanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal balancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        BigDecimal remainingLimitBank = this.limitService.getRemainingLimit(Constants.BANK_USER_ID);
        BigDecimal remainingLimitPeter = this.limitService.getRemainingLimit(Constants.PETER_USER_ID);
        log.debug("Balance of {}: {}", Constants.BANK_IBAN, balanceBank.toString());
        log.debug("Balance of {}: {}", Constants.PETER_IBAN_1, balancePeter.toString());
        log.debug("Remaining limit of {}: {}", Constants.BANK_USER_ID, remainingLimitBank.toString());
        log.debug("Remaining limit of {}: {}", Constants.PETER_USER_ID, remainingLimitPeter.toString());
        assertThat(balanceBank).isEqualTo(origBalanceBank.subtract(amount));
        assertThat(balancePeter).isEqualTo(origBalancePeter.add(amount));
        assertThat(remainingLimitBank).isEqualTo(origRemainingLimitBank.subtract(amount));
        assertThat(remainingLimitPeter).isEqualTo(origRemainingLimitPeter);
        this.giroService.dumpTransactions();
    }

    @Test
    void testExecuteWithGiroException() throws CreditTransferException, ResourceNotFoundException
    {
        BigDecimal origBalanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal origBalancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        BigDecimal origRemainingLimitBank = this.limitService.getRemainingLimit(Constants.BANK_USER_ID);
        BigDecimal origRemainingLimitPeter = this.limitService.getRemainingLimit(Constants.PETER_USER_ID);

        BigDecimal amount = new BigDecimal("120");
        CreditTransferRequest checkRequest = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, amount);
        checkRequest.setForcedExceptionForTesting(ForcedExceptionType.GIRO_EXCEPTION);
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

        BigDecimal balanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal balancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        BigDecimal remainingLimitBank = this.limitService.getRemainingLimit(Constants.BANK_USER_ID);
        BigDecimal remainingLimitPeter = this.limitService.getRemainingLimit(Constants.PETER_USER_ID);
        log.debug("Balance of {}: {}", Constants.BANK_IBAN, balanceBank.toString());
        log.debug("Balance of {}: {}", Constants.PETER_IBAN_1, balancePeter.toString());
        log.debug("Remaining limit of {}: {}", Constants.BANK_USER_ID, remainingLimitBank.toString());
        log.debug("Remaining limit of {}: {}", Constants.PETER_USER_ID, remainingLimitPeter.toString());
        assertThat(balanceBank).isEqualTo(origBalanceBank);
        assertThat(balancePeter).isEqualTo(origBalancePeter);
        assertThat(remainingLimitBank).isEqualTo(origRemainingLimitBank);
        assertThat(remainingLimitPeter).isEqualTo(origRemainingLimitPeter);
        this.giroService.dumpTransactions();
    }

    @Test
    void testExecuteWithTimeoutException() throws CreditTransferException, ResourceNotFoundException
    {
        BigDecimal origBalanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal origBalancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        BigDecimal origRemainingLimitBank = this.limitService.getRemainingLimit(Constants.BANK_USER_ID);
        BigDecimal origRemainingLimitPeter = this.limitService.getRemainingLimit(Constants.PETER_USER_ID);

        BigDecimal amount = new BigDecimal("120");
        CreditTransferRequest checkRequest = new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, amount);
        checkRequest.setForcedExceptionForTesting(ForcedExceptionType.READ_TIMEOUT);
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

        BigDecimal balanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal balancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        BigDecimal remainingLimitBank = this.limitService.getRemainingLimit(Constants.BANK_USER_ID);
        BigDecimal remainingLimitPeter = this.limitService.getRemainingLimit(Constants.PETER_USER_ID);
        log.debug("Balance of {}: {}", Constants.BANK_IBAN, balanceBank.toString());
        log.debug("Balance of {}: {}", Constants.PETER_IBAN_1, balancePeter.toString());
        log.debug("Remaining limit of {}: {}", Constants.BANK_USER_ID, remainingLimitBank.toString());
        log.debug("Remaining limit of {}: {}", Constants.PETER_USER_ID, remainingLimitPeter.toString());
        assertThat(balanceBank).isEqualTo(origBalanceBank.subtract(amount));
        assertThat(balancePeter).isEqualTo(origBalancePeter.add(amount));
        assertThat(remainingLimitBank).isEqualTo(origRemainingLimitBank.subtract(amount));
        assertThat(remainingLimitPeter).isEqualTo(origRemainingLimitPeter);
        this.giroService.dumpTransactions();
    }
}
