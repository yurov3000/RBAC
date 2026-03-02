package Filters;

import org.example.RecUser;

import java.util.Comparator;

public class UserSorters {

    // Сортировка по имени пользователя (username)
    public static Comparator<RecUser> byUsername() {
        return (user1, user2) -> user1.username().compareTo(user2.username());
    }

    // Сортировка по полному имени (fullname)
    public static Comparator<RecUser> byFullName() {
        return (user1, user2) -> user1.fullname().compareTo(user2.fullname());
    }

    // Сортировка по email
    public static Comparator<RecUser> byEmail() {
        return (user1, user2) -> user1.email().compareTo(user2.email());
    }
}

