package hu.perit.microorchestrator.db.microorchestratordb.repo;

import hu.perit.microorchestrator.db.microorchestratordb.table.CreditTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransferRepo extends JpaRepository<CreditTransferEntity, Long>
{
}
