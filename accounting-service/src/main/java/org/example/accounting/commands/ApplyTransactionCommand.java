package org.example.accounting.commands;

import lombok.RequiredArgsConstructor;
import org.example.accounting.db.BillingCycleEntity;
import org.example.accounting.db.BillingCycleRepository;
import org.example.accounting.db.PaymentTransactionEntity;
import org.example.models.accounting.PaymentTransactionAppliedEventV1;
import org.example.models.accounting.PaymentTransactionTopics;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<UUID, PaymentTransactionAppliedEventV1> kafkaTemplate;

    @Transactional
    public void apply(UUID accountId,
                      String description,
                      PaymentTransactionEntity.Type transactionType,
                      int debit,
                      int credit,
                      Instant timestamp) {
        var billingCycle = getOrInitOpenBillingCycle(accountId);
        var transaction = new PaymentTransactionEntity();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setDescription(description);
        transaction.setType(transactionType);
        transaction.setDebit(debit);
        transaction.setCredit(credit);
        transaction.setTimestamp(timestamp);
        transaction.setBillingCycle(billingCycle);
        billingCycle.getPaymentTransactions().add(transaction);
        billingCycleRepository.save(billingCycle);
        sendTransactionAppliedEvent(transaction);
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

    private void sendTransactionAppliedEvent(PaymentTransactionEntity transaction) {
        var event = new PaymentTransactionAppliedEventV1()
                .withTransactionId(transaction.getId())
                .withAccountId(transaction.getAccountId())
                .withDebit(transaction.getDebit())
                .withCredit(transaction.getCredit())
                .withTimestamp(transaction.getTimestamp())
                .withType(switch (transaction.getType()) {
                    case PAYOUT -> PaymentTransactionAppliedEventV1.Type.PAYOUT;
                    case INTERNAL_BALANCE_CHANGE -> PaymentTransactionAppliedEventV1.Type.INTERNAL_BALANCE_CHANGE;
                });
        kafkaTemplate.send(PaymentTransactionTopics.PAYMENT_TRANSACTION_APPLIED, event.getAccountId(), event);
    }

}
