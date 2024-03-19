package org.example.accounting.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {
    @Query("""
            select p from PaymentTransactionEntity p
            where p.accountId = ?1 and p.timestamp >= ?2 and p.timestamp < ?3
            order by p.timestamp desc
            """)
    List<PaymentTransactionEntity> findTransactionsInRange(UUID accountId, Instant from, Instant to);


    @Query("""
            select p from PaymentTransactionEntity p
            where p.timestamp >= ?1 and p.timestamp < ?2
            order by p.timestamp desc
            """)
    List<PaymentTransactionEntity> findTransactionsInRange(Instant from, Instant to);
}