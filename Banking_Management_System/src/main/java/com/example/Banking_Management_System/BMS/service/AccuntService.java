package com.example.Banking_Management_System.BMS.service;

import com.example.Banking_Management_System.BMS.DTO.AccountRequest;
import com.example.Banking_Management_System.BMS.DTO.AccountsResponse;
import com.example.Banking_Management_System.BMS.model.AccountStatus;
import com.example.Banking_Management_System.BMS.model.Accounts;
import com.example.Banking_Management_System.BMS.repository.AccountsRepo;
import com.example.Banking_Management_System.userAuth.user.User;
import com.example.Banking_Management_System.userAuth.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccuntService {

    private final AccountsRepo accountsRepo;
    private final UserRepository userRepository;

    //==================
    // Create an account
    //==================
    @Transactional
    public Accounts createAccount(String userEmail, AccountRequest request) {
        // Find the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        // Generate a unique 20-digit account number
        Long newAccountNumber;
        boolean isUnique = false;
        SecureRandom random = new SecureRandom(); // More secure than Random
        int attempts = 0;
        final int MAX_ATTEMPTS = 20;

        do {
            BigInteger min = new BigInteger("10000000000000000000"); // smallest 20-digit number
            BigInteger max = new BigInteger("99999999999999999999");
            BigInteger range = max.subtract(min);

            BigInteger randomNumber;
            do {
                randomNumber = new BigInteger(max.bitLength(), random);
            } while (randomNumber.compareTo(range) > 0);

            BigInteger finalNumber = randomNumber.add(min);
            newAccountNumber = Long.valueOf(finalNumber.toString()); // Always 20 digits, no leading zeros

            if (accountsRepo.findByAccountNumber(newAccountNumber).isEmpty()) {
                isUnique = true;
            }

            attempts++;
            if (attempts >= MAX_ATTEMPTS && !isUnique) {
                throw new RuntimeException("Failed to generate a unique account number after " + MAX_ATTEMPTS + " attempts.");
            }

        } while (!isUnique);

        // Create and save the account
        Accounts account = Accounts.builder()
                .user(user)
                .accountNumber(String.valueOf(newAccountNumber))
                .accountType(request.getAccountType())
                .build();

        return accountsRepo.save(account);
    }

    //===============
    //Approve Account
    //===============
    @Transactional
    public AccountsResponse approvePendingAccount(Long accountId){
        Accounts account = accountsRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() != AccountStatus.PENDING){
            throw new IllegalStateException("Account with ID " + accountId + " is not in PENDING status and cannot be approved. Current status: " + account.getStatus());
        }
        account.setStatus(AccountStatus.ACTIVE);
        Accounts updatedAccount = accountsRepo.save(account);
        return mapToAccountResponse(updatedAccount);
    }

    //===============
    //Approve Account
    //===============
    @Transactional
    public AccountsResponse freezeAccount(Long accountId){
        Accounts account = accountsRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() != AccountStatus.ACTIVE){
            throw new IllegalStateException("Account with ID " + accountId + " is not in ACTIVE status and cannot be approved. Current status: " + account.getStatus());
        }
        account.setStatus(AccountStatus.FROZEN);
        Accounts updatedAccount = accountsRepo.save(account);
        return mapToAccountResponse(updatedAccount);
    }

    //====================
    // Get Account Details
    //====================
    @Transactional(readOnly = true)
    public Optional<AccountsResponse> getAccountDetailsForCurrentUserByAccountNo(Long accountNumber){
        return accountsRepo.findByAccountNumber(accountNumber)
                .map(this::mapToAccountResponse);
    }

    @Transactional(readOnly = true)
    public Optional<AccountsResponse> getAccountDetailsForCurrentUserByPhoneNo(String phone){
        return accountsRepo.findByUserPhone(phone)
                .map(this::mapToAccountResponse);
    }

    //=======================
    //Map to Account Response
    //=======================
    private AccountsResponse mapToAccountResponse (Accounts accounts){
        return AccountsResponse.builder()
                .id(accounts.getId())
                .accountNumber(accounts.getAccountNumber())
                .applicantsFirstname(accounts.getUser().getFirstname())
                .applicantsLastname(accounts.getUser().getLastname())
                .applicantsEmail(accounts.getUser().getEmail())
                .phone(accounts.getUser().getPhone())
                .accountType(accounts.getAccountType())
                .balance(accounts.getBalance())
                .accountStatus(accounts.getStatus())
                .createdAt(LocalDate.from(accounts.getCreatedAt()))
                .lastUpdated(LocalDate.from(accounts.getUpdatedAt()))
                .build();
    }
}
