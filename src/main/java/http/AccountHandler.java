package http;

import account.model.Account;
import account.service.AccountService;
import account.service.TransactionExecutor;
import account.service.TransactionService;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountHandler extends AbstractHandler {

    private static final Pattern PATTERN = Pattern.compile("/accounts/(\\d+?)/transfer/(\\d+?)");
    private static final Pattern PUT_PATTERN = Pattern.compile("/accounts|/accounts/");
    private final AccountService accountService = AccountService.getInstance();
    private final TransactionService transactionService = TransactionService.getInstance();
    private final TransactionExecutor transactionExecutor = new TransactionExecutor(transactionService);

    @Override
    protected ResponseBody post(HttpExchange he, Map<String, String> requestParameters, String requestBody) {
        Matcher matcher = PATTERN.matcher(he.getRequestURI().getPath());
        if (matcher.matches()) {
            String accountIdFrom = matcher.group(1);
            String accountIdTo = matcher.group(2);
            System.out.println("process transfer fro ID " + accountIdFrom + " to ID " + accountIdTo);
            JSONObject object = new JSONObject(requestBody);
            BigDecimal amountToTransfer = object.getBigDecimal("amount");
            return new ResponseBody(
                    new JSONObject(transactionService
                            .createNewTransaction(Long.valueOf(accountIdFrom), Long.valueOf(accountIdTo), amountToTransfer))
                            .toString(),
                    STATUS_CREATED);
        } else if (PUT_PATTERN.matcher(he.getRequestURI().getPath()).matches()) {
            JSONObject object = new JSONObject(requestBody);
            System.out.println("inserting new Account");
            return new ResponseBody(
                    new JSONObject(accountService.addAccount(object.getString("ownerName"), object.getBigDecimal("amount"))).toString(),
                    STATUS_CREATED);
        }

        return new ResponseBody(STATUS_NOT_FOUND);
    }

    @Override
    protected ResponseBody get(HttpExchange he, Map<String, String> requestParameters) {
        Pattern pattern = Pattern.compile("/wallets/(\\d+?)");
        Matcher matcher = pattern.matcher(he.getRequestURI().getPath());
        if (matcher.matches()) {
            String accountId = matcher.group(1);
            System.out.println("Get wallet with ID " + accountId);
            Account account = accountService.get(Long.valueOf(accountId));
            if (account == null) {
                return new ResponseBody(STATUS_NOT_FOUND);
            }
            return new ResponseBody(
                    new JSONObject(account).toString(),
                    STATUS_OK);
        }
        System.out.println("Show list with wallets ");
        return new ResponseBody(
                new JSONArray(accountService.getAll()).toString(),
                STATUS_OK);
    }
}
