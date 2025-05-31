package com.example.Banking_Management_System.BMS.repository;

import com.example.Banking_Management_System.BMS.model.AccountStatus;
import com.example.Banking_Management_System.BMS.model.AccountType;
import com.example.Banking_Management_System.BMS.model.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface AccountsRepo extends JpaRepository <Accounts, Long> {

    Optional<Accounts> findByStatus(AccountStatus accountStatus);
    Optional<Accounts> findByUserEmail(String email);
    Optional<Accounts> findByType(AccountType accountType);
    Optional<Accounts> findByUser_Id(Long userid);
    Optional<Accounts> findByAccountNumber(String accountNumber);
}
