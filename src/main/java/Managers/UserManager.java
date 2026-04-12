package Managers;

import Filters.UserFilter;
import org.example.RecUser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<RecUser> {
    // Внутреннее хранилище пользователей (ключ — username)
    private final Map<String, RecUser> users = new ConcurrentHashMap<>();

    @Override
    public void add(RecUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        synchronized (this) {
            String username = user.username();
            if (users.containsKey(username)) {
                throw new IllegalArgumentException("Пользователь с username '" + username + "' уже существует");
            }
            RecUser.validate(username, user.fullname(), user.email());
            users.put(username, user);
        }
    }

    @Override
    public boolean remove(RecUser user) {
        if (user == null) return false;
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<RecUser> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<RecUser> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    // Найти пользователя по username
    public Optional<RecUser> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        return findById(username);
    }

    // Найти пользователя по email
    public Optional<RecUser> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        return users.values().stream()
                .filter(user -> email.equals(user.email()))
                .findFirst();
    }

    // Найти пользователей по фильтру
    public List<RecUser> findByFilter(UserFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }

        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    // Найти всех пользователей с фильтрацией и сортировкой
    public List<RecUser> findAll(UserFilter filter, Comparator<RecUser> sorter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }
        if (sorter == null) {
            throw new IllegalArgumentException("Компаратор не может быть null");
        }

        return users.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    // Проверить существование пользователя по username
    public boolean exists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return users.containsKey(username);
    }

    // Обновить данные пользователя
    public void update(String username, String newFullName, String newEmail) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }

        RecUser existingUser = users.get(username);
        if (existingUser == null) {
            throw new NoSuchElementException("Пользователь с username '" + username + "' не найден");
        }

        // Валидация новых данных
        RecUser.validate(username, newFullName, newEmail);

        // Создаём нового пользователя с обновлёнными данными
        RecUser updatedUser = new RecUser(username, newFullName, newEmail);
        users.put(username, updatedUser);
    }

    public RecUser findByName(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return findByUsername(username).orElse(null);
    }

    public List<RecUser> findByFilterParallel(UserFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }
        // Используем parallelStream для параллельной обработки списка пользователей
        return users.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }
}
