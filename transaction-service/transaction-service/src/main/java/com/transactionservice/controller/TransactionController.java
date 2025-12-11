package com.transactionservice.controller;

import com.transactionservice.document.Transaction;
import com.transactionservice.service.TransactionService;
import com.transactionservice.util.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    // ---------------- Deposit ----------------
    @PostMapping(value = "/deposit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse deposit(@RequestBody Map<String, Object> body) {

        logger.info("POST /api/transactions/deposit requestBody={}", body);

        String accountNumber = body.get("accountNumber").toString();
        Double amount = Double.valueOf(body.get("amount").toString());

        Transaction txn = transactionService.deposit(accountNumber, amount);

        logger.info("Deposit Successful: txnId={} account={} amount={}",
                txn.getTransactionId(), accountNumber, amount);

        return new ApiResponse("Deposit Successful", txn, true);
    }

    // ---------------- Withdraw ----------------
    @PostMapping(value = "/withdraw", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse withdraw(@RequestBody Map<String, Object> body) {

        logger.info("POST /api/transactions/withdraw requestBody={}", body);

        String accountNumber = body.get("accountNumber").toString();
        Double amount = Double.valueOf(body.get("amount").toString());

        Transaction txn = transactionService.withdraw(accountNumber, amount);

        logger.info("Withdraw Successful: txnId={} account={} amount={}",
                txn.getTransactionId(), accountNumber, amount);

        return new ApiResponse("Withdraw Successful", txn, true);
    }

    // ---------------- Transfer ----------------
    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse transfer(@RequestBody Map<String, Object> body) {

        logger.info("POST /api/transactions/transfer requestBody={}", body);

        String source = body.get("sourceAccount").toString();
        String destination = body.get("destinationAccount").toString();
        Double amount = Double.valueOf(body.get("amount").toString());

        Transaction txn = transactionService.transfer(source, destination, amount);

        logger.info("Transfer Successful: txnId={} source={} destination={} amount={}",
                txn.getTransactionId(), source, destination, amount);

        return new ApiResponse("Transfer Successful", txn, true);
    }

    // ---------------- Transaction History ----------------
    @GetMapping("/{accountNumber}")
    public ApiResponse getTransactions(@PathVariable String accountNumber) {

        logger.info("GET /api/transactions/{} - Fetching history", accountNumber);

        return new ApiResponse(
                "Transactions fetched",
                transactionService.getTransactions(accountNumber),
                true
        );
    }
}
