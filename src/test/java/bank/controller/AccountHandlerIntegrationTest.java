package bank.controller;

import bank.BankApplication;
import bank.account.model.Account;
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

import static bank.controller.AbstractHandler.STATUS_CREATED;

public class AccountHandlerIntegrationTest {

    @BeforeAll
    public static void init() throws IOException {
        BankApplication.startServer();
        BankApplication.startTransactionExecutor();
    }

    @Test
    public void accountCreationTest() throws IOException {
        Account newAccount = new Account("John", BigDecimal.valueOf(1000));
        Response response = httpRequest("http://localhost:8080/accounts/", "POST", newAccount);

        JSONObject payload = new JSONObject(response.getPayload());;
        Assertions.assertEquals(STATUS_CREATED, response.getStatus());
        Assertions.assertEquals(newAccount.getOwnerName(), payload.get("ownerName"));
        Assertions.assertEquals(newAccount.getAmount(), payload.getBigDecimal("amount"));
    }

    private Response httpRequest(String endpoint, String method, Object requestBody) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(new JSONObject(requestBody).toString());
        out.flush();
        out.close();
        Response response = new Response(con);
        con.disconnect();
        return response;
    }

    private static class Response {
        private int status;
        private String payload;

        public Response(HttpURLConnection con) throws IOException {
            this.status = con.getResponseCode();
            this.payload = readRepose(con);
        }

        private String readRepose(HttpURLConnection con) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
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
}
