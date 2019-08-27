package com.github.udalovsergey.bank.account.service;

import com.github.udalovsergey.bank.account.model.Account;
import com.github.udalovsergey.bank.account.repository.AccountRepository;
import com.github.udalovsergey.bank.repository.Repository;

import java.math.BigDecimal;
import java.util.Collection;

public class AccountService {

    private final Repository<Account> repository = new AccountRepository();

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
