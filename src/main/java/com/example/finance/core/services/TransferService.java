package com.example.finance.core.services;

import com.example.finance.core.model.User;
import com.example.finance.core.model.Wallet;
import com.example.finance.core.ports.UserRepository;
import com.example.finance.core.ports.WalletStorage;


public class TransferService {
    private final UserRepository users;
    private final WalletStorage walletStorage;
    private final WalletService walletService;

    public TransferService(UserRepository users, WalletStorage walletStorage, WalletService walletService) {
        this.users = users;
        this.walletStorage = walletStorage;
        this.walletService = walletService;
    }

    /**
     * Перевод денег from -> toLogin
     * У from: EXPENSE, у получателя: INCOME
     * category: "Перевод"
     */
    public void transfer(User from, String toLogin, double amount, String note) {
        if (toLogin == null || toLogin.trim().isEmpty())
            throw new IllegalArgumentException("Логин получателя пуст.");

        if (from.getLogin().equals(toLogin))
            throw new IllegalArgumentException("Нельзя переводить самому себе.");

        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0)
            throw new IllegalArgumentException("Сумма перевода должна быть > 0.");

        var toUser = users.findByLogin(toLogin)
                .orElseThrow(() -> new IllegalArgumentException("Получатель не найден: " + toLogin));

        Wallet toWallet = walletStorage.loadWallet(toLogin);
        toUser.setWallet(toWallet);

        String cat = "Перевод";
        walletService.addCategory(from.getWallet(), cat);
        walletService.addCategory(toUser.getWallet(), cat);

        walletService.addExpense(from.getWallet(), cat, amount, "to " + toLogin + (note == null || note.isBlank() ? "" : (": " + note)));
        walletService.addIncome(toUser.getWallet(), cat, amount, "from " + from.getLogin() + (note == null || note.isBlank() ? "" : (": " + note)));

        walletStorage.saveWallet(from.getLogin(), from.getWallet());
        walletStorage.saveWallet(toLogin, toUser.getWallet());
    }
}
