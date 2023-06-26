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

import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.exception.GiroTimeoutException;
import hu.perit.microorchestrator.orchestrator.MicroOrchestrator;
import hu.perit.microorchestrator.orchestrator.ProcessStep;
import hu.perit.microorchestrator.services.api.*;
import hu.perit.microorchestrator.services.model.*;
import hu.perit.spvitamin.core.typehelpers.LongUtils;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditTransferServiceImpl implements CreditTransferService
{
    private final AuthorizationService authorizationService;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final GiroService giroService;
    private final LimitService limitService;
    private final Map<String, CreditTransferCacheData> creditTransferMap = new HashMap<>();

    @Override
    public CheckCreditTransferResponse checkCreditTransfer(CreditTransferRequest request) throws ResourceNotFoundException, CreditTransferException
    {
        AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();

        // Check if customer has right to the debitor account
        CustomerDto customerDto = this.customerService.getCustomerById(authenticatedUser.getUserId());
        if (!customerDto.getAccounts().contains(request.getDebitorIban()))
        {
            throw new CreditTransferException(MessageFormat.format("Customer does not have right for this IBAN {0}!", request.getDebitorIban()));
        }

        // Check if there is enough account coverage
        BigDecimal balance = this.accountService.getBalance(request.getDebitorIban());
        if (balance.compareTo(request.getAmount()) < 0)
        {
            throw new CreditTransferException("Account coverage is not enough!");
        }

        // Check if daily limit ok
        if (!this.limitService.checkLimit(authenticatedUser.getUserId(), request.getAmount()))
        {
            throw new CreditTransferException("Daily limit is exceeded!");
        }

        // Save credit transfer
        Long giroId = this.giroService.save(request);

        // Saving the credit transfer in the cache
        CreditTransferCacheData data = new CreditTransferCacheData(authenticatedUser.getUserId(), giroId, request.getAmount(), request.getForcedExceptionForTesting());
        String transactionHash = UUID.randomUUID().toString();
        this.creditTransferMap.put(transactionHash, data);

        return new CheckCreditTransferResponse(transactionHash);
    }

    @Override
    public ExecuteCreditTransferResponse executeCreditTransfer(ExecuteCreditTransferRequest executeRequest) throws Exception
    {
        AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();

        if (!this.creditTransferMap.containsKey(executeRequest.getTransactionHash()))
        {
            // Transaction not found
            throw new CreditTransferException(MessageFormat.format("Transaction {0} not found!", executeRequest.getTransactionHash()));
        }
        CreditTransferCacheData creditTransferCacheData = this.creditTransferMap.get(executeRequest.getTransactionHash());

        // Check if same user executes
        if (!LongUtils.equals(creditTransferCacheData.getUserId(), authenticatedUser.getUserId()))
        {
            throw new CreditTransferException("User does not have right to execute this transaction!");
        }

        MicroOrchestrator orchestrator = new MicroOrchestrator();

        // STEP 1: Changing limit
        orchestrator.addStep(new ProcessStep("Changing limit")
                .action(() -> {
                    if (!this.limitService.decreaseLimit(authenticatedUser.getUserId(), creditTransferCacheData.getAmount()))
                    {
                        throw new CreditTransferException("Daily limit is exceeded!");
                    }
                })
                .undoAction(e -> {
                    // In case of a timeout we do not know if the transfer will be executed by the backend system
                    if (!(e instanceof GiroTimeoutException))
                    {
                        // This is the corrective action after an exception has been thrown
                        this.limitService.increaseLimit(authenticatedUser.getUserId(), creditTransferCacheData.getAmount());
                    }
                }));

        // STEP 2: Execute credit transfer
        orchestrator.addStep(new ProcessStep("Executing credit transfer")
                .action(() -> this.giroService.execute(creditTransferCacheData.getGiroId())));

        orchestrator.execute();

        return new ExecuteCreditTransferResponse(ZonedDateTime.now());
    }
}
