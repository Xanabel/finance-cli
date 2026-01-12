package com.example.finance.infra;

import com.example.finance.core.model.User;
import com.example.finance.core.ports.UserStorage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonUserStorage implements UserStorage {
    private final Path path;
    private final ObjectMapper om = new ObjectMapper();

    public JsonUserStorage(Path path) {
        this.path = path;
    }

    @Override
    public List<User> loadAll() {
        try {
            if (!Files.exists(path)) return new ArrayList<>();
            User[] arr = om.readValue(path.toFile(), User[].class);
            return new ArrayList<>(Arrays.asList(arr));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(List<User> users) {
        try {
            Files.createDirectories(path.getParent() == null ? Path.of(".") : path.getParent());
            om.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), users);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось сохранить пользователей: " + e.getMessage(), e);
        }
    }
}