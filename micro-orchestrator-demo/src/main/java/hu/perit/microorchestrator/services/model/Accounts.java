package hu.perit.microorchestrator.services.model;


import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class Accounts
{
    private final List<AccountDto> accounts = new ArrayList<>();

    public void addAccount(AccountDto accountDto)
    {
        this.accounts.add(accountDto);
    }
}
