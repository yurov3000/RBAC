package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew = false;

    // Формат должен совпадать с AssignmentMetadata для совместимости
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TemporaryAssignment(RecUser user, Role role, AssignmentMetadata metadata, String expiresAt) {
        super(user, role, metadata);
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата окончания не может быть пустой");
        }
        this.expiresAt = expiresAt.trim();
        validateDateFormat(this.expiresAt);
    }

    private void validateDateFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата не может быть пустой");
        }
        try {
            // Нормализуем и пытаемся распарсить
            LocalDateTime.parse(normalizeDateTime(dateStr), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Некорректный формат даты: " + dateStr +
                            ". Ожидается 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm' или 'yyyy-MM-dd HH:mm:ss'",
                    e
            );
        }
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public boolean isActive(String now) {
        if (now == null || now.trim().isEmpty()) {
            throw new IllegalArgumentException("Текущая дата не может быть пустой");
        }
        String normalizedNow = normalizeDateTime(now.trim());
        String normalizedExpires = normalizeDateTime(this.expiresAt);
        // Лексикографическое сравнение работает для ISO-подобных форматов
        return normalizedNow.compareTo(normalizedExpires) <= 0;
    }

    @Override
    public boolean isActive() {
        // Используем DateUtils для сравнения
        return !DateUtils.isAfter(getCurrentReferenceDate(), expiresAt);
    }

    private String normalizeDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата не может быть пустой");
        }
        String dt = dateTime.trim();

        // Поддерживаем три формата входных данных
        if (dt.length() == 10) { // yyyy-MM-dd
            return dt + " 00:00:00";
        } else if (dt.length() == 16) { // yyyy-MM-dd HH:mm
            return dt + ":00";
        } else if (dt.length() == 19) { // yyyy-MM-dd HH:mm:ss
            return dt;
        } else {
            throw new IllegalArgumentException(
                    "Неподдерживаемый формат даты: " + dateTime +
                            ". Ожидается 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm' или 'yyyy-MM-dd HH:mm:ss'"
            );
        }
    }

    public boolean isExpired() {
        return !isActive();
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Новая дата окончания не может быть пустой");
        }
        this.expiresAt = newExpirationDate.trim();
        validateDateFormat(this.expiresAt);
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    protected String getCurrentReferenceDate() {
        return DateUtils.getCurrentDate();
    }

    public String getTimeRemaining() {
        if (!isActive()) {
            return "expired";
        }
        return DateUtils.formatRelativeTime(expiresAt);
    }


    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "EXPIRED";
        String relativeTime = DateUtils.formatRelativeTime(expiresAt);
        String reasonLine = (metadata().reason() != null && !metadata().reason().trim().isEmpty())
                ? "\nReason: " + metadata().reason()
                : "";

        return String.format(
                "[%s] %s assigned to %s by %s at %s%s\nStatus: %s (%s)\nExpires at: %s",
                assignmentType(),
                role().getName(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt(),
                reasonLine,
                status,
                relativeTime,
                expiresAt
        );
    }
}