package org.example.accounting.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingCycleRepository extends JpaRepository<BillingCycleEntity, UUID> {
    @Query("select b from BillingCycleEntity b where b.accountId = ?1 and b.status = 'OPEN' order by b.endDate desc limit 1")
    Optional<BillingCycleEntity> findLatestOpenBillingCycle(UUID accountId);

    @Query("select b from BillingCycleEntity b where b.accountId = ?1 order by b.endDate desc limit 1")
    Optional<BillingCycleEntity> findLatestBillingCycle(UUID accountId);

    @Query("select b from BillingCycleEntity b join fetch b.paymentTransactions where b.endDate <= ?1 and b.status = 'OPEN'")
    List<BillingCycleEntity> findOpenBillingCyclesWhichEndBefore(LocalDate endDate);


}