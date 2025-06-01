package com.example.Banking_Management_System.BMS.service;

import com.example.Banking_Management_System.BMS.DTO.DepositRequest;
import com.example.Banking_Management_System.BMS.DTO.TransactionResponse;
import com.example.Banking_Management_System.BMS.DTO.TransferRequest;
import com.example.Banking_Management_System.BMS.DTO.WithdrawRequest;
import com.example.Banking_Management_System.BMS.model.*;
import com.example.Banking_Management_System.BMS.repository.AccountsRepo;
import com.example.Banking_Management_System.BMS.repository.TransactionRepository;
import com.example.Banking_Management_System.userAuth.user.User;
import com.example.Banking_Management_System.userAuth.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Banking_Management_System.BMS.model.TransactionStatus;
import com.example.Banking_Management_System.BMS.model.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountsRepo accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    //=======
    //Deposit
    //=======
    @Transactional
    public TransactionResponse deposit(String userEmail, DepositRequest request) {

        Accounts account = getAndValidateUserAccount(userEmail, request.getAccountNumber());
        validateAccountActive(account);
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        Accounts updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction(
                updatedAccount,
                TransactionType.DEPOSIT,
                request.getAmount(),
                updatedAccount.getBalance(),
                TransactionStatus.COMPLETED,
                "Cash Deposit",
                null
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToTransactionResponse(savedTransaction);
    }

    //========
    //withdraw
    //========
    @Transactional
    public TransactionResponse withdraw(String userEmail, WithdrawRequest request) {

        Accounts account = getAndValidateUserAccount(userEmail, request.getAccountNumber());
        validateAccountActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds in account " + request.getAccountNumber());
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        Accounts updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction(
                updatedAccount,
                TransactionType.WITHDRAWAL,
                request.getAmount(),
                updatedAccount.getBalance(), // Balance after this transaction
                TransactionStatus.COMPLETED,
                "Cash Withdrawal",
                null // No related account for a simple withdrawal
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToTransactionResponse(savedTransaction);
    }

    //========
    //transfer
    //========
    @Transactional
    public List<TransactionResponse> transfer(String userEmail, TransferRequest request) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalStateException("Cannot transfer funds to the same account.");
        }

        // 1. Validate source account ownership and status
        Accounts sourceAccount = getAndValidateUserAccount(userEmail, request.getSourceAccountNumber());
        validateAccountActive(sourceAccount);

        // 2. Validate destination account status (no ownership check needed for destination as it can be anyone's account)
        Accounts destinationAccount = accountRepository.findByAccountNumber(Long.valueOf(request.getDestinationAccountNumber()))
                .orElseThrow(() -> new EntityNotFoundException("Destination account not found with number: " + request.getDestinationAccountNumber()));
        validateAccountActive(destinationAccount);

        // 3. Check for sufficient balance in source account
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds in source account " + request.getSourceAccountNumber());
        }

        // 4. Perform debit from source account
        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(request.getAmount());
        sourceAccount.setBalance(newSourceBalance);
        Accounts updatedSourceAccount = accountRepository.save(sourceAccount);

        // 5. Perform credit to destination account
        BigDecimal newDestinationBalance = destinationAccount.getBalance().add(request.getAmount());
        destinationAccount.setBalance(newDestinationBalance);
        Accounts updatedDestinationAccount = accountRepository.save(destinationAccount);

        List<TransactionResponse> transactionResponses = new ArrayList<>();

        // 6. Log debit transaction for source account (TRANSFER_OUT)
        Transaction debitTransaction = new Transaction(
                updatedSourceAccount,
                TransactionType.TRANSFER_OUT,
                request.getAmount(),
                updatedSourceAccount.getBalance(),
                TransactionStatus.COMPLETED,
                request.getDescription() != null ? request.getDescription() : "Transfer to " + request.getDestinationAccountNumber(),
                request.getDestinationAccountNumber()
        );
        transactionResponses.add(mapToTransactionResponse(transactionRepository.save(debitTransaction)));

        // 7. Log credit transaction for destination account (TRANSFER_IN)
        Transaction creditTransaction = new Transaction(
                updatedDestinationAccount,
                TransactionType.TRANSFER_IN,
                request.getAmount(),
                updatedDestinationAccount.getBalance(),
                TransactionStatus.COMPLETED,
                request.getDescription() != null ? request.getDescription() : "Transfer from " + request.getSourceAccountNumber(),
                request.getSourceAccountNumber()
        );
        transactionResponses.add(mapToTransactionResponse(transactionRepository.save(creditTransaction)));

        return transactionResponses;
    }

    //=========================
    //getTransactionsForAccount
    //=========================
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsForAccount(String userEmail, String accountNumber) {
        // 1. Validate account ownership
        Accounts account = getAndValidateUserAccount(userEmail, accountNumber);

        // 2. Retrieve transactions for this account, ordered by timestamp
        List<Transaction> transactions = transactionRepository.findByAccountOrderByTimestampDesc(account);

        // 3. Map to DTOs and return
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    //========================
    //getAndValidateUserAccount
    //=========================
    private Accounts getAndValidateUserAccount(String userEmail, String accountNumber) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        Accounts account = accountRepository.findByAccountNumber(Long.valueOf(accountNumber))
                .orElseThrow(() -> new EntityNotFoundException("Account not found with number: " + accountNumber));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access Denied: Account " + accountNumber + " does not belong to the authenticated user.");
        }
        return account;
    }

    //========================
    //validateAccountActive
    //=========================
    private void validateAccountActive(Accounts account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account " + account.getAccountNumber() + " is not active. Current status: " + account.getStatus() + ". Cannot perform transaction.");
        }
    }

    //========================
    //mapToTransactionResponse
    //========================
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .sourceAccountNumber(transaction.getType() == TransactionType.TRANSFER_OUT || transaction.getType() == TransactionType.WITHDRAWAL ? transaction.getAccounts().getAccountNumber() : null)
                .destinationAccountNumber(transaction.getType() == TransactionType.TRANSFER_IN || transaction.getType() == TransactionType.DEPOSIT ? transaction.getAccounts().getAccountNumber() : null)
                .type(TransactionType.valueOf(String.valueOf(transaction.getType())))
                .amount(transaction.getAmount())
                .currentBalance(transaction.getBalanceAfterTransaction())
                .status(TransactionStatus.valueOf(String.valueOf(transaction.getStatus())))
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
