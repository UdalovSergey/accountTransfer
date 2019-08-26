package bank.transaction.service;

import bank.account.model.Account;
import bank.account.service.AccountService;
import bank.transaction.model.TransactionStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionExecutorIntegrationTest {

    private static final AccountService accountService = AccountService.getInstance();
    private static final TransactionService transactionService = TransactionService.getInstance();

    @BeforeAll
    public static void init() {
        accountService.addAccount("From", BigDecimal.valueOf(1000));
        accountService.addAccount("To", BigDecimal.valueOf(1000));
    }

    @Test
    public void concurrentCreationAndExecutionTest() throws InterruptedException {
        TransactionExecutor transactionExecutor = new TransactionExecutor(transactionService);
        transactionExecutor.start();

        Account accountFrom = accountService.addAccount("From", BigDecimal.valueOf(1000));
        Account accountTo = accountService.addAccount("To", BigDecimal.valueOf(1000));

        int transactionsNumber = 1000;
        BigDecimal amountToTransfer = BigDecimal.ONE;
        BigDecimal totalToTransfer = amountToTransfer.multiply(BigDecimal.valueOf(transactionsNumber));
        BigDecimal expectedFromAmount = accountFrom.getAmount().subtract(totalToTransfer);
        BigDecimal expectedFromBlockedAmount = BigDecimal.ZERO;
        BigDecimal expectedToAmount = accountTo.getAmount().add(totalToTransfer);

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < transactionsNumber; i++) {
            ids.add(transactionService.createNewTransaction(accountFrom.getId(), accountTo.getId(), amountToTransfer).getId());
        }

        waitForTheCompletion(ids);

        Assertions.assertEquals(expectedFromAmount, accountService.get(accountFrom.getId()).getAmount());
        Assertions.assertEquals(expectedFromBlockedAmount, accountService.get(accountFrom.getId()).getBlockedAmount());
        Assertions.assertEquals(expectedToAmount, accountService.get(accountTo.getId()).getAmount());
    }

    private void waitForTheCompletion(List<Long> ids) throws InterruptedException {
        Thread thread = new Thread(() -> {
            boolean check = false;
            while (!check) {
                for (long id : ids) {
                    check = transactionService.getById(id).getStatus().equals(TransactionStatus.SUCCESSFUL);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join(5000);
        boolean check = false;
        for (long id : ids) {
            check = transactionService.getById(id).getStatus().equals(TransactionStatus.SUCCESSFUL);
        }
        if (!check) {
            throw new RuntimeException("Something went wrong - the test has been working longer that 5 sec.");
        }
    }
}
