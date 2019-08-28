package com.github.udalovsergey.bank.transaction.service;

import com.github.udalovsergey.bank.account.model.Account;
import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.transaction.exception.TransactionProcessingException;
import com.github.udalovsergey.bank.transaction.model.Transaction;
import com.github.udalovsergey.bank.transaction.model.TransactionStatus;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represents a transaction processor, which workers take transactions from a queue, and then process them with
 * synchronisation.
 * Attention - the processing order of transactions is not guaranteed, because two different workers can get different
 * transactions of the same account. More complicated logic is needed (e.g. one thread per account).
 */
public class TransactionExecutor {

    private static final int THREAD_PULL_SIZE = 10;

    private final Executor executor = Executors.newFixedThreadPool(THREAD_PULL_SIZE);
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final Lock distributedLock;

    public TransactionExecutor(AccountService accountService, TransactionService transactionService, Lock distributedLock) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.distributedLock = distributedLock;
    }

    public void start() {
        for (int i = 0; i < THREAD_PULL_SIZE; i++) {
            executor.execute(new Worker(i));
        }
    }

    protected class Worker implements Runnable {

        private final int workerId;

        Worker(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            BlockingQueue<Transaction> queue = transactionService.getQueue();
            try {
                while (true) {
                    Transaction transaction = queue.take();
                    process(transaction);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public int getWorkerId() {
            return workerId;
        }

        void process(Transaction transaction) {
            long accountFromId = transaction.getAccountFromId();
            long accountToId = transaction.getAccountToId();
            Object lock1 = accountFromId < accountToId ? distributedLock.get(accountFromId) : distributedLock.get(accountToId);
            Object lock2 = accountFromId < accountToId ? distributedLock.get(accountToId) : distributedLock.get(accountFromId);
            synchronized (lock1) {
                synchronized (lock2) {
                    if (transaction.getStatus() != TransactionStatus.NEW) {
                        throw new TransactionProcessingException(accountFromId,
                                accountToId,
                                transaction.getAmount(),
                                String.format("Transaction has the wrong state. Expected [%s], but has [%s]",
                                        TransactionStatus.NEW, transaction.getStatus()));
                    }
                    Account accountFrom = accountService.get(accountFromId);
                    Account accountTo = accountService.get(accountToId);
                    BigDecimal amountToTransfer = transaction.getAmount();

                    BigDecimal newBlockedAmount = accountFrom.getBlockedAmount().subtract(amountToTransfer);
                    BigDecimal newAmount = accountFrom.getAmount().subtract(amountToTransfer);

                    if (newBlockedAmount.compareTo(BigDecimal.ZERO) < 0 || newAmount.compareTo(BigDecimal.ZERO) < 0) {
                        transaction.setStatus(TransactionStatus.FAILED);
                    } else {
                        accountFrom.setBlockedAmount(newBlockedAmount);
                        accountFrom.setAmount(newAmount);
                        accountService.updateAccount(accountFrom);

                        accountTo.setAmount(accountTo.getAmount().add(amountToTransfer));
                        accountService.updateAccount(accountTo);

                        transaction.setStatus(TransactionStatus.SUCCESSFUL);
                    }
                    transactionService.updateTransaction(transaction);
                }
            }
        }
    }


}
