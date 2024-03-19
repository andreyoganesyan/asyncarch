package org.example.accounting.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "payment_transaction")
public class PaymentTransactionEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "credit", nullable = false)
    private Integer credit = 0;

    @Column(name = "debit", nullable = false)
    private Integer debit = 0;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "billing_cycle_id")
    private BillingCycleEntity billingCycle;

    public enum Type {
        INTERNAL_BALANCE_CHANGE, PAYOUT
    }

}