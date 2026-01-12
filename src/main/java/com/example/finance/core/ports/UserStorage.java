package com.example.finance.core.ports;

import com.example.finance.core.model.User;

import java.util.List;

public interface UserStorage {
    List<User> loadAll();
    void saveAll(List<User> users);
}
