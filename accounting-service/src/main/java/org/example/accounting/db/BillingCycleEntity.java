package org.example.accounting.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "billing_cycle")
public class BillingCycleEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "initial_balance", nullable = false)
    private Integer initialBalance = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.OPEN;

    @OneToMany(mappedBy = "billingCycle", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<PaymentTransactionEntity> paymentTransactions = new ArrayList<>();

    public enum Status {
        OPEN, CLOSED
    }

    public int calculateBalance() {
        return this.getInitialBalance() + this.getPaymentTransactions().stream()
                .mapToInt(transaction -> transaction.getDebit() - transaction.getCredit())
                .sum();
    }

}