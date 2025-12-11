package com.transactionservice.service;

import com.transactionservice.document.Transaction;
import com.transactionservice.exception.TransactionException;
import com.transactionservice.feign.AccountClient;
import com.transactionservice.feign.NotificationClient;
import com.transactionservice.repository.TransactionRepository;
import com.transactionservice.util.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransactionService transactionService;

    private Map<String, Object> accountData;
    private ApiResponse successAccountResp;

    @BeforeEach
    void setup() {
        accountData = new HashMap<>();
        accountData.put("balance", 5000.0);

        successAccountResp = new ApiResponse("Fetched", accountData, true);
    }


    // ------------------------------------------------------
    // DEPOSIT TESTS
    // ------------------------------------------------------
    @Test
    void testDepositSuccess() {

        when(accountClient.getAccount("SAR1234"))
                .thenReturn(successAccountResp);

        when(accountClient.updateBalance(eq("SAR1234"), anyMap()))
                .thenReturn(new ApiResponse("OK", null, true));

        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-123");
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(txn);

        Transaction result = transactionService.deposit("SAR1234", 1000.0);

        assertNotNull(result);
        assertEquals("DEPOSIT", result.getType());
        verify(accountClient, times(1)).getAccount("SAR1234");
        verify(accountClient, times(1)).updateBalance(eq("SAR1234"), anyMap());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testDepositInvalidAmount() {
        assertThrows(TransactionException.class,
                () -> transactionService.deposit("SAR1234", -10.0));
    }

    @Test
    void testDepositAccountNotFound() {

        when(accountClient.getAccount("SAR1234"))
                .thenReturn(new ApiResponse("Not found", null, false));

        assertThrows(TransactionException.class,
                () -> transactionService.deposit("SAR1234", 1000.0));
    }


    // ------------------------------------------------------
    // WITHDRAW TESTS
    // ------------------------------------------------------
    @Test
    void testWithdrawSuccess() {

        when(accountClient.getAccount("SAR1234"))
                .thenReturn(successAccountResp);

        when(accountClient.updateBalance(eq("SAR1234"), anyMap()))
                .thenReturn(new ApiResponse("OK", null, true));

        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-456");

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(txn);

        Transaction result = transactionService.withdraw("SAR1234", 2000.0);

        assertEquals("WITHDRAW", result.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdrawInvalidAmount() {
        assertThrows(TransactionException.class,
                () -> transactionService.withdraw("SAR1234", -100.0));
    }

    @Test
    void testWithdrawInsufficientBalance() {

        accountData.put("balance", 100.0);

        when(accountClient.getAccount("SAR1234"))
                .thenReturn(new ApiResponse("Fetched", accountData, true));

        assertThrows(TransactionException.class,
                () -> transactionService.withdraw("SAR1234", 200.0));
    }


    // ------------------------------------------------------
    // TRANSFER TESTS
    // ------------------------------------------------------
    @Test
    void testTransferSuccess() {

        // Source account
        Map<String, Object> sData = new HashMap<>();
        sData.put("balance", 5000.0);

        // Destination
        Map<String, Object> dData = new HashMap<>();
        dData.put("balance", 1000.0);

        ApiResponse sResp = new ApiResponse("Fetched", sData, true);
        ApiResponse dResp = new ApiResponse("Fetched", dData, true);

        when(accountClient.getAccount("SRC123")).thenReturn(sResp);
        when(accountClient.getAccount("DEST123")).thenReturn(dResp);

        when(accountClient.updateBalance(eq("SRC123"), anyMap()))
                .thenReturn(new ApiResponse("OK", null, true));

        when(accountClient.updateBalance(eq("DEST123"), anyMap()))
                .thenReturn(new ApiResponse("OK", null, true));

        Transaction txn = new Transaction();
        txn.setTransactionId("TXN-789");

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(txn);

        Transaction result = transactionService.transfer("SRC123", "DEST123", 1000.0);

        assertEquals("TRANSFER", result.getType());
        assertEquals("SRC123", result.getSourceAccount());
        assertEquals("DEST123", result.getDestinationAccount());
    }

    @Test
    void testTransferSameSourceDest() {
        assertThrows(TransactionException.class,
                () -> transactionService.transfer("ACC1", "ACC1", 500.0));
    }

    @Test
    void testTransferInsufficientBalance() {

        accountData.put("balance", 300.0);

        when(accountClient.getAccount("SRC123"))
                .thenReturn(new ApiResponse("Fetched", accountData, true));

        assertThrows(TransactionException.class,
                () -> transactionService.transfer("SRC123", "DEST1", 1000.0));
    }

    @Test
    void testTransferDestinationNotFound() {

        when(accountClient.getAccount("SRC123"))
                .thenReturn(successAccountResp);

        when(accountClient.getAccount("DEST123"))
                .thenReturn(new ApiResponse("Not found", null, false));

        assertThrows(TransactionException.class,
                () -> transactionService.transfer("SRC123", "DEST123", 500.0));
    }


    // ------------------------------------------------------
    // FALLBACK TESTS
    // ------------------------------------------------------
    @Test
    void testGetAccountFallback() {

        Throwable ex = new RuntimeException("Service down");

        assertThrows(TransactionException.class,
                () -> transactionService.accountGetFallback("ACC1", ex));
    }

    @Test
    void testUpdateBalanceFallback() {

        Throwable ex = new RuntimeException("LB error");

        assertThrows(TransactionException.class,
                () -> transactionService.accountUpdateFallback("ACC1", new HashMap<>(), ex));
    }
}
