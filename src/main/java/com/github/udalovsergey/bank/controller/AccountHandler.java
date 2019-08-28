package com.github.udalovsergey.bank.controller;

import com.github.udalovsergey.bank.account.model.Account;
import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.transaction.service.TransactionService;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents endpoints handler of basic operations with accounts.
 */
public class AccountHandler extends AbstractHandler {

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("/accounts/(\\d+?)/transactions");
    private static final Pattern ACCOUNTS_PATTERN = Pattern.compile("/accounts|/accounts/");
    private static final Pattern ACCOUNTS_BY_ID_PATTERN = Pattern.compile("/accounts/(\\d+?)");
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountHandler(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }
    
    @Override
    protected ResponseBody post(HttpExchange he, Map<String, String> requestParameters, String requestBody) {
        Matcher transferMatcher = TRANSACTION_PATTERN.matcher(he.getRequestURI().getPath());
        if (transferMatcher.matches()) {
            JSONObject object = new JSONObject(requestBody);
            long accountFromId = Long.parseLong(transferMatcher.group(1));
            long accountToId = object.getLong("accToId");
            BigDecimal amountToTransfer = object.getBigDecimal("amount");
            return new ResponseBody(
                    new JSONObject(transactionService
                            .createNewTransaction(accountFromId, accountToId, amountToTransfer))
                            .toString(),
                    STATUS_CREATED);
        } else if (ACCOUNTS_PATTERN.matcher(he.getRequestURI().getPath()).matches()) {
            JSONObject object = new JSONObject(requestBody);
            return new ResponseBody(
                    new JSONObject(accountService
                            .addAccount(object.getString("ownerName"), object.getBigDecimal("amount")))
                            .toString(),
                    STATUS_CREATED);
        }

        return new ResponseBody(STATUS_NOT_FOUND);
    }

    @Override
    protected ResponseBody get(HttpExchange he, Map<String, String> requestParameters) {
        Matcher accountByIdMatcher = ACCOUNTS_BY_ID_PATTERN.matcher(he.getRequestURI().getPath());
        if (accountByIdMatcher.matches()) {
            String accountId = accountByIdMatcher.group(1);
            Account account = accountService.get(Long.valueOf(accountId));
            if (account == null) {
                return new ResponseBody(STATUS_NOT_FOUND);
            }
            return new ResponseBody(
                    new JSONObject(account).toString(),
                    STATUS_OK);
        } else if (ACCOUNTS_PATTERN.matcher(he.getRequestURI().getPath()).matches()) {
            return new ResponseBody(
                    new JSONArray(accountService.getAll()).toString(),
                    STATUS_OK);
        }
        return new ResponseBody(STATUS_NOT_FOUND);
    }
}