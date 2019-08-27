package com.github.udalovsergey.bank;

import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.controller.AccountHandler;
import com.github.udalovsergey.bank.controller.AccountServlet;
import com.github.udalovsergey.bank.transaction.service.Lock;
import com.github.udalovsergey.bank.transaction.service.TransactionExecutor;
import com.github.udalovsergey.bank.transaction.service.TransactionService;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class BankApplication {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;

    public static void main(final String... args) throws Exception {
        AccountService accountService = new AccountService();
        Lock distributedLock = new Lock();
        TransactionService transactionService = new TransactionService(accountService, distributedLock);
        //startServer(accountService, transactionService);
        startJettyServer(accountService, transactionService);
        startTransactionExecutor(accountService, transactionService, distributedLock);
    }

    public static void startServer(AccountService accountService, TransactionService transactionService) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.createContext("/accounts", new AccountHandler(accountService, transactionService));
        server.start();
    }

    public static void startTransactionExecutor(AccountService accountService, TransactionService transactionService, Lock distributedLock) {
        TransactionExecutor transactionExecutor = new TransactionExecutor(accountService, transactionService, distributedLock);
        transactionExecutor.start();
    }

    public static void startJettyServer(AccountService accountService, TransactionService transactionService) throws Exception {
        int maxThreads = 100;
        int minThreads = 10;
        int idleTimeout = 120;

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(PORT);
        server.setConnectors(new Connector[] { connector });

        ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);
        context.setContextPath("/");
        AccountServlet accountServlet = new AccountServlet(accountService, transactionService);
        ServletHolder accountHolder = new ServletHolder(accountServlet);
        context.addServlet(accountHolder, "/accounts/*");
        server.setHandler(context);

        server.start();
    }

}
