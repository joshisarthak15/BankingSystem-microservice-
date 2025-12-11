package com.accountservice.service;

import com.accountservice.document.Account;
import com.accountservice.exception.AccountNotFoundException;
import com.accountservice.repository.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account sampleAccount;

    @BeforeEach
    void setup() {
        sampleAccount = new Account();
        sampleAccount.setId("1");
        sampleAccount.setAccountNumber("SAR1234");
        sampleAccount.setHolderName("Sarthak Joshi");
        sampleAccount.setStatus("ACTIVE");
        sampleAccount.setBalance(500.0);
        sampleAccount.setCreatedAt(new Date());
    }

    // -------------------------------------------------------
    // CREATE ACCOUNT
    // -------------------------------------------------------
    @Test
    void testCreateAccount() {

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.createAccount("Sarthak Joshi");

        assertNotNull(result);
        assertEquals("Sarthak Joshi", result.getHolderName());
        assertEquals(0.0, result.getBalance());
        assertEquals("ACTIVE", result.getStatus());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    // -------------------------------------------------------
    // GET ACCOUNT - Success
    // -------------------------------------------------------
    @Test
    void testGetAccountSuccess() {

        when(accountRepository.findByAccountNumber("SAR1234"))
                .thenReturn(Optional.of(sampleAccount));

        Account result = accountService.getAccount("SAR1234");

        assertNotNull(result);
        assertEquals("SAR1234", result.getAccountNumber());
        assertEquals("Sarthak Joshi", result.getHolderName());
    }

    // -------------------------------------------------------
    // GET ACCOUNT - Not Found
    // -------------------------------------------------------
    @Test
    void testGetAccountNotFound() {

        when(accountRepository.findByAccountNumber("SAR0000"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccount("SAR0000"));
    }

    // -------------------------------------------------------
    // UPDATE ACCOUNT
    // -------------------------------------------------------
    @Test
    void testUpdateAccount() {

        when(accountRepository.findByAccountNumber("SAR1234"))
                .thenReturn(Optional.of(sampleAccount));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account updated = accountService.updateAccount("SAR1234", "Rohit Sharma");

        assertNotNull(updated);
        assertEquals("Rohit Sharma", updated.getHolderName());
    }

    // -------------------------------------------------------
    // UPDATE BALANCE
    // -------------------------------------------------------
    @Test
    void testUpdateBalance() {

        when(accountRepository.findByAccountNumber("SAR1234"))
                .thenReturn(Optional.of(sampleAccount));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account updated = accountService.updateBalance("SAR1234", 2000.0);

        assertEquals(2000.0, updated.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    // -------------------------------------------------------
    // DELETE ACCOUNT - Success
    // -------------------------------------------------------
    @Test
    void testDeleteAccountSuccess() {

        when(accountRepository.existsByAccountNumber("SAR1234"))
                .thenReturn(true);

        doNothing().when(accountRepository).deleteByAccountNumber("SAR1234");

        assertDoesNotThrow(() -> accountService.deleteAccount("SAR1234"));
        verify(accountRepository, times(1)).deleteByAccountNumber("SAR1234");
    }

    // -------------------------------------------------------
    // DELETE ACCOUNT - Not Found
    // -------------------------------------------------------
    @Test
    void testDeleteAccountNotFound() {

        when(accountRepository.existsByAccountNumber("SAR0000"))
                .thenReturn(false);

        assertThrows(AccountNotFoundException.class,
                () -> accountService.deleteAccount("SAR0000"));
    }
}
