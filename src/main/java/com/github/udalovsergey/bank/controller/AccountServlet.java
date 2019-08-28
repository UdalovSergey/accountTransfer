package com.github.udalovsergey.bank.controller;

import com.github.udalovsergey.bank.account.model.Account;
import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.transaction.service.TransactionService;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Matcher accountByIdMatcher = ACCOUNTS_BY_ID_PATTERN.matcher(request.getRequestURI());
        if (accountByIdMatcher.matches()) {
            //Get account by Id
            String accountId = accountByIdMatcher.group(1);
            Account account = accountService.get(Long.valueOf(accountId));
            if (account == null) {
                response.setStatus(HttpStatus.NOT_FOUND_404);
                return;
            }
            writeJsonResponse(response, new JSONObject(account).toString(), HttpStatus.OK_200);
            return;
        } else if (ACCOUNTS_PATTERN.matcher(request.getRequestURI()).matches()) {
            //Get all accounts
            writeJsonResponse(response, new JSONArray(accountService.getAll()).toString(), HttpStatus.OK_200);
            return;
        }
        response.setStatus(HttpStatus.NOT_FOUND_404);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Matcher transferMatcher = TRANSACTION_PATTERN.matcher(request.getRequestURI());
        if (transferMatcher.matches()) {
            //Create a new transaction
            JSONObject object = new JSONObject(requestBody);
            long accountFromId = Long.parseLong(transferMatcher.group(1));
            long accountToId = object.getLong("accToId");
            BigDecimal amountToTransfer = object.getBigDecimal("amount");
            writeJsonResponse(response,
                    new JSONObject(transactionService.createNewTransaction(accountFromId, accountToId, amountToTransfer))
                            .toString(),
                    HttpStatus.CREATED_201);
            return;
        } else if (ACCOUNTS_PATTERN.matcher(request.getRequestURI()).matches()) {
            //Create a new Account
            JSONObject object = new JSONObject(requestBody);
            writeJsonResponse(response,
                    new JSONObject(accountService.addAccount(object.getString("ownerName"), object.getBigDecimal("amount")))
                            .toString(),
                    HttpStatus.CREATED_201);
            return;
        }
        response.setStatus(HttpStatus.NOT_FOUND_404);
    }

    private void writeJsonResponse(HttpServletResponse response, String jsonString, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print(jsonString);
        out.flush();
    }
}
