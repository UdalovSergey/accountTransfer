package bank.account.service;

import bank.transaction.service.TransactionService;
import bank.account.model.Account;
import bank.account.repository.AccountRepository;
import bank.repository.Repository;

import java.math.BigDecimal;
import java.util.Collection;

public class AccountService {

    private static AccountService service;

    private final Repository<Account> repository = new AccountRepository();

    private AccountService() {

    }

    public static AccountService getInstance() {
        if (service == null) {
            synchronized (TransactionService.class) {
                if (service == null) {
                    service = new AccountService();
                }
            }
        }
        return service;
    }

    public Account get(long id) {
        return repository.get(id);
    }

    public Collection<Account> getAll() {
        return repository.getAll();
    }

    public Account addAccount(String ownerName, BigDecimal amount) {
        return repository.put(new Account(ownerName, amount));
    }

    public void updateAccount(Account account) {
        repository.update(account);
    }

}
