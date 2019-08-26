package bank.transaction.service;

import bank.account.model.Account;
import bank.account.service.AccountService;
import bank.transaction.exception.TransactionProcessingException;
import bank.transaction.model.Transaction;
import bank.transaction.model.TransactionStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransactionServiceTest {

    private static final AccountService accountService = AccountService.getInstance();
    private static final TransactionService transactionService = TransactionService.getInstance();

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
}
