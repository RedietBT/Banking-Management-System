package com.example.Banking_Management_System.BMS.repository;

import com.example.Banking_Management_System.BMS.model.AccountStatus;
import com.example.Banking_Management_System.BMS.model.AccountType;
import com.example.Banking_Management_System.BMS.model.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepo extends JpaRepository <Accounts, Long> {

    List<Accounts> findByStatus(AccountStatus accountStatus);
    List<Accounts> findByUserEmail(String email);
    List<Accounts> findByType(AccountType accountType);
}
