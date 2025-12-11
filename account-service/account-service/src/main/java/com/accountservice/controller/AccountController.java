package com.accountservice.controller;

import com.accountservice.document.Account;
import com.accountservice.service.AccountService;
import com.accountservice.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse create(@RequestBody Account account) {
        logger.info("POST /api/accounts/create holderName={}", account.getHolderName());
        Account created = accountService.createAccount(account.getHolderName());
        logger.info("Account created: accountNumber={}", created.getAccountNumber());
        return new ApiResponse("Account created", created, true);
    }

    @GetMapping("/{accountNumber}")
    public ApiResponse get(@PathVariable String accountNumber) {
        logger.info("GET /api/accounts/{}", accountNumber);
        return new ApiResponse("Fetched", accountService.getAccount(accountNumber), true);
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse update(@RequestBody Account account) {
        logger.info("PUT /api/accounts/update accountNumber={}", account.getAccountNumber());
        Account updated = accountService.updateAccount(account.getAccountNumber(), account.getHolderName());
        return new ApiResponse("Updated", updated, true);
    }

    @PutMapping("/{accountNumber}/balance")
    public ApiResponse updateBalance(@PathVariable String accountNumber, @RequestBody Account acc) {
        logger.info("PUT /api/accounts/{}/balance newBalance={}", accountNumber, acc.getBalance());
        Account updated = accountService.updateBalance(accountNumber, acc.getBalance());
        return new ApiResponse("Balance updated", updated, true);
    }

    @DeleteMapping("/{accountNumber}")
    public ApiResponse delete(@PathVariable String accountNumber) {
        logger.info("DELETE /api/accounts/{}", accountNumber);
        accountService.deleteAccount(accountNumber);
        return new ApiResponse("Deleted", accountNumber, true);
    }
}
