package bank.transaction.service;

import bank.account.model.Account;
import bank.account.service.AccountService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransactionTest {

    @Test
    public void test() throws InterruptedException {
        TransactionService service = TransactionService.getInstance();
        AccountService accountService = AccountService.getInstance();

        Account account1 = accountService.addAccount("Ivan", new BigDecimal(1000));
        Account account2 = accountService.addAccount("Vasya", new BigDecimal(1000));

        for (int i = 0; i < 99; i++) {
            service.createNewTransaction(account1.getId(), account2.getId(), new BigDecimal(10));
        }

        Thread.sleep(1000);
        Account acc1 = accountService.get(account1.getId());
        Account acc2 = accountService.get(account2.getId());
        System.out.println(acc1.getAmount() + " blocked " + acc1.getBlockedAmount());
        System.out.println(acc2.getAmount() + " blocked " + acc2.getBlockedAmount());
    }
}
