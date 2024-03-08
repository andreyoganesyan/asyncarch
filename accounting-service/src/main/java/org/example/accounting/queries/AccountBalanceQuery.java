package org.example.accounting.queries;

import lombok.RequiredArgsConstructor;
import org.example.accounting.db.BillingCycleEntity;
import org.example.accounting.db.BillingCycleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountBalanceQuery {
    private final BillingCycleRepository billingCycleRepository;

    @Transactional(readOnly = true)
    public Result execute(UUID accountId) {
        Integer balance = billingCycleRepository.findLatestBillingCycle(accountId)
                .map(BillingCycleEntity::calculateBalance).orElse(0);
        return new Result(accountId, balance);
    }

    public record Result(UUID accountId, Integer balance) {
    }
}
