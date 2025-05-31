package com.example.Banking_Management_System.BMS.controler;

import com.example.Banking_Management_System.BMS.DTO.AccountRequest;
import com.example.Banking_Management_System.BMS.DTO.AccountsResponse;
import com.example.Banking_Management_System.BMS.model.Accounts;
import com.example.Banking_Management_System.BMS.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@RequestMapping("api/customer/account")
@RequiredArgsConstructor
public class CustomerController {

    private final AccountService accountService;

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
}
