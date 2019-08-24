package account.service;

import account.exception.AccountNotFoundException;
import account.exception.TransactionProcessingException;
import account.model.Account;
import account.model.Transaction;
import account.model.TransactionStatus;
import account.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransactionService {

    private static TransactionService service;
    private final AccountService accountService = AccountService.getInstance();

    private TransactionRepository repository = TransactionRepository.getInstance();
    private BlockingQueue<Transaction> queue = new LinkedBlockingQueue<>();

    private TransactionService() {
        TransactionExecutor transactionExecutor = new TransactionExecutor(this);
        transactionExecutor.start();
    }

    public static TransactionService getInstance() {
        if (service == null) {
            synchronized (TransactionService.class) {
                if (service == null) {
                    service = new TransactionService();
                }
            }
        }
        return service;
    }

    public Transaction createNewTransaction(long accountFromId, long accountToId, BigDecimal amountToTransfer) {
        Long lock = accountFromId;
        Transaction newTransaction;
        synchronized (lock) {
            Account accountFrom = accountService.get(accountFromId);
            Account accountTo = accountService.get(accountToId);
            if (accountFrom == null) {
                throw new AccountNotFoundException(accountFromId);
            } else if (accountTo == null) {
                throw new AccountNotFoundException(accountToId);
            }

            if (amountToTransfer.compareTo(BigDecimal.ZERO) <= 0) {
                throw new TransactionProcessingException(accountFromId, accountToId, amountToTransfer,
                        "Transactions with zero and negative amount is not allowed");
            }

            if (accountFromId == accountToId) {
                throw new TransactionProcessingException(accountFromId, accountToId, amountToTransfer,
                        "Sender and receiver should be different");
            }
            if (accountFrom.getAmount().subtract(accountFrom.getBlockedAmount()).compareTo(amountToTransfer) < 0) {
                throw new TransactionProcessingException(accountFromId, accountToId, amountToTransfer,
                        "Not enough money");
            }

            accountFrom.setBlockedAmount(accountFrom.getBlockedAmount().add(amountToTransfer));
            accountService.updateAccount(accountFrom);

            Transaction transaction = new Transaction(accountFromId, accountToId, amountToTransfer);
            transaction.setStatus(TransactionStatus.NEW);
            newTransaction = repository.put(transaction);
        }
        queue.add(newTransaction);
        return newTransaction;
    }

    public void updateTransaction(Transaction transaction) {
        repository.update(transaction);
    }

    BlockingQueue<Transaction> getQueue() {
        return queue;
    }


}
