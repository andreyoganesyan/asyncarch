package org.example.analytics.queries;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.example.jooq.analytics.Tables.PAYMENT_TRANSACTION;

@Service
@RequiredArgsConstructor
public class NegativeBalanceAccountsQuery {
    private final DSLContext dsl;

    public QueryResult execute(LocalDate date) {

        Field<BigDecimal> balance = DSL.sum(PAYMENT_TRANSACTION.DEBIT.minus(PAYMENT_TRANSACTION.CREDIT));
        Result<Record2<UUID, BigDecimal>> negativeBalancesForDate = dsl
                .select(PAYMENT_TRANSACTION.ACCOUNT_ID, balance)
                .from(PAYMENT_TRANSACTION)
                .where(PAYMENT_TRANSACTION.TIMESTAMP
                        // not the most optimal solution, but it works
                        .lt(date.plusDays(1).atStartOfDay())
                        .and(PAYMENT_TRANSACTION.TYPE.ne("PAYOUT")))
                .groupBy(PAYMENT_TRANSACTION.ACCOUNT_ID)
                .having(balance.lt(BigDecimal.ZERO))
                .fetch();

        List<UUID> accountIds = negativeBalancesForDate.stream()
                .map(Record2::component1)
                .toList();
        return new QueryResult(accountIds.size(), accountIds, date);
    }

    public record QueryResult(long count, List<UUID> accountIds, LocalDate date) {
    }
}
