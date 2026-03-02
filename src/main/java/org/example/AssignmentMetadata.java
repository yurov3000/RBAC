package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    // Формат по умолчанию — ISO-подобный, но читаемый
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Статический метод для создания с текущим временем
    public static AssignmentMetadata now(String assignedBy, String reason) {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedBy не может быть пустым");
        }
        String now = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    // Форматированный вывод
    public String format() {
        if (reason == null || reason.trim().isEmpty()) {
            return "Assigned by %s at %s (no reason provided)".formatted(assignedBy, assignedAt);
        }
        return "Assigned by %s at %s\nReason: %s".formatted(assignedBy, assignedAt, reason);
    }
}