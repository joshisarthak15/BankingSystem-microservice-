package com.accountservice.service;

import com.accountservice.document.Account;
import com.accountservice.exception.AccountNotFoundException;
import com.accountservice.repository.AccountRepository;
import com.accountservice.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Date;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);


    public Account createAccount(String holderName) {
        logger.info("Creating account for holder='{}'", holderName);
        String accNum = IdGenerator.generateAccountNumber(holderName);

        Account acc = new Account();
        acc.setAccountNumber(accNum);
        acc.setHolderName(holderName);
        acc.setStatus("ACTIVE");
        acc.setBalance(0.0);
        acc.setCreatedAt(new Date());

        logger.debug("Created account: {}", acc);
        return accountRepository.save(acc);
    }

    public Account getAccount(String accountNumber) {
        logger.info("Fetching account for accountNumber={}", accountNumber);
        Account acc = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        logger.debug("Found account: {}", acc);
        return acc;
    }

    public Account updateAccount(String accountNumber, String holderName) {
        Account acc = getAccount(accountNumber);
        acc.setHolderName(holderName);
        return accountRepository.save(acc);
    }

    public Account updateBalance(String accountNumber, Double amount) {
        Account acc = getAccount(accountNumber);
        acc.setBalance(amount);
        return accountRepository.save(acc);
    }

    public void deleteAccount(String accountNumber) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountNotFoundException(accountNumber);
        }
        accountRepository.deleteByAccountNumber(accountNumber);
    }
}
