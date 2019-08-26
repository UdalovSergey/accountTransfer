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

public class TransactionExecutorTest {

    private static final AccountService accountService = AccountService.getInstance();
    private static final TransactionService transactionService = TransactionService.getInstance();

    @BeforeAll
    public static void init() {
        accountService.addAccount("From", BigDecimal.valueOf(1000));
        accountService.addAccount("To", BigDecimal.valueOf(1000));
    }

    @Test
    public void transactionExecutionTestSuccess() {
        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));
        BigDecimal amountToTransfer = BigDecimal.valueOf(700);
        BigDecimal expectedFromAmount = accountFrom.getAmount().subtract(amountToTransfer);
        BigDecimal expectedFromBlockedAmount = BigDecimal.ZERO;
        BigDecimal expectedToAmount = accountTo.getAmount().add(amountToTransfer);

        Transaction transaction = transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), amountToTransfer);
        TransactionExecutor.Worker worker = new TransactionExecutor(transactionService).new Worker(1);
        worker.process(transaction);

        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedFromBlockedAmount, accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());
        Assertions.assertEquals(TransactionStatus.SUCCESSFUL, transactionService.getById(transaction.getId()).getStatus());
    }

    @Test
    public void transactionExecutionTestFail() {
        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));
        BigDecimal amountToTransfer = BigDecimal.valueOf(1100);
        BigDecimal expectedFromAmount = accountFrom.getAmount();
        BigDecimal expectedFromBlockedAmount = BigDecimal.ZERO;
        BigDecimal expectedToAmount = accountTo.getAmount();

        Transaction transaction = new Transaction(accountFrom.getId(), accountTo.getId(), amountToTransfer, TransactionStatus.NEW);
        TransactionExecutor.Worker worker = new TransactionExecutor(transactionService).new Worker(1);
        worker.process(transaction);

        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedFromBlockedAmount, accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionService.getById(transaction.getId()).getStatus());
    }

    @Test
    public void transactionExecutionTestWrongState() {
        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));
        BigDecimal amountToTransfer = BigDecimal.valueOf(1100);

        Transaction transaction = new Transaction(accountFrom.getId(), accountTo.getId(), amountToTransfer, TransactionStatus.SUCCESSFUL);
        TransactionExecutor.Worker worker = new TransactionExecutor(transactionService).new Worker(1);
        Assertions.assertThrows(TransactionProcessingException.class, () -> worker.process(transaction),
                "Exception expected when transaction has status different from [NEW]");

    }

    @Test
    public void createNewTransactionException() {
        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(0, 1, BigDecimal.ZERO),
                "Exception expected when amount is zero or below");

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(0, 1, BigDecimal.valueOf(-1)),
                "Exception expected when amount is zero or below");

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(0, 0, BigDecimal.valueOf(500)),
                "Exception expected when accountFrom and accountTo are the same");

        Assertions.assertThrows(TransactionProcessingException.class,
                () -> transactionService.createNewTransaction(0, 1, BigDecimal.valueOf(1500)),
                "Exception expected when accountFrom and accountTo are the same");
    }
}
