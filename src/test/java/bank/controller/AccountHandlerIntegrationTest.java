package bank.controller;

import bank.BankApplication;
import bank.account.model.Account;
import bank.account.service.AccountService;
import bank.transaction.service.TransactionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import static bank.controller.AbstractHandler.*;

public class AccountHandlerIntegrationTest {

    private static AccountService accountService;

    @BeforeAll
    public static void init() throws IOException {
        accountService = new AccountService();
        TransactionService transactionService = new TransactionService(accountService);

        BankApplication.startServer(accountService, transactionService);
        BankApplication.startTransactionExecutor(accountService, transactionService);
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

        Response response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transfer/" + fred.getId(),
                "POST", new TransferPayload(BigDecimal.valueOf(500)));

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

        Response response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transfer/" + fred.getId(),
                "POST", new TransferPayload(BigDecimal.valueOf(1100)));
        Assertions.assertEquals(STATUS_FORBIDDEN, response.getStatus());

        response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transfer/" + john.getId(),
                "POST", new TransferPayload(BigDecimal.valueOf(500)));
        Assertions.assertEquals(STATUS_FORBIDDEN, response.getStatus());

        response = httpRequest("http://localhost:8080/accounts/" + john.getId() + "/transfer/" + fred.getId(),
                "POST", new TransferPayload(BigDecimal.ZERO));
        Assertions.assertEquals(STATUS_FORBIDDEN, response.getStatus());

        response = httpRequest("http://localhost:8080/accounts/" + -1 + "/transfer/" + fred.getId(),
                "POST", new TransferPayload(BigDecimal.ZERO));
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

        private BigDecimal amount;

        public TransferPayload(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}