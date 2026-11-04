package com.atm.util;

import com.atm.model.Account;
import com.atm.service.AccountService;
import com.atm.service.AuthService;

/** Holds the logged-in account and the services the UI panels need. */
public final class Session {
    private final Account account;
    private final AuthService authService;
    private final AccountService accountService;

    public Session(Account account, AuthService authService, AccountService accountService) {
        this.account = account;
        this.authService = authService;
        this.accountService = accountService;
    }

    public Account getAccount() { return account; }
    public String getAccountNumber() { return account.getAccountNumber(); }
    public AuthService getAuthService() { return authService; }
    public AccountService getAccountService() { return accountService; }
}
