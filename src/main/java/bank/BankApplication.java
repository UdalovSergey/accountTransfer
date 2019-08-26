package bank;

import bank.account.service.AccountService;
import bank.controller.AccountHandler;
import bank.transaction.service.TransactionExecutor;
import bank.transaction.service.TransactionService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BankApplication {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;

    public static void main(final String... args) throws IOException {
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService(accountService);
        startServer(accountService, transactionService);
        startTransactionExecutor(accountService, transactionService);
    }

    public static void startServer(AccountService accountService, TransactionService transactionService) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.createContext("/accounts", new AccountHandler(accountService, transactionService));
        server.start();
    }

    public static void startTransactionExecutor(AccountService accountService, TransactionService transactionService) {
        TransactionExecutor transactionExecutor = new TransactionExecutor(accountService, transactionService);
        transactionExecutor.start();
    }
}
