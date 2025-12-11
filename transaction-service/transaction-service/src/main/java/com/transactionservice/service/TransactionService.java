package com.transactionservice.service;

import com.transactionservice.document.Transaction;
import com.transactionservice.exception.TransactionException;
import com.transactionservice.feign.AccountClient;
import com.transactionservice.feign.NotificationClient;
import com.transactionservice.repository.TransactionRepository;
import com.transactionservice.util.ApiResponse;
import com.transactionservice.util.IdGenerator;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private NotificationClient notificationClient;

    // ------------------------------------------------------------
    //   Circuit Breaker Wrapped Remote Calls
    // ------------------------------------------------------------

    @CircuitBreaker(name = "accountCB", fallbackMethod = "accountGetFallback")
    public ApiResponse getAccountRemote(String accountNumber) {
        logger.debug("Calling AccountService GET /api/accounts/{}", accountNumber);
        return accountClient.getAccount(accountNumber);
    }

    public ApiResponse accountGetFallback(String accountNumber, Throwable ex) {
        logger.error("Fallback triggered for GET account {} - reason: {}", accountNumber, ex.getMessage());
        throw new TransactionException("Account Service unavailable! Try again later.");
    }

    @CircuitBreaker(name = "accountCB", fallbackMethod = "accountUpdateFallback")
    public ApiResponse updateBalanceRemote(String accountNumber, Map<String, Object> body) {
        logger.debug("Calling AccountService PUT /api/accounts/{}/balance body={}", accountNumber, body);
        return accountClient.updateBalance(accountNumber, body);
    }

    public ApiResponse accountUpdateFallback(String accountNumber, Map<String, Object> body, Throwable ex) {
        logger.error("Fallback triggered for UPDATE balance account={} - reason={}", accountNumber, ex.getMessage());
        throw new TransactionException("Account Service unavailable! Try again later.");
    }


    // ------------------------------------------------------------
    //   Deposit
    // ------------------------------------------------------------
    public Transaction deposit(String accountNumber, Double amount) {

        logger.info("Initiating deposit: account={} amount={}", accountNumber, amount);

        if (amount == null || amount <= 0) {
            logger.warn("Invalid deposit amount={} for account={}", amount, accountNumber);
            throw new TransactionException("Deposit amount must be positive!");
        }

        ApiResponse accountResp = getAccountRemote(accountNumber);
        if (!accountResp.isSuccess())
            throw new TransactionException("Account not found!");

        Map accData = (Map) accountResp.getData();
        Double oldBalance = Double.valueOf(accData.get("balance").toString());

        Double newBalance = oldBalance + amount;
        logger.debug("Old balance={} new balance={} for {}", oldBalance, newBalance, accountNumber);

        Map<String, Object> body = new HashMap<>();
        body.put("balance", newBalance);
        updateBalanceRemote(accountNumber, body);

        Transaction txn = new Transaction();
        txn.setTransactionId(IdGenerator.generateTransactionId());
        txn.setType("DEPOSIT");
        txn.setAmount(amount);
        txn.setStatus("SUCCESS");
        txn.setSourceAccount(accountNumber);
        txn.setTimestamp(new Date());

        transactionRepository.save(txn);
        logger.info("Deposit transaction saved: txnId={} account={}", txn.getTransactionId(), accountNumber);

        Map<String, Object> notify = new HashMap<>();
        notify.put("message", "Deposit of " + amount + " successful for account " + accountNumber);
        try {
            notificationClient.sendNotification(notify);
            logger.debug("Notification sent for deposit txnId={}", txn.getTransactionId());
        } catch (Exception e) {
            logger.error("Notification failed for deposit: {}", e.getMessage());
        }

        return txn;
    }


    // ------------------------------------------------------------
    //   Withdraw
    // ------------------------------------------------------------
    public Transaction withdraw(String accountNumber, Double amount) {

        logger.info("Initiating withdraw: account={} amount={}", accountNumber, amount);

        if (amount == null || amount <= 0) {
            logger.warn("Invalid withdraw amount={} for account={}", amount, accountNumber);
            throw new TransactionException("Withdraw amount must be positive!");
        }

        ApiResponse acc = getAccountRemote(accountNumber);
        if (!acc.isSuccess())
            throw new TransactionException("Account not found!");

        Map accData = (Map) acc.getData();
        Double oldBalance = Double.valueOf(accData.get("balance").toString());

        if (oldBalance < amount) {
            logger.warn("Insufficient balance={} for withdraw amount={} account={}", oldBalance, amount, accountNumber);
            throw new TransactionException("Insufficient balance!");
        }

        Double newBalance = oldBalance - amount;
        logger.debug("Old balance={} new balance={} for {}", oldBalance, newBalance, accountNumber);

        Map<String, Object> body = new HashMap<>();
        body.put("balance", newBalance);
        updateBalanceRemote(accountNumber, body);

        Transaction txn = new Transaction();
        txn.setTransactionId(IdGenerator.generateTransactionId());
        txn.setType("WITHDRAW");
        txn.setAmount(amount);
        txn.setStatus("SUCCESS");
        txn.setSourceAccount(accountNumber);
        txn.setTimestamp(new Date());

        transactionRepository.save(txn);
        logger.info("Withdraw transaction saved: txnId={} account={}", txn.getTransactionId(), accountNumber);

        Map<String, Object> notify = new HashMap<>();
        notify.put("message", "Withdraw of " + amount + " successful for account " + accountNumber);
        try {
            notificationClient.sendNotification(notify);
            logger.debug("Notification sent for withdraw txnId={}", txn.getTransactionId());
        } catch (Exception e) {
            logger.error("Notification failed for withdraw: {}", e.getMessage());
        }

        return txn;
    }


    // ------------------------------------------------------------
    //   Transfer
    // ------------------------------------------------------------
    public Transaction transfer(String source, String destination, Double amount) {

        logger.info("Initiating transfer: source={} destination={} amount={}", source, destination, amount);

        if (source.equals(destination)) {
            logger.warn("Invalid transfer: same source and destination {}", source);
            throw new TransactionException("Source and destination cannot be same!");
        }

        if (amount == null || amount <= 0) {
            logger.warn("Invalid transfer amount={} from {}", amount, source);
            throw new TransactionException("Amount must be positive!");
        }

        ApiResponse src = getAccountRemote(source);
        if (!src.isSuccess())
            throw new TransactionException("Source account not found!");

        Map srcData = (Map) src.getData();
        Double srcBalance = Double.valueOf(srcData.get("balance").toString());

        if (srcBalance < amount) {
            logger.warn("Insufficient funds: balance={} amount={} for source={}", srcBalance, amount, source);
            throw new TransactionException("Insufficient funds in source account!");
        }

        ApiResponse dest = getAccountRemote(destination);
        if (!dest.isSuccess())
            throw new TransactionException("Destination account not found!");

        Map destData = (Map) dest.getData();
        Double destBalance = Double.valueOf(destData.get("balance").toString());

        Double newSrcBalance = srcBalance - amount;
        Double newDestBalance = destBalance + amount;

        logger.debug("Updating balances: src={}→{} dest={}→{}", srcBalance, newSrcBalance, destBalance, newDestBalance);

        Map<String, Object> body1 = new HashMap<>();
        body1.put("balance", newSrcBalance);
        updateBalanceRemote(source, body1);

        Map<String, Object> body2 = new HashMap<>();
        body2.put("balance", newDestBalance);
        updateBalanceRemote(destination, body2);

        Transaction txn = new Transaction();
        txn.setTransactionId(IdGenerator.generateTransactionId());
        txn.setType("TRANSFER");
        txn.setAmount(amount);
        txn.setStatus("SUCCESS");
        txn.setSourceAccount(source);
        txn.setDestinationAccount(destination);
        txn.setTimestamp(new Date());

        transactionRepository.save(txn);
        logger.info("Transfer transaction saved: txnId={} source={} dest={}", txn.getTransactionId(), source, destination);

        Map<String, Object> notify = new HashMap<>();
        notify.put("message", "Transfer of " + amount + " from " + source + " to " + destination + " successful");
        try {
            notificationClient.sendNotification(notify);
            logger.debug("Notification sent for transfer txnId={}", txn.getTransactionId());
        } catch (Exception e) {
            logger.error("Notification failed for transfer: {}", e.getMessage());
        }

        return txn;
    }


    // ------------------------------------------------------------
    //   Get Transactions
    // ------------------------------------------------------------
    public List<Transaction> getTransactions(String accountNumber) {
        logger.info("Fetching transaction history for account={}", accountNumber);
        return transactionRepository.findBySourceAccountOrDestinationAccount(accountNumber, accountNumber);
    }
}
