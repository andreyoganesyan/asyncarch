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

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "description")
    private String description;

    @Column(name = "credit")
    private Integer credit = 0;

    @Column(name = "debit")
    private Integer debit = 0;

    @Column(name = "timestamp")
    private Instant timestamp;

    @ManyToOne
    @JoinColumn(name = "billing_cycle_id")
    private BillingCycleEntity billingCycle;

}