package hu.perit.microorchestrator.mapper;

import hu.perit.microorchestrator.db.table.AccountEntity;
import hu.perit.microorchestrator.services.model.AccountDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper
{
    AccountDto fromEntity(AccountEntity entity);
}
