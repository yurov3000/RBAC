package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        ValidationUtils.requireNonEmpty(assignedBy, "assignedBy");

        String normalizedBy = ValidationUtils.normalizeString(assignedBy);
        String normalizedReason = ValidationUtils.normalizeString(reason);
        String timestamp = LocalDateTime.now().format(FORMATTER);

        return new AssignmentMetadata(normalizedBy, timestamp, normalizedReason);
    }

    // Конструктор с валидацией даты
    public static AssignmentMetadata create(String assignedBy, String assignedAt, String reason) {
        ValidationUtils.requireNonEmpty(assignedBy, "assignedBy");
        if (!ValidationUtils.isValidDate(assignedAt)) {
            throw new IllegalArgumentException("Неверный формат даты: " + assignedAt);
        }

        return new AssignmentMetadata(
                ValidationUtils.normalizeString(assignedBy),
                assignedAt.trim(),
                ValidationUtils.normalizeString(reason)
        );
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assigned by ").append(assignedBy).append(" at ").append(assignedAt);
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append("\nReason: ").append(reason);
        } else {
            sb.append(" (no reason provided)");
        }
        return sb.toString();
    }
}