package com.example.Banking_Management_System.BMS.controler;

import com.example.Banking_Management_System.BMS.DTO.AccountsResponse;
import com.example.Banking_Management_System.BMS.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final AccountService accountService;

    @GetMapping("/accounts/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AccountsResponse> getAccountDetailsByAccountNumber(@PathVariable Long accountNumber) throws AccessDeniedException {
        return accountService.getAccountDetailsByAccountNo(accountNumber)
                .map(ResponseEntity::ok)
                .orElseThrow();
    }

    @PutMapping("/accounts/{accountId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AccountsResponse> approveAccount(@PathVariable Long accountId){
        AccountsResponse accountsResponse = accountService.approvePendingAccount(accountId);
        return ResponseEntity.ok(accountsResponse);
    }

    @PutMapping("/accounts/{accountId}/freeze")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AccountsResponse> freezeAccount(@PathVariable Long accountId){
        AccountsResponse accountsResponse = accountService.freezeAccount(accountId);
        return ResponseEntity.ok(accountsResponse);
    }

    @PutMapping("/accounts/{accountId}/unfreeze")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AccountsResponse> unfreezeAccount(@PathVariable Long accountId){
        AccountsResponse accountsResponse = accountService.unFreezeAccount(accountId);
        return ResponseEntity.ok(accountsResponse);
    }

    @PutMapping("/accounts/{accountId}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<AccountsResponse> closeAccount(@PathVariable Long accountId){
        AccountsResponse accountsResponse = accountService.closeAccount(accountId);
        return ResponseEntity.ok(accountsResponse);
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<AccountsResponse>> getAllAccounts(){
        List<AccountsResponse> accounts = accountService.getAllAccounts();
        if(accounts.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(accounts);
    }
}
