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

package hu.perit.microorchestrator.services.impl.giroservice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.GiroService;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
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
    private AccountService accountService;

    @Autowired
    private GiroService giroService;

    @Test
    void saveAndExecute() throws Exception
    {
        log.debug("------------------- saveAndExecute ------------------------------------------------------------------");
        BigDecimal origBalanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal origBalancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        Long transactionId = this.giroService.save(new CreditTransferRequest(Constants.BANK_IBAN, Constants.PETER_IBAN_1, new BigDecimal(10)));
        this.giroService.execute(transactionId);
        BigDecimal balanceBank = this.accountService.getBalance(Constants.BANK_IBAN);
        BigDecimal balancePeter = this.accountService.getBalance(Constants.PETER_IBAN_1);
        Assertions.assertThat(balanceBank).isEqualTo(origBalanceBank.subtract(new BigDecimal(10)));
        Assertions.assertThat(balancePeter).isEqualTo(origBalancePeter.add(new BigDecimal(10)));
    }
}
