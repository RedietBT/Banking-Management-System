package com.example.Banking_Management_System.BMS.DTO;

import com.example.Banking_Management_System.BMS.model.TransactionStatus;
import com.example.Banking_Management_System.BMS.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private String transactionId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal currentBalance;
    private TransactionStatus status;
    private String description;
    private LocalDateTime timestamp;
}
