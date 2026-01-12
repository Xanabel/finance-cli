package com.example.finance.core.ports;

import com.example.finance.core.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByLogin(String login);
    boolean exists(String login);
    void save(User user);
    List<User> findAll();
}