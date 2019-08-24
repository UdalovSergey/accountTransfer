package account.service;

import account.exception.TransactionProcessingException;
import account.model.Account;
import account.model.Transaction;
import account.model.TransactionStatus;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionExecutor {

    private final static int THREAD_PULL_SIZE = 10;

    private final Executor executor = Executors.newFixedThreadPool(THREAD_PULL_SIZE);
    private final TransactionService transactionService;
    private final AccountService accountService = AccountService.getInstance();

    public TransactionExecutor(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void start() {
        for (int i = 0; i < THREAD_PULL_SIZE; i++) {
            executor.execute(new Worker(i));
        }
    }

    private class Worker implements Runnable {

        private final int workerId;

        private Worker(int workerId) {
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
                e.printStackTrace();
            }
        }

        public int getWorkerId() {
            return workerId;
        }

        /*
            Attention - the processing order of the transaction is not guaranteed, because two different worker can get different
            Transactions of the same account.
         */
        private void process(Transaction transaction) {
            Long accountFromId = transaction.getAccountFromId();
            Long accountToId = transaction.getAccountToId();
            Object lock1 = accountFromId < accountToId ? accountFromId : accountToId;
            Object lock2 = accountFromId < accountToId ? accountToId : accountFromId;
            synchronized (lock1) {
                synchronized (lock2) {
                    if (transaction.getStatus() != TransactionStatus.NEW) {
                        throw new TransactionProcessingException(accountFromId,
                                accountToId,
                                transaction.getAmount(),
                                String.format("Transaction has the wrong state. Expected [%s], but has [%s]", TransactionStatus.NEW, transaction.getStatus()));
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

                    System.out.println("Worker id " + workerId + "  transaction id" + transaction.getId() + " hashL1:" + lock2.hashCode());
                }
            }

        }
    }


}
