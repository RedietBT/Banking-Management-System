package com.example.Banking_Management_System.BMS.model;

public enum TransactionType {

    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_OUT, // For the sending side of a transfer
    TRANSFER_IN   // For the receiving side of a transfer
}
