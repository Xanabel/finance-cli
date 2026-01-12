package com.example.finance.core.ports;

import com.example.finance.core.model.Wallet;

public interface WalletStorage {
    Wallet loadWallet(String login);
    void saveWallet(String login, Wallet wallet);
}
