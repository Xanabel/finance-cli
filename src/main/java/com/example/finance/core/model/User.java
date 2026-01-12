package com.example.finance.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    private String login;
    private String passwordHash;

    @JsonIgnore
    private Wallet wallet = new Wallet();

    public User() {}

    public User(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
}
