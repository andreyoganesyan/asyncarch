package org.example.analytics.queries;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.example.jooq.analytics.Tables.PAYMENT_TRANSACTION;

@Service
@RequiredArgsConstructor
public class CompanyIncomeQuery {
    private final DSLContext dsl;

    public QueryResult execute(LocalDate startDate, LocalDate endDate) {
        Record1<BigDecimal> incomeRecord = dsl.select(DSL.sum(PAYMENT_TRANSACTION.CREDIT.minus(PAYMENT_TRANSACTION.DEBIT)))
                .from(PAYMENT_TRANSACTION)
                .where(PAYMENT_TRANSACTION.TIMESTAMP.between(
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay())
                        .and(PAYMENT_TRANSACTION.TYPE.ne("PAYOUT")))
                .fetchOne();
        BigDecimal income = Optional.ofNullable(incomeRecord)
                .map(Record1::component1)
                .orElse(BigDecimal.ZERO);
        return new QueryResult(income);
    }

    public record QueryResult(BigDecimal income) {
    }
}
