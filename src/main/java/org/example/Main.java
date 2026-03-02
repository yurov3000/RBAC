package org.example;

import java.util.Arrays;
import java.util.List;

class Main {
    static void main(String[] args) {
        // === 1. Тестирование RecUser ===
        System.out.println("=== Тестирование RecUser ===");
        List<String[]> testCases = List.of(
                new String[]{"john_doe", "John Doe", "johndoe@example.com"},
                new String[]{"invalid@username", "Test User", "test@example.com"},
                new String[]{"usr", "Short Username", "short@user.com"},
                new String[]{"very_long_username_with_more_than_twenty_characters", "Long Username", "long@example.com"},
                new String[]{"valid_user", "", "valid@email.com"},
                new String[]{"valid_user", "Valid Fullname", ""},
                new String[]{"valid_user", "Valid Fullname", "not_an_email"}
        );

        for (var caseData : testCases) {
            try {
                var user = RecUser.validate(caseData[0], caseData[1], caseData[2]);
                System.out.println(user.format());
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }

        // === 2. Тестирование RecPermission ===
        System.out.println("\n=== Тестирование RecPermission ===");
        RecPermission readUsers = new RecPermission("read", "Users", "Позволяет читать пользователей");
        RecPermission writeReports = new RecPermission("write", "REPORTS", "Позволяет создавать отчёты");
        RecPermission writeUsers = new RecPermission("write", "Users", "Позволяет создавать и редактировать пользователей");

        System.out.println(readUsers.format());
        System.out.println(writeReports.format());

        System.out.println("readUsers matches 'READ'/'US': " + readUsers.matches("READ", "US")); // true
        System.out.println("writeReports matches 'WRI'/'REP': " + writeReports.matches("WRI", "REP")); // true

        // === 3. Тестирование Role ===
        System.out.println("\n=== Тестирование Role ===");
        Role admin = new Role("Administrator", "Full system access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        System.out.println(admin.format());

        // === 4. Тестирование AssignmentMetadata ===
        System.out.println("\n=== Тестирование AssignmentMetadata ===");
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("john_doe", "");
        System.out.println(meta1.format());
        System.out.println(meta2.format());

        // === 5. Создание пользователей для назначений ===
        RecUser alice = RecUser.validate("alice", "Alice Smith", "alice@example.com");
        RecUser bob = RecUser.validate("bob_dev", "Bob Developer", "bob@example.com");

        // === 6. Тестирование PermanentAssignment ===
        System.out.println("\n=== Тестирование PermanentAssignment ===");
        PermanentAssignment permAssign = new PermanentAssignment(alice, admin, meta1);
        System.out.println("Permanent assignment ID: " + permAssign.assignmentId());
        System.out.println("Is active? " + permAssign.isActive()); // true
        System.out.println("Is revoked? " + permAssign.isRevoked()); // false
        System.out.println("\nSummary:\n" + permAssign.summary());

        // Отмена назначения
        permAssign.revoke();
        System.out.println("\nAfter revoke:");
        System.out.println("Is active? " + permAssign.isActive()); // false
        System.out.println("Is revoked? " + permAssign.isRevoked()); // true
        System.out.println("\nSummary:\n" + permAssign.summary());

        // === 7. Тестирование TemporaryAssignment ===
        System.out.println("\n=== Тестирование TemporaryAssignment ===");
        AssignmentMetadata tempMeta = AssignmentMetadata.now("security", "Temporary dev access");
        // Устанавливаем срок окончания — на 1 день вперёд от текущего времени (пример)
        String tomorrow = "2026-02-17 12:00"; // вы можете изменить на актуальную дату
        TemporaryAssignment tempAssign = new TemporaryAssignment(bob, admin, tempMeta, tomorrow);

        System.out.println("Temporary assignment ID: " + tempAssign.assignmentId());
        System.out.println("Expires at: " + tempAssign.getExpiresAt());
        System.out.println("Is active (real time)? " + tempAssign.isActive()); // зависит от системного времени
        System.out.println("Is expired? " + tempAssign.isExpired());
        System.out.println("Time remaining: " + tempAssign.getTimeRemaining());

        // Тест с фиксированной "текущей" датой (для предсказуемости)
        System.out.println("Is active on 2026-02-16 10:00? " + tempAssign.isActive("2026-02-16 10:00")); // true
        System.out.println("Is active on 2026-02-18 10:00? " + tempAssign.isActive("2026-02-18 10:00")); // false

        System.out.println("\nSummary:\n" + tempAssign.summary());

        // Продление срока
        tempAssign.extend("2026-03-01 09:00");
        System.out.println("\nAfter extend to 2026-03-01:");
        System.out.println("New expires at: " + tempAssign.getExpiresAt());
        System.out.println("Is active now? " + tempAssign.isActive());
        System.out.println("\nUpdated Summary:\n" + tempAssign.summary());

        // === 8. Дополнительно: проверка equals/hashCode по assignmentId ===
        System.out.println("\n=== Проверка equals/hashCode ===");
        PermanentAssignment sameAsPerm = new PermanentAssignment(alice, admin, meta1);
        System.out.println("permAssign.equals(sameAsPerm): " + permAssign.equals(sameAsPerm)); // false (разные ID)
        System.out.println("permAssign.hashCode() == sameAsPerm.hashCode(): " + (permAssign.hashCode() == sameAsPerm.hashCode())); // false
    }
}