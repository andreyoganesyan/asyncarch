package org.example.accounting.queries;

import lombok.RequiredArgsConstructor;
import org.example.accounting.db.PaymentTransactionEntity;
import org.example.accounting.db.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountStatementQuery {
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional(readOnly = true)
    public Result execute(UUID accountId, LocalDate startDate, LocalDate endDate) {
        List<PaymentTransactionEntity> transactionsInRange = paymentTransactionRepository.findTransactionsInRange(accountId,
                startDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));

        Map<LocalDate, List<Result.Transaction>> transactionsByDate = transactionsInRange.stream()
                .map(transaction -> new Result.Transaction(
                        transaction.getTimestamp(),
                        transaction.getDescription(),
                        transaction.getDebit() - transaction.getCredit()))
                .collect(Collectors.groupingBy(
                        transaction -> LocalDate.ofInstant(transaction.timestamp(), ZoneId.of("UTC"))));

        return new Result(accountId, transactionsByDate);
    }

    public record Result(UUID accountId, Map<LocalDate, List<Transaction>> transactionsByDate) {
        public record Transaction(Instant timestamp, String description, Integer value) {
        }
    }
}
