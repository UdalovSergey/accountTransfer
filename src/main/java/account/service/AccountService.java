package account.service;

import account.model.Transaction;
import account.model.TransactionStatus;
import repository.Repository;
import account.repository.AccountInmemoryRepository;
import account.model.Account;

import java.math.BigDecimal;
import java.util.Collection;

public class AccountService {

    private static AccountService service;

    private final Repository<Account> repository = new AccountInmemoryRepository();


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

    //TODO: id creation
    public Account addAccount(String ownerName, BigDecimal amount) {
        return repository.put(new Account(ownerName, amount));
    }

    //TODO: it is not a threadsafe operation. Because amount can be changed accidentally
    public void updateAccount(Account account) {
        repository.update(account);
    }



}
