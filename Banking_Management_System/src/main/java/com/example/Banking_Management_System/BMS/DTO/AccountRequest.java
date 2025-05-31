package com.example.Banking_Management_System.BMS.DTO;

import com.example.Banking_Management_System.BMS.model.AccountStatus;
import com.example.Banking_Management_System.BMS.model.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Account type can not be null")
    private AccountType accountType;
}
