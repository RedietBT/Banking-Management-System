package com.example.Banking_Management_System.BMS.controler;

import com.example.Banking_Management_System.BMS.DTO.*;
import com.example.Banking_Management_System.BMS.model.Accounts;
import com.example.Banking_Management_System.BMS.service.AccountService;
import com.example.Banking_Management_System.BMS.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Controller
@RestController
@RequestMapping("api/customer/account")
@RequiredArgsConstructor
public class CustomerController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PostMapping("/apply")
    public ResponseEntity<String> applyForAccount(@Valid @RequestBody AccountRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Accounts account = accountService.createAccount(userEmail, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Account application submitted successfully with ID:" + account.getId());
    }

    @GetMapping("/me/all")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AccountsResponse>> getCurrectUserAccountsByPhone(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        List<AccountsResponse> accountsResponses = accountService.getAccountDetailsForCurrentUserByPhoneNo(userEmail);
        if (accountsResponses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(accountsResponses);
    }

    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AccountsResponse> getMyAccountDetails(@PathVariable String accountNumber){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        try {
            Optional<AccountsResponse> accountsResponse = accountService.getAccountDetailsForCurrentUserByAccountNo(userEmail, (accountNumber));
            return accountsResponse
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with number: " + accountNumber));
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.deposit(userEmail, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        TransactionResponse response = transactionService.withdraw(userEmail, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        List<TransactionResponse> responses = transactionService.transfer(userEmail, request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/account/{accountNumber}/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(@PathVariable String accountNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        List<TransactionResponse> transactions = transactionService.getTransactionsForAccount(userEmail, accountNumber);
        if (transactions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(transactions);
    }
}
