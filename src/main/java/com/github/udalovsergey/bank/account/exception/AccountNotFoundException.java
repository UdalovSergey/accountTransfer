package com.github.udalovsergey.bank.account.exception;

public class AccountNotFoundException extends RuntimeException{

    public AccountNotFoundException(long accountId) {
        super(String.format("Account with Id [%s] not found", accountId));
    }
}
