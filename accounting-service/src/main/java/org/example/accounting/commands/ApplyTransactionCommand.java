package org.example.accounting.commands;

import lombok.RequiredArgsConstructor;
import org.example.accounting.db.BillingCycleEntity;
import org.example.accounting.db.BillingCycleRepository;
import org.example.accounting.db.PaymentTransactionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplyTransactionCommand {
    private final BillingCycleRepository billingCycleRepository;

    @Transactional
    public void apply(UUID accountId, String description, int debit, int credit, Instant timestamp) {
        var billingCycle = getOrInitOpenBillingCycle(accountId);
        var transaction = new PaymentTransactionEntity();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setDescription(description);
        transaction.setDebit(debit);
        transaction.setCredit(credit);
        transaction.setTimestamp(timestamp);
        billingCycle.getPaymentTransactions().add(transaction);
        billingCycleRepository.save(billingCycle);
    }

    private BillingCycleEntity getOrInitOpenBillingCycle(UUID accountId) {
        Optional<BillingCycleEntity> latestOpenBillingCycle = billingCycleRepository.findLatestOpenBillingCycle(accountId);
        if (latestOpenBillingCycle.isPresent()) {
            return latestOpenBillingCycle.get();
        }
        var newBillingCycle = new BillingCycleEntity();
        newBillingCycle.setId(UUID.randomUUID());
        newBillingCycle.setStartDate(LocalDate.now());
        newBillingCycle.setEndDate(newBillingCycle.getStartDate());
        newBillingCycle.setAccountId(accountId);
        return newBillingCycle;
    }
}
