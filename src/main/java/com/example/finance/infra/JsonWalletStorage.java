package com.example.finance.infra;

import com.example.finance.core.model.Wallet;
import com.example.finance.core.ports.WalletStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Files;
import java.nio.file.Path;

public class JsonWalletStorage implements WalletStorage {
    private final Path dataDir;
    private final ObjectMapper om = new ObjectMapper();

    public JsonWalletStorage(Path dataDir) {
        this.dataDir = dataDir;
        om.registerModule(new JavaTimeModule());
    }

    private Path walletPath(String login) {
        return dataDir.resolve(login + ".wallet.json");
    }

    @Override
    public Wallet loadWallet(String login) {
        try {
            Files.createDirectories(dataDir);
            Path p = walletPath(login);
            if (!Files.exists(p)) return new Wallet();
            return om.readValue(p.toFile(), Wallet.class);
        } catch (Exception e) {
            return new Wallet();
        }
    }

    @Override
    public void saveWallet(String login, Wallet wallet) {
        try {
            Files.createDirectories(dataDir);
            om.writerWithDefaultPrettyPrinter().writeValue(walletPath(login).toFile(), wallet);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить кошелёк: " + e.getMessage(), e);
        }
    }
}
