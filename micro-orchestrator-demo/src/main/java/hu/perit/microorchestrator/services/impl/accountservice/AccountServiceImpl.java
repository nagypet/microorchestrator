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
import hu.perit.microorchestrator.db.repo.AccountRepo;
import hu.perit.microorchestrator.db.table.AccountEntity;
import hu.perit.microorchestrator.exception.GiroException;
import hu.perit.microorchestrator.mapper.AccountMapper;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.microorchestrator.services.model.LockMode;
import hu.perit.spvitamin.core.typehelpers.LongUtils;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService
{
    private final CustomerService customerService;
    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;


    @PostConstruct
    void init()
    {
        for (CustomerDto customerDto : this.customerService.getCustomers())
        {
            initAccountsForCustomer(customerDto);
        }
    }


    @Override
    public Accounts getCustomerAccountsWithBalance(Long userId) throws ResourceNotFoundException
    {
        CustomerDto customerDto = this.customerService.getCustomerById(userId);
        Accounts accounts = new Accounts();
        for (String iban : customerDto.getAccounts())
        {
            AccountEntity entity = getAccountByIban(iban, LockMode.FOR_READ);
            accounts.addAccount(this.accountMapper.fromEntity(entity));
        }
        return accounts;
    }


    @Override
    public BigDecimal getBalance(String iban) throws ResourceNotFoundException
    {
        AccountEntity entity = getAccountByIban(iban, LockMode.FOR_READ);
        return entity.getBalance();
    }

    @Override
    @Transactional
    public void transfer(CreditTransferRequest request) throws ResourceNotFoundException, GiroException
    {
        // Using a write lock so that parallel transaction with the same debitor account have to wait until this
        // transaction completes. No optimistic locking used.
        AccountEntity debitorAccount = getAccountByIban(request.getDebitorIban(), LockMode.FOR_UPDATE);

        // Using optimistic locking here, in order to avoid deadlocks. But the caller must be aware of
        // OptimisticLockingException and retry the transaction in such cases
        AccountEntity creditorAccount = getAccountByIban(request.getCreditorIban(), LockMode.FOR_READ);

        BigDecimal amount = request.getAmount();
        if (!debitorAccount.withdraw(amount))
        {
            // Throwing business-related exception, there is no rollback to allow the external transaction to commit
            throw new GiroException("There is no sufficient account coverage!");
        }
        creditorAccount.deposit(amount);

        // Save the account balances
        this.accountRepo.save(debitorAccount);

        // This may throw OptimisticLockingException, if a parallel transaction would also try to deposit money into
        // the same creditor account
        this.accountRepo.save(creditorAccount);
    }

    private AccountEntity getAccountByIban(String iban, LockMode lockMode) throws ResourceNotFoundException
    {
        Optional<AccountEntity> entity = (lockMode == LockMode.FOR_UPDATE) ?
                this.accountRepo.findByIbanWithWriteLock(iban) :
                this.accountRepo.findByIban(iban);
        if (entity.isEmpty())
        {
            throw new ResourceNotFoundException(MessageFormat.format("No account found by iban {0}", iban));
        }

        return entity.get();
    }


    private void initAccountsForCustomer(CustomerDto customerDto)
    {
        for (String iban : customerDto.getAccounts())
        {
            try
            {
                getAccountByIban(iban, LockMode.FOR_READ);
            }
            catch (ResourceNotFoundException e)
            {
                // Let's create a new AccountEntity
                AccountEntity accountEntity = new AccountEntity();
                accountEntity.setIban(iban);
                accountEntity.setOwnersName(customerDto.getName());
                if (LongUtils.equals(customerDto.getId(), Constants.BANK_USER_ID))
                {
                    accountEntity.setBalance(new BigDecimal("1000000"));
                }
                else
                {
                    accountEntity.setBalance(BigDecimal.ZERO);
                }
                this.accountRepo.save(accountEntity);
            }
        }
    }
}
