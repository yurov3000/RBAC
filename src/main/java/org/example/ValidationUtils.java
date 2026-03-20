package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Regex для имени пользователя: 3-20 символов, только буквы, цифры, подчёркивание
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // Regex для email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Форматы дат
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    // Проверка имени пользователя
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    //Проверка email
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    //Проверка формата даты
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) return false;
        String trimmed = date.trim();

        // Поддерживаемые форматы
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime.parse(trimmed, formatter);
                return true;
            } catch (DateTimeParseException ignored) {}
        }
        return false;
    }

    // Нормализация строки
    public static String normalizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeStringLower(String input) {
        String normalized = normalizeString(input);
        return normalized != null ? normalized.toLowerCase() : null;
    }

    public static String normalizeStringUpper(String input) {
        String normalized = normalizeString(input);
        return normalized != null ? normalized.toUpperCase() : null;
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }
}