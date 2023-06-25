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

import hu.perit.microorchestrator.db.repo.CreditTransferRepo;
import hu.perit.microorchestrator.db.table.CreditTransferEntity;
import hu.perit.microorchestrator.exception.CreditTransferException;
import hu.perit.microorchestrator.mapper.CreditTransferRequestMapper;
import hu.perit.microorchestrator.services.api.AccountService;
import hu.perit.microorchestrator.services.api.GiroService;
import hu.perit.microorchestrator.services.model.CreditTransferRequest;
import hu.perit.microorchestrator.services.model.CreditTransferStatus;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiroServiceImpl implements GiroService
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
    public void execute(Long giroId) throws CreditTransferException, ResourceNotFoundException
    {
        CreditTransferEntity creditTransferEntity = findById(giroId);

        try
        {
            // --------------- TEST CODE -------------------------------------------------------------------------------
            if (BooleanUtils.isTrue(creditTransferEntity.getForceGiroException()))
            {
                throw new CreditTransferException("forced exception by input");
            }
            // --------------- TEST CODE -------------------------------------------------------------------------------

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

    private CreditTransferEntity findById(Long transactionId) throws ResourceNotFoundException
    {
        Optional<CreditTransferEntity> optEntity = this.repo.findById(transactionId);
        if (optEntity.isEmpty())
        {
            throw new ResourceNotFoundException(MessageFormat.format("Transaction not found by id: {}", transactionId));
        }

        return optEntity.get();
    }
}
