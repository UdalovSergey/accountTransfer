package com.github.udalovsergey.bank.controller;

import com.github.udalovsergey.bank.BankApplication;
import com.github.udalovsergey.bank.account.model.Account;
import com.github.udalovsergey.bank.account.service.AccountService;
import com.github.udalovsergey.bank.transaction.service.Lock;
import com.github.udalovsergey.bank.transaction.service.TransactionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.udalovsergey.bank.controller.AbstractHandler.*;

public class AccountHandlerIntegrationTest {

    private static AccountService accountService;

    @BeforeAll
    public static void init() throws IOException {
        accountService = new AccountService();
        Lock distributedLock = new Lock();
        TransactionService transactionService = new TransactionService(accountService, distributedLock);

        BankApplication.startServer(accountService, transactionService);
        BankApplication.startTransactionExecutor(accountService, transactionService, distributedLock);
    }

    @Test
    public void accountCreationTest() throws IOException {
        Account newAccount = new Account("John", BigDecimal.valueOf(1000));
        Response response = httpRequest("http://localhost:8080/accounts/", "POST", newAccount);

        JSONObject payload = new JSONObject(response.getPayload());

        Assertions.assertEquals(STATUS_CREATED, response.getStatus());
        Assertions.assertEquals(newAccount.getOwnerName(), payload.get("ownerName"));
        Assertions.assertEquals(newAccount.getAmount(), payload.getBigDecimal("amount"));
    }

    @Test
    public void accountGetAllTest() throws IOException {
        Account john = accountService.addAccount("John", BigDecimal.valueOf(1000));
        Account fred = accountService.addAccount("Fred", BigDecimal.valueOf(1000));

        Response response = httpRequest("http://localhost:8080/accounts/", "GET", null);

        JSONArray payload = new JSONArray(response.getPayload());
        Assertions.assertEquals(STATUS_OK, response.getStatus());
        Assertions.assertEquals(john.getOwnerName(), payload.getJSONObject((int) john.getId()).get("ownerName"));
        Assertions.assertEquals(fred.getAmount(), payload.getJSONObject((int) fred.getId()).getBigDecimal("amount"));
    }

    @Test
    public void accountGetByIdTest() throws IOException {
        Account john = accountService.addAccount("John", BigDecimal.valueOf(1000));
        Response response = httpRequest("http://localhost:8080/accounts/" + john.getId(), "GET", null);

        JSONObject payload = new JSONObject(response.getPayload());
        Assertions.assertEquals(STATUS_OK, response.getStatus());
        Assertions.assertEquals(john.getOwnerName(), payload.get("ownerName"));

        response = httpRequest("http://localhost:8080/accounts/" + -1, "GET", null);
        Assertions.assertEquals(STATUS_NOT_FOUND, response.getStatus());
    }

    @Test
    public void createTransactionTest() throws IOException {
        Account john = accountService.addAccount("John", BigDecimal.valueOf(1000));
        Account fred = accountService.addAccount("Fred", BigDecimal.valueOf(1000));

        Response response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transactions",
                "POST", new TransferPayload(fred.getId(), BigDecimal.valueOf(500)));

        JSONObject payload = new JSONObject(response.getPayload());
        Assertions.assertEquals(STATUS_CREATED, response.getStatus());
        Assertions.assertEquals(john.getId(), payload.getLong("accountFromId"));
        Assertions.assertEquals(fred.getId(), payload.getLong("accountToId"));
        Assertions.assertEquals(BigDecimal.valueOf(500), payload.getBigDecimal("amount"));
    }

    @Test
    public void createTransactionFailedTest() throws IOException {
        Account john = accountService.addAccount("John", BigDecimal.valueOf(1000));
        Account fred = accountService.addAccount("Fred", BigDecimal.valueOf(1000));

        Response response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transactions",
                "POST", new TransferPayload(fred.getId(), BigDecimal.valueOf(1100)));
        Assertions.assertEquals(STATUS_FORBIDDEN, response.getStatus());

        response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transactions",
                "POST", new TransferPayload(john.getId(), BigDecimal.valueOf(500)));
        Assertions.assertEquals(STATUS_FORBIDDEN, response.getStatus());

        response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transactions",
                "POST", new TransferPayload(fred.getId(), BigDecimal.ZERO));
        Assertions.assertEquals(STATUS_FORBIDDEN, response.getStatus());

        response = httpRequest("http://localhost:8080/accounts/" + -1 + "/transactions",
                "POST", new TransferPayload(fred.getId(), BigDecimal.ZERO));
        Assertions.assertEquals(STATUS_NOT_FOUND, response.getStatus());
    }

    private Response httpRequest(String endpoint, String method, Object requestBody) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(true);
        if (requestBody != null) {
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(new JSONObject(requestBody).toString());
            out.flush();
            out.close();
        }
        Response response = new Response(con);
        con.disconnect();
        return response;
    }

    private static class Response {
        private int status;
        private String payload;

        public Response(HttpURLConnection con) throws IOException {
            this.status = con.getResponseCode();
            this.payload = readResponse(con);
        }

        private String readResponse(HttpURLConnection con) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getResponseCode() < 300 ? con.getInputStream() : con.getErrorStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        public int getStatus() {
            return status;
        }

        public String getPayload() {
            return payload;
        }
    }

    public static class TransferPayload {

        private long accToId;
        private BigDecimal amount;

        public TransferPayload(long accToId, BigDecimal amount) {
            this.accToId = accToId;
            this.amount = amount;
        }

        public long getAccToId() {
            return accToId;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}
