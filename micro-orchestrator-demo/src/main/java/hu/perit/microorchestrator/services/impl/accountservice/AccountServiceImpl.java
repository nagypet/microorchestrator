package hu.perit.microorchestrator.services.impl.accountservice;

import hu.perit.microorchestrator.db.repo.AccountRepo;
import hu.perit.microorchestrator.db.table.AccountEntity;
import hu.perit.microorchestrator.mapper.AccountMapper;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.microorchestrator.services.model.AccountDto;
import hu.perit.microorchestrator.services.model.Accounts;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService
{
    private final AuthorizationService authorizationService;
    private final CustomerService customerService;
    private final AccountRepo accountRepo;
    private final AccountMapper accountMapper;

    @Override
    public Accounts getCustomerAccountsWithBalance() throws ResourceNotFoundException
    {
        AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();
        CustomerDto customerDto = this.customerService.getCustomerByUsername(authenticatedUser.getUsername());
        Accounts accounts = new Accounts();
        for (String iban : customerDto.getAccounts())
        {
            AccountEntity accountEntity = this.accountRepo.findByIban(iban).orElse(null);
            if (accountEntity == null)
            {
                // Let's create a new AccountEntity
                accountEntity = new AccountEntity();
                accountEntity.setIban(iban);
                accountEntity.setOwnersName(customerDto.getName());
                accountEntity.setBalance(BigDecimal.ZERO);
                this.accountRepo.save(accountEntity);
            }
            accounts.addAccount(this.accountMapper.fromEntity(accountEntity));
        }
        return accounts;
    }
}
