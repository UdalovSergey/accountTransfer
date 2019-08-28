package com.github.udalovsergey.bank.transaction.exception;

import java.math.BigDecimal;

public class TransactionProcessingException extends RuntimeException {

    public TransactionProcessingException(long accountFromId, long accountToId, BigDecimal amountToTransfer, String message) {
        super(String.format("Transfer of [%s] () from account [%s] to account [%s] can not be processed because %s",
                amountToTransfer,
                accountFromId,
                accountToId,
                message));
    }
}
