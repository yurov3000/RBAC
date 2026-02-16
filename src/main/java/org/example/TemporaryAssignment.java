package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt; // в формате "yyyy-MM-dd HH:mm" или "yyyy-MM-dd"
    private boolean autoRenew = false;

    // Формат по умолчанию — совместим с AssignmentMetadata
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(RecUser user, Role role, AssignmentMetadata metadata, String expiresAt) {
        super(user, role, metadata);
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата окончания не может быть пустой");
        }
        this.expiresAt = expiresAt.trim();
        validateDateFormat(this.expiresAt); // опциональная проверка
    }

    // Вспомогательный метод для проверки формата (необязательный, но полезен)
    private void validateDateFormat(String dateStr) {
        try {
            if (dateStr.length() == 10) {
                LocalDateTime.parse(dateStr + " 00:00", FORMATTER);
            } else {
                LocalDateTime.parse(dateStr, FORMATTER);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат даты: " + dateStr + ". Ожидается 'yyyy-MM-dd' или 'yyyy-MM-dd HH:mm'");
        }
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    // Основной метод: проверка активности с передачей текущего времени (для тестирования)
    public boolean isActive(String now) {
        if (now == null || now.trim().isEmpty()) {
            throw new IllegalArgumentException("Текущая дата не может быть пустой");
        }
        // Приведение к одинаковому формату (добавляем время, если его нет)
        String normalizedNow = normalizeDateTime(now.trim());
        String normalizedExpires = normalizeDateTime(this.expiresAt);

        return normalizedNow.compareTo(normalizedExpires) <= 0;
    }

    // Удобный метод без параметров — использует текущее системное время
    @Override
    public boolean isActive() {
        String now = LocalDateTime.now().format(FORMATTER);
        return isActive(now);
    }

    // Нормализует дату до формата "yyyy-MM-dd HH:mm"
    private String normalizeDateTime(String dateTime) {
        if (dateTime.length() == 10) {
            return dateTime + " 00:00";
        } else if (dateTime.length() == 16) {
            return dateTime;
        } else {
            throw new IllegalArgumentException("Неподдерживаемый формат даты: " + dateTime);
        }
    }

    // Проверка, истёк ли срок
    public boolean isExpired() {
        return !isActive();
    }

    // Продление срока действия
    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Новая дата окончания не может быть пустой");
        }
        this.expiresAt = newExpirationDate.trim();
        validateDateFormat(this.expiresAt);
    }

    // Геттеры
    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    // Опционально: оставшееся время (упрощённо — только дни)
    public String getTimeRemaining() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expire = LocalDateTime.parse(normalizeDateTime(expiresAt), FORMATTER);
            if (!isActive()) {
                return "expired";
            }
            long days = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), expire.toLocalDate());
            return days + " day(s) left";
        } catch (Exception e) {
            return "unknown";
        }
    }

    // Переопределённый summary() с информацией об истечении
    @Override
    public String summary() {
        String base = super.summary(); // вызывает summary() из AbstractRoleAssignment
        String activeStatus = isActive() ? "ACTIVE" : "EXPIRED";
        return base.replace("Status: " + (isActive() ? "ACTIVE" : "INACTIVE"),
                "Status: " + activeStatus + "\nExpires at: " + expiresAt);
    }
}