package com.github.udalovsergey.bank.transaction.service;

import com.github.udalovsergey.bank.account.model.Account;
import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.transaction.exception.TransactionProcessingException;
import com.github.udalovsergey.bank.transaction.model.Transaction;
import com.github.udalovsergey.bank.transaction.model.TransactionStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransactionServiceTest {

    private final AccountService accountService = new AccountService();
    private final TransactionService transactionService = new TransactionService(accountService, new Lock());

    @Test
    public void createNewTransaction() {
        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));
        BigDecimal expectedFromAmount = accountFrom.getAmount();
        BigDecimal expectedToAmount = accountTo.getAmount();

        Transaction actualTransaction = transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), BigDecimal.valueOf(500));

        Assertions.assertEquals(TransactionStatus.NEW, actualTransaction.getStatus());
        Assertions.assertEquals(BigDecimal.valueOf(500), accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());

        transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), BigDecimal.valueOf(200));
        Assertions.assertEquals(BigDecimal.valueOf(700), accountService.get(accountFrom.getId()).getBlockedAmount());
    }

    @Test
    public void createNewTransactionException() {
        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), null));

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), BigDecimal.ZERO),
                "Exception expected when amount is zero or below");

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), BigDecimal.valueOf(-1)),
                "Exception expected when amount is zero or below");

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(accountFrom.getId(), accountFrom.getId(), BigDecimal.valueOf(500)),
                "Exception expected when accountFrom and accountTo are the same");

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(accountFrom.getId(), accountFrom.getId(), BigDecimal.valueOf(1500)),
                "Exception expected when accountFrom and accountTo are the same");
    }
}
