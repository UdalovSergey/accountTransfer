package bank.transaction.service;

import bank.account.model.Account;
import bank.account.service.AccountService;
import bank.transaction.model.Transaction;
import bank.transaction.model.TransactionStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionExecutorIntegrationTest {

    private AccountService accountService;
    private TransactionService transactionService;
    private Lock distributedLock;

    @BeforeEach
    public void init() {
        distributedLock = new Lock();
        accountService = new AccountService();
        transactionService = new TransactionService(accountService, distributedLock);
    }

    @Test
    public void concurrentCreationAndExecutionTest() throws InterruptedException {
        TransactionExecutor transactionExecutor = new TransactionExecutor(accountService, transactionService, distributedLock);
        transactionExecutor.start();

        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));

        int transactionsNumber = 1000;
        BigDecimal amountToTransfer = BigDecimal.ONE;
        BigDecimal totalToTransfer = amountToTransfer.multiply(BigDecimal.valueOf(transactionsNumber));
        BigDecimal expectedFromAmount = accountFrom.getAmount().subtract(totalToTransfer);
        BigDecimal expectedFromBlockedAmount = BigDecimal.ZERO;
        BigDecimal expectedToAmount = accountTo.getAmount().add(totalToTransfer);

        createTransactionsInParallel(transactionsNumber, accountFrom, accountTo, amountToTransfer);
        waitForTheCompletion(transactionsNumber);

        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedFromBlockedAmount, accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());
    }

    private void createTransactionsInParallel(int transactionsNumber, Account accountFrom, Account accountTo, BigDecimal amountToTransfer) {
        Executor executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < transactionsNumber; i++) {
            executor.execute(() -> transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), amountToTransfer));
        }
    }

    private void waitForTheCompletion(int transactionsNumber) throws InterruptedException {
        Thread thread = new Thread(() -> {
            boolean check = false;
            while (!check) {
                check = checkTransactionForSuccess(transactionsNumber);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join(10000);
        boolean check = checkTransactionForSuccess(transactionsNumber);
        if (!check) {
            throw new RuntimeException("Something went wrong - the test has been working longer that 10 sec.");
        }
    }

    private boolean checkTransactionForSuccess(int transactionsNumber) {
        boolean check = false;
        for (long id = 0; id < transactionsNumber; id++) {
            Transaction transaction = transactionService.getById(id);
            check = transaction != null && transaction.getStatus().equals(TransactionStatus.SUCCESSFUL);
        }
        return check;
    }
}
