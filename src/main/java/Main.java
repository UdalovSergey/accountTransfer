import bank.transaction.service.TransactionExecutor;
import bank.transaction.service.TransactionService;
import com.sun.net.httpserver.HttpServer;
import bank.controller.AccountHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;

    public static void main(final String... args) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.createContext("/accounts", new AccountHandler());
        server.start();

        TransactionExecutor transactionExecutor = new TransactionExecutor(TransactionService.getInstance());
        transactionExecutor.start();
    }
}
