package bank.transaction.service;

import bank.account.model.Account;
import bank.account.service.AccountService;
import bank.transaction.exception.TransactionProcessingException;
import bank.transaction.model.Transaction;
import bank.transaction.model.TransactionStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransactionExecutorTest {

    private AccountService accountService;
    private TransactionService transactionService;
    private Account accountFrom;
    private Account accountTo;
    private TransactionExecutor.Worker worker;


    @BeforeEach
    public void init() {
        accountService = new AccountService();
        transactionService = new TransactionService(accountService);
        accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));
        worker = new TransactionExecutor(accountService, transactionService).new Worker(1);
    }

    @Test
    public void transactionExecutionTestSuccess() {
        BigDecimal amountToTransfer = BigDecimal.valueOf(700);
        BigDecimal expectedFromAmount = accountFrom.getAmount().subtract(amountToTransfer);
        BigDecimal expectedFromBlockedAmount = BigDecimal.ZERO;
        BigDecimal expectedToAmount = accountTo.getAmount().add(amountToTransfer);

        Transaction transaction = transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), amountToTransfer);
        worker.process(transaction);

        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedFromBlockedAmount, accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());
        Assertions.assertEquals(TransactionStatus.SUCCESSFUL, transactionService.getById(transaction.getId()).getStatus());
    }

    @Test
    public void transactionExecutionTestFail() {
        BigDecimal amountToTransfer = BigDecimal.valueOf(1100);
        BigDecimal expectedFromAmount = accountFrom.getAmount();
        BigDecimal expectedFromBlockedAmount = BigDecimal.ZERO;
        BigDecimal expectedToAmount = accountTo.getAmount();

        Transaction transaction = new Transaction(accountFrom.getId(), accountTo.getId(), amountToTransfer, TransactionStatus.NEW);
        worker.process(transaction);

        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedFromBlockedAmount, accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionService.getById(transaction.getId()).getStatus());
    }

    @Test
    public void transactionExecutionTestWrongState() {
        BigDecimal amountToTransfer = BigDecimal.valueOf(1100);

        Transaction transaction = new Transaction(accountFrom.getId(), accountTo.getId(), amountToTransfer, TransactionStatus.SUCCESSFUL);
        Assertions.assertThrows(TransactionProcessingException.class, () -> worker.process(transaction),
                "Exception expected when transaction has status different from [NEW]");

    }

}
