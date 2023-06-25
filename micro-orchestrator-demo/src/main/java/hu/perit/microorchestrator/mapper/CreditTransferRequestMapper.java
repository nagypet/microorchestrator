package hu.perit.microorchestrator.mapper;

import hu.perit.microorchestrator.db.table.CreditTransferEntity;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditTransferRequestMapper
{
    @Mapping(target = "statusCode", expression = "java(1L)")
    CreditTransferEntity toEntity(CreditTransferRequest request);

    CreditTransferRequest fromEntity(CreditTransferEntity entity);
}
