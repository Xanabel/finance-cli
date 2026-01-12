package com.example.finance;

import com.example.finance.cli.CommandLoop;
import com.example.finance.core.ports.UserStorage;
import com.example.finance.core.ports.WalletStorage;
import com.example.finance.core.services.AuthService;
import com.example.finance.core.services.TransferService;
import com.example.finance.core.services.WalletService;
import com.example.finance.infra.InMemoryUserRepository;
import com.example.finance.infra.JsonUserStorage;
import com.example.finance.infra.JsonWalletStorage;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path dataDir = Path.of("data");

        InMemoryUserRepository repo = new InMemoryUserRepository();
        UserStorage userStorage = new JsonUserStorage(dataDir.resolve("users.json"));
        WalletStorage walletStorage = new JsonWalletStorage(dataDir);

        repo.replaceAll(userStorage.loadAll());

        AuthService authService = new AuthService(repo, userStorage, walletStorage);
        WalletService walletService = new WalletService();
        TransferService transferService = new TransferService(repo, walletStorage, walletService);

        new CommandLoop(authService, walletService, transferService).run();
    }
}
