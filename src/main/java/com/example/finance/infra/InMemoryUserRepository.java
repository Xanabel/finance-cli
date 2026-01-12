package com.example.finance.infra;

import com.example.finance.core.model.User;
import com.example.finance.core.ports.UserRepository;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public boolean exists(String login) {
        return users.containsKey(login);
    }

    @Override
    public void save(User user) {
        users.put(user.getLogin(), user);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void replaceAll(List<User> loaded) {
        users.clear();
        for (User u : loaded) users.put(u.getLogin(), u);
    }
}
