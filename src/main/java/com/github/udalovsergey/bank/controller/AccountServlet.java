package com.github.udalovsergey.bank.controller;

import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.transaction.service.TransactionService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class AccountServlet extends HttpServlet {

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("/accounts/(\\d+?)/transactions");
    private static final Pattern ACCOUNTS_PATTERN = Pattern.compile("/accounts|/accounts/");
    private static final Pattern ACCOUNTS_BY_ID_PATTERN = Pattern.compile("/accounts/(\\d+?)");

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountServlet(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }
}
