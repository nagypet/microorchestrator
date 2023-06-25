package hu.perit.microorchestrator.db.repo;


import hu.perit.microorchestrator.db.table.CreditTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransferRepo extends JpaRepository<CreditTransferEntity, Long>
{
}
