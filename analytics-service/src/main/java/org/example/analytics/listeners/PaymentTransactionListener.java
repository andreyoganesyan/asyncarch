package org.example.analytics.listeners;

import lombok.RequiredArgsConstructor;
import org.example.jooq.analytics.tables.records.PaymentTransactionRecord;
import org.example.models.accounting.PaymentTransactionAppliedEventV1;
import org.example.models.accounting.PaymentTransactionTopics;
import org.jooq.DSLContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PaymentTransactionListener {
    private final DSLContext dsl;

    @KafkaListener(topics = PaymentTransactionTopics.PAYMENT_TRANSACTION_APPLIED, groupId = "analytics-service-payment-transaction-applied")
    @Transactional
    public void accept(@Payload PaymentTransactionAppliedEventV1 paymentTransactionAppliedEvent) {
        var record = new PaymentTransactionRecord();
        record.setId(paymentTransactionAppliedEvent.getTransactionId());
        record.setAccountId(paymentTransactionAppliedEvent.getAccountId());
        record.setCredit(paymentTransactionAppliedEvent.getCredit());
        record.setDebit(paymentTransactionAppliedEvent.getDebit());
        record.setTimestamp(LocalDateTime.ofInstant(paymentTransactionAppliedEvent.getTimestamp(), ZoneId.of("UTC")));
        record.setType(paymentTransactionAppliedEvent.getType().name());
        dsl.attach(record);
        record.insert();
    }
}
