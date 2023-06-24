package hu.perit.microorchestrator.services.impl.credittransferservice;

import hu.perit.microorchestrator.db.microorchestratordb.repo.CreditTransferRepo;
import hu.perit.microorchestrator.db.microorchestratordb.table.CreditTransferEntity;
import hu.perit.microorchestrator.mapper.CreditTransferRequestMapper;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.CreditTransferService;
import hu.perit.microorchestrator.services.model.CreditTransferException;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.microorchestrator.services.model.CreditTransferStatus;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditTransferServiceImpl implements CreditTransferService
{
    private final CreditTransferRepo repo;
    private final CreditTransferRequestMapper mapper;
    private final AccountService accountService;

    @Override
    public Long save(CreditTransferRequest request)
    {
        CreditTransferEntity creditTransferEntity = this.mapper.toEntity(request);

        CreditTransferEntity savedEntity = this.repo.save(creditTransferEntity);
        log.info(MessageFormat.format("Credit transfer saved: {0}", savedEntity));
        return savedEntity.getId();
    }

    @Override
    @Transactional(rollbackOn = RuntimeException.class)
    public void execute(Long transactionId) throws CreditTransferException, ResourceNotFoundException
    {
        CreditTransferEntity creditTransferEntity = findById(transactionId);

        try
        {
            this.accountService.transfer(this.mapper.fromEntity(creditTransferEntity));
            creditTransferEntity.setStatus(CreditTransferStatus.EXECUTED);
        }
        catch (RuntimeException | CreditTransferException | ResourceNotFoundException e)
        {
            // Here could be a retry in case of OptimisticLockingException
            log.error(StackTracer.toString(e));
            creditTransferEntity.setStatus(CreditTransferStatus.FAILED);
            creditTransferEntity.setErrorText(e.toString());
            throw e;
        }

        log.info(MessageFormat.format("Credit transfer executed: {0}", creditTransferEntity));
        this.repo.save(creditTransferEntity);
    }

    @Override
    public CreditTransferEntity findById(Long transactionId) throws ResourceNotFoundException
    {
        Optional<CreditTransferEntity> optEntity = this.repo.findById(transactionId);
        if (optEntity.isEmpty())
        {
            throw new ResourceNotFoundException(MessageFormat.format("Transaction not found by id: {}", transactionId));
        }

        return optEntity.get();
    }

    @Override
    public void dumpTransactions()
    {
        List<CreditTransferEntity> all = this.repo.findAll();
        log.debug("----------------------------------------------------------------------------------------------------------");
        for (CreditTransferEntity entity : all)
        {
            log.debug(entity.toString());
        }
        log.debug("----------------------------------------------------------------------------------------------------------");
    }
}
