package com.github.udalovsergey.bank;

import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.controller.AccountServlet;
import com.github.udalovsergey.bank.controller.BankErrorHandler;
import com.github.udalovsergey.bank.transaction.service.Lock;
import com.github.udalovsergey.bank.transaction.service.TransactionExecutor;
import com.github.udalovsergey.bank.transaction.service.TransactionService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class BankApplication {

    private static final int PORT = 8080;

    public static void main(final String... args) throws Exception {
        AccountService accountService = new AccountService();
        Lock distributedLock = new Lock();
        TransactionService transactionService = new TransactionService(accountService, distributedLock);
        startJettyServer(accountService, transactionService);
        startTransactionExecutor(accountService, transactionService, distributedLock);
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
        try (ServerConnector connector = new ServerConnector(server)) {
            connector.setPort(PORT);
            server.setConnectors(new Connector[]{connector});
        }

        ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);
        context.setContextPath("/");
        AccountServlet accountServlet = new AccountServlet(accountService, transactionService);
        ServletHolder accountHolder = new ServletHolder(accountServlet);
        context.addServlet(accountHolder, "/accounts/*");
        server.setHandler(context);
        server.setErrorHandler(new BankErrorHandler());

        server.start();
    }

}
