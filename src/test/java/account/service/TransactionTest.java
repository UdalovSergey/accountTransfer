package account.service;

import account.model.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TransactionTest {

    @Test
    public void test() throws InterruptedException {
        TransactionService service = TransactionService.getInstance();
        TransactionExecutor executor = new TransactionExecutor(service);
        AccountService accountService = AccountService.getInstance();

        Account account1 = accountService.addAccount("Ivan", new BigDecimal(1000));
        Account account2 = accountService.addAccount("Vasya", new BigDecimal(1000));

        for (int i = 0; i < 99; i++) {
            service.createNewTransaction(account1.getId(), account2.getId(), new BigDecimal(10));
        }

        Thread.sleep(1000);
        System.out.println(accountService.get(account1.getId()).getAmount());
        System.out.println(accountService.get(account2.getId()).getAmount());
    }
}
