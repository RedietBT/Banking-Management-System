package com.example.Banking_Management_System.BMS.DTO;

import com.example.Banking_Management_System.BMS.model.AccountStatus;
import com.example.Banking_Management_System.BMS.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsResponse {

    private Long id;
    private String applicantsFirstname;
    private String applicantsLastname;
    private String applicantsEmail;
    private Long accountNumber;
    private String phone;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private LocalDate lastUpdated;
    private LocalDate createdAt;
}
