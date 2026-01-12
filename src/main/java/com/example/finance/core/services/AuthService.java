package com.example.finance.core.services;

import com.example.finance.core.model.User;
import com.example.finance.core.model.Wallet;
import com.example.finance.core.ports.UserRepository;
import com.example.finance.core.ports.UserStorage;
import com.example.finance.core.ports.WalletStorage;

public class AuthService {
    private final UserRepository users;
    private final UserStorage storage;
    private final WalletStorage walletStorage;

    private User currentUser;

    public AuthService(UserRepository users, UserStorage storage, WalletStorage walletStorage) {
        this.users = users;
        this.storage = storage;
        this.walletStorage = walletStorage;
    }

    public void register(String login, String password) {
        validateLogin(login);
        validatePassword(password);
        if (users.exists(login)) throw new IllegalArgumentException("Логин уже занят.");

        users.save(new User(login, Passwords.sha256(password)));
        storage.saveAll(users.findAll());
    }

    public void login(String login, String password) {
        validateLogin(login);
        validatePassword(password);

        User u = users.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));

        if (!u.getPasswordHash().equals(Passwords.sha256(password))) {
            throw new IllegalArgumentException("Неверный пароль.");
        }

        Wallet w = walletStorage.loadWallet(login);
        u.setWallet(w);

        currentUser = u;
    }

    public void logoutAndSave() {
        if (currentUser == null) return;

        walletStorage.saveWallet(currentUser.getLogin(), currentUser.getWallet());

        currentUser = null;
    }

    public User requireUser() {
        if (currentUser == null) throw new IllegalStateException("Сначала выполните login.");
        return currentUser;
    }

    public User getCurrentUserOrNull() {
        return currentUser;
    }

    private static void validateLogin(String login) {
        if (login == null || login.trim().isEmpty()) throw new IllegalArgumentException("Логин не должен быть пустым.");
        if (login.length() < 3) throw new IllegalArgumentException("Логин слишком короткий (минимум 3 символа).");
    }

    private static void validatePassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Пароль не должен быть пустым.");
        if (password.length() < 3) throw new IllegalArgumentException("Пароль слишком короткий (минимум 3 символа).");
    }
}
