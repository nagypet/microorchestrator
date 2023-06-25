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

package hu.perit.microorchestrator.services.impl.accountservice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles({"default", "spvitamin-defaults"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class AccountServiceImplTest
{
    @Autowired
    private AccountService accountService;

//    @Autowired
//    private CustomerService customerService;
//
//    @Autowired
//    private AuthorizationService authorizationService;

//    @BeforeEach
//    void setup() throws ResourceNotFoundException
//    {
//        CustomerDto customerDto = this.customerService.getCustomerByUsername("peter");
//
//        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
//                .username(customerDto.getUsername())
//                .anonymous(false)
//                .authorities(Collections.emptyList())
//                .userId(customerDto.getId())
//                .build();
//        this.authorizationService.setAuthenticatedUser(authenticatedUser);
//    }

    @Test
    void testGetCustomerAccountsWithBalance() throws ResourceNotFoundException
    {
        Accounts accountsWithBalance = this.accountService.getCustomerAccountsWithBalance(Constants.BANK_USER_ID);
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
