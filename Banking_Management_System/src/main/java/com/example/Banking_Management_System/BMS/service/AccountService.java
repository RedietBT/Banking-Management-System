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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.AccessDeniedException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountsRepo accountsRepo;
    private final UserRepository userRepository;

    //==================
    // Create an account
    //==================
    @Transactional
    public Accounts createAccount(String userEmail, AccountRequest request) { // Or return AccountDetailsResponse if you have one
        // Find the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        // Generate a unique 15-digit account number (as a Long)
        Long newAccountNumber; // Keep as Long
        boolean isUnique = false;
        SecureRandom random = new SecureRandom();
        int attempts = 0;
        final int MAX_ATTEMPTS = 20;

        do {
            // Smallest 15-digit number
            BigInteger min = new BigInteger("1000000000000000"); // 1 followed by 14 zeros
            // Largest 15-digit number
            BigInteger max = new BigInteger("9999999999999999"); // 15 nines
            BigInteger range = max.subtract(min).add(BigInteger.ONE); // Inclusive range

            BigInteger randomNumber;
            do {
                // Generate a random BigInteger within the range's bit length
                randomNumber = new BigInteger(range.bitLength(), random);
            } while (randomNumber.compareTo(range) >= 0); // Keep generating if it's outside the desired range

            BigInteger finalNumber = randomNumber.add(min);
            newAccountNumber = finalNumber.longValue(); // <--- CORRECTED: Use longValue() as it now fits

            // Check if the generated account number already exists
            if (accountsRepo.findByAccountNumber(newAccountNumber).isEmpty()) { // Assuming findByAccountNumber takes Long
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
                .accountNumber(newAccountNumber) // Now a Long
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
    //Freeze Account
    //===============
    @Transactional
    public AccountsResponse freezeAccount(Long accountId){
        Accounts account = accountsRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() != AccountStatus.ACTIVE){
            throw new IllegalStateException("Account with ID " + accountId + " is not in ACTIVE status and cannot be frozen. Current status: " + account.getStatus());
        }
        account.setStatus(AccountStatus.FROZEN);
        Accounts updatedAccount = accountsRepo.save(account);
        return mapToAccountResponse(updatedAccount);
    }

    //===============
    //Unfreeze Account
    //===============
    @Transactional
    public AccountsResponse unFreezeAccount(Long accountId){
        Accounts account = accountsRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() != AccountStatus.FROZEN){
            throw new IllegalStateException("Account with ID " + accountId + " is not in FROZEN status and cannot be approved. Current status: " + account.getStatus());
        }
        account.setStatus(AccountStatus.ACTIVE);
        Accounts updatedAccount = accountsRepo.save(account);
        return mapToAccountResponse(updatedAccount);
    }

    //===============
    //Close Account
    //===============
    @Transactional
    public AccountsResponse closeAccount(Long accountId){
        Accounts account = accountsRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() != AccountStatus.FROZEN &&
                account.getStatus() != AccountStatus.ACTIVE &&
        account.getStatus() != AccountStatus.PENDING) {
            throw new IllegalStateException("Account with ID " + accountId + " is not in FROZEN, ACTIVE or PENDING status and cannot be closed. Current status: " + account.getStatus());
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setBalance(BigDecimal.ZERO);
        Accounts updatedAccount = accountsRepo.save(account);
        return mapToAccountResponse(updatedAccount);
    }

    //====================
    // Get Account Details
    //====================
    @Transactional(readOnly = true)
    public Optional<AccountsResponse> getAccountDetailsForCurrentUserByAccountNo(String userEmail, Long accountNumber) throws AccessDeniedException {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + userEmail));

        Optional<Accounts> accountOptional = accountsRepo.findByAccountNumber(accountNumber);

        if(accountOptional.isEmpty()){
            return Optional.empty();
        }
        Accounts account = accountOptional.get();

        if(!account.getUser().getId().equals(currentUser.getId())){
            throw new AccessDeniedException("Access Denied: Account " + accountNumber + " does not belong to the authenticated user.");
        }
        return Optional.of(mapToAccountResponse(account));
    }

    @Transactional(readOnly = true)
    public Optional<AccountsResponse> getAccountDetailsByAccountNo(Long accountNumber) throws AccessDeniedException {

        Optional<Accounts> accountOptional = accountsRepo.findByAccountNumber(accountNumber);

        if(accountOptional.isEmpty()){
            return Optional.empty();
        }
        Accounts account = accountOptional.get();
        return Optional.of(mapToAccountResponse(account));
    }

    @Transactional(readOnly = true)
    public List<AccountsResponse> getAccountDetailsForCurrentUserByPhoneNo(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        String userPhone = user.getPhone();
        if(userPhone == null || userPhone.trim().isEmpty()){
            throw new IllegalStateException("Phone number not available for user: " + userPhone);
        }

        List<Accounts> accounts= accountsRepo.findByUserPhone(userPhone);
        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountsResponse> getAllAccounts() {

        List<Accounts> accounts = accountsRepo.findAll();
        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
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
