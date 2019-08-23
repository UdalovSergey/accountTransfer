package account.exception;

import java.math.BigDecimal;

public class TransactionProcessingException extends RuntimeException {

    public TransactionProcessingException(long accountFromId, long accountToId, BigDecimal amountToTransfer, String message) {
        super(String.format("Transfer of [%s] () from [%s] to [%s] can not be processed because %s",
                amountToTransfer,
                accountFromId,
                accountToId,
                message));
    }
}
