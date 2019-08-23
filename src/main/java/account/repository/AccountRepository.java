package account.repository;

import repository.Repository;
import account.model.Account;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AccountRepository implements Repository<Account> {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private AtomicLong idCounter = new AtomicLong(0);

    @Override
    public Account get(long id) {
        Account account = accounts.get(id);
        return account == null ? null : new Account(account);
    }

    @Override
    public Collection<Account> getAll() {
        return accounts.values().stream()
                .map(account -> new Account(account))
                .collect(Collectors.toList());
    }

    @Override
    public Account put(Account account) {
        long currentId = idCounter.getAndIncrement();
        accounts.put(currentId, new Account(currentId, account.getOwnerName(), account.getAmount()));
        account.setId(currentId);
        return account;
    }

    @Override
    public void update(Account account) {
        accounts.put(account.getId(), new Account(account));
    }

    @Override
    public void remove(Account account) {
        accounts.remove(account.getId());
    }
}
