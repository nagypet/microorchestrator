package hu.perit.microorchestrator.services.impl.credittransferservice;

import hu.perit.microorchestrator.exception.CreditTransferException;
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
        CreditTransferCacheData data = new CreditTransferCacheData(authenticatedUser.getUserId(), giroId, request.getAmount(), request.getForceGiroException());
        String transactionHash = UUID.randomUUID().toString();
        this.creditTransferMap.put(transactionHash, data);

        return new CheckCreditTransferResponse(transactionHash);
    }

    @Override
    public ExecuteCreditTransferResponse executeCreditTransfer(ExecuteCreditTransferRequest executeRequest) throws CreditTransferException, ResourceNotFoundException
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

        // Decrease limit
        if (!this.limitService.decreaseLimit(authenticatedUser.getUserId(), creditTransferCacheData.getAmount()))
        {
            throw new CreditTransferException("Daily limit is exceeded!");
        }

        // Execute credit transfer
        try
        {
            this.giroService.execute(creditTransferCacheData.getGiroId());
        }
        catch (RuntimeException | CreditTransferException | ResourceNotFoundException e)
        {
            this.limitService.increaseLimit(authenticatedUser.getUserId(), creditTransferCacheData.getAmount());
            throw e;
        }

        return new ExecuteCreditTransferResponse(ZonedDateTime.now());
    }
}
