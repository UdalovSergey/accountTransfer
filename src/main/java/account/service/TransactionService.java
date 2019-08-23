package account.service;

import account.model.Account;
import account.model.Transaction;
import account.model.TransactionStatus;
import account.repository.TransactionInmemoryRepository;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransactionService {

    private static TransactionService service;
    private final AccountService accountService = AccountService.getInstance();

    private TransactionInmemoryRepository repository = TransactionInmemoryRepository.getInstance();
    private BlockingQueue<Transaction> queue = new LinkedBlockingQueue<>();

    private TransactionService() {

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


    public Transaction createNewTransaction(long accFromId, long accToId, BigDecimal amountToTransfer) {
        //TODO: Lock on FromAccount and create a new transaction
        Long lock = accFromId;
        Transaction newTransaction;
        synchronized (lock) {
            Account accountFrom = accountService.get(accFromId);
            if (accountFrom.getAmount().subtract(accountFrom.getBlockedAmount()).compareTo(amountToTransfer) < 0) {
                throw new RuntimeException("Not enough money");
            }
            accountFrom.setBlockedAmount(accountFrom.getBlockedAmount().add(amountToTransfer));
            accountService.updateAccount(accountFrom);

            Transaction transaction = new Transaction(accFromId, accToId, amountToTransfer);
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
