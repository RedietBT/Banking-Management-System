package com.example.Banking_Management_System.BMS.repository;

import com.example.Banking_Management_System.BMS.model.Accounts;
import com.example.Banking_Management_System.BMS.model.Transaction;

import com.example.Banking_Management_System.BMS.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountOrderByTimestampDesc(Accounts account);
    List<Transaction> findByAccountAndTypeOrderByTimestampDesc(Accounts account, TransactionType type);
    Optional<Transaction> findByTransactionId(String transactionId);
}
