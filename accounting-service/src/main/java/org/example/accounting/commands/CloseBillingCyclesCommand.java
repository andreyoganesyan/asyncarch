package org.example.accounting.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.accounting.db.BillingCycleEntity;
import org.example.accounting.db.BillingCycleRepository;
import org.example.accounting.db.PaymentTransactionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class CloseBillingCyclesCommand {
    private final BillingCycleRepository billingCycleRepository;

    @Transactional
    public void executeFor(LocalDate localDate) {
        List<BillingCycleEntity> billingCyclesToClose = billingCycleRepository.findOpenBillingCyclesWhichEndOn(localDate);
        List<BillingCycleEntity> billingCyclesToSave = billingCyclesToClose
                .stream()
                .map(this::closeBillingCycle)
                .flatMap(Collection::stream)
                .toList();
        billingCycleRepository.saveAll(billingCyclesToSave);
    }

    private List<BillingCycleEntity> closeBillingCycle(BillingCycleEntity billingCycle) {
        var billingCyclesToSave = new ArrayList<BillingCycleEntity>();
        billingCyclesToSave.add(billingCycle);
        billingCycle.setStatus(BillingCycleEntity.Status.CLOSED);
        int balance = billingCycle.getInitialBalance() + billingCycle.getPaymentTransactions().stream()
                .mapToInt(transaction -> transaction.getDebit() - transaction.getCredit())
                .sum();

        if (balance < 0) {
            // next billing cycle will start with a negative balance
            var nextBillingCycle = initializeNextBillingCycle(billingCycle, balance);
            billingCyclesToSave.add(nextBillingCycle);
        }

        if (balance > 0) {
            var payoutTransaction = constructPayoutTransaction(billingCycle, balance);
            billingCycle.getPaymentTransactions().add(payoutTransaction);
            log.info("Paid out current balance of {} for account {} for {}",
                    balance, billingCycle.getAccountId(), billingCycle.getEndDate());
        }
        return billingCyclesToSave;
    }

    private PaymentTransactionEntity constructPayoutTransaction(BillingCycleEntity billingCycle, int balance) {
        var payoutTransaction = new PaymentTransactionEntity();
        payoutTransaction.setId(UUID.randomUUID());
        payoutTransaction.setCredit(balance);
        payoutTransaction.setAccountId(billingCycle.getAccountId());
        payoutTransaction.setDescription("Payout of current balance for %s".formatted(billingCycle.getEndDate()));
        return payoutTransaction;
    }

    private BillingCycleEntity initializeNextBillingCycle(BillingCycleEntity billingCycle, int balance) {
        var nextBillingCycle = new BillingCycleEntity();
        nextBillingCycle.setAccountId(billingCycle.getAccountId());
        nextBillingCycle.setInitialBalance(balance);
        nextBillingCycle.setId(UUID.randomUUID());
        nextBillingCycle.setStartDate(billingCycle.getEndDate().plusDays(1));
        // dates are inclusive, so for 1-day billing cycle start and end are equal
        nextBillingCycle.setEndDate(nextBillingCycle.getStartDate());
        return nextBillingCycle;
    }
}
