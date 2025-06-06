package com.example.Banking_Management_System.BMS.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequest {

    @NotBlank(message = "Account number is required")
    private Long accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Deposit amount must be positive")
    private BigDecimal amount;
}
