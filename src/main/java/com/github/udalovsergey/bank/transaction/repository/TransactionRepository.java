package com.github.udalovsergey.bank.transaction.repository;

import com.github.udalovsergey.bank.repository.Repository;
import com.github.udalovsergey.bank.transaction.model.Transaction;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TransactionRepository implements Repository<Transaction> {

    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private AtomicLong idCounter = new AtomicLong(0);

    @Override
    public Transaction get(long id) {
        Transaction transaction = transactions.get(id);
        return transaction == null ? null : new Transaction(transaction);
    }

    @Override
    public Collection<Transaction> getAll() {
        return transactions.values().stream()
                .map(Transaction::new)
                .collect(Collectors.toList());
    }

    @Override
    public Transaction put(Transaction transaction) {
        long currentId = idCounter.getAndIncrement();
        transactions.put(currentId, new Transaction(currentId, transaction.getAccountFromId(),
                transaction.getAccountToId(), transaction.getAmount(), transaction.getStatus()));
        transaction.setId(currentId);
        return transaction;
    }

    @Override
    public void update(Transaction transaction) {
        transactions.put(transaction.getId(), new Transaction(transaction));
    }

    @Override
    public void remove(Transaction transaction) {
        transactions.remove(transaction.getId());
    }
}
