package com.transactionservice.repository;

import com.transactionservice.document.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findBySourceAccountOrDestinationAccount(String source, String dest);
}
