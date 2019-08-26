package bank;

import bank.transaction.service.TransactionExecutor;
import bank.transaction.service.TransactionService;
import com.sun.net.httpserver.HttpServer;
import bank.controller.AccountHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BankApplication {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;

    public static void main(final String... args) throws IOException {
        startServer();
        startTransactionExecutor();
    }

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.createContext("/accounts", new AccountHandler());
        server.start();
    }

    public static void startTransactionExecutor() {
        TransactionExecutor transactionExecutor = new TransactionExecutor(TransactionService.getInstance());
        transactionExecutor.start();
    }
}
