package com.example.Banking_Management_System.BMS.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Accounts account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 255)
    private String description;

    @Column(length = 20)
    private Long relatedAccountNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;


    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        if (this.transactionId == null || this.transactionId.isEmpty()) {
            this.transactionId = java.util.UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = TransactionStatus.PENDING;
        }
    }

    public Transaction(Accounts account, TransactionType type, BigDecimal amount, BigDecimal balanceAfterTransaction, TransactionStatus status, String description, @NotBlank Long relatedAccountNumber) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.status = status;
        this.description = description;
        this.relatedAccountNumber = relatedAccountNumber;
    }
}
