package org.example;

import Filters.AssignmentFilter;
import Managers.RoleManager;
import Managers.AssignmentManager;

import java.util.*;
import java.util.stream.Collectors;

class Main {
    static void main(String[] args) {
        // === 1. Тестирование RecUser ===
        System.out.println("=== Тестирование RecUser ===");
        List<String[]> testCases = List.of(
                new String[]{"john_doe", "John Doe", "johndoe@example.com"},
                new String[]{"invalid@username", "Test User", "test@example.com"}
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
        System.out.println(readUsers.format());
        System.out.println(writeReports.format());

        // Тестирование нового метода matches с разными вариантами входных данных
        System.out.println("readUsers matches 'READ'/'US': " + readUsers.matches("READ", "US"));
        System.out.println("writeReports matches 'WRI'/'REP': " + writeReports.matches("WRI", "REP"));

        // === 3. Тестирование Role и новых методов RoleManager ===
        System.out.println("\n=== Тестирование Role и RoleManager ===");
        Role admin = new Role("Administrator", "Full system access");
        Role user = new Role("User", "Basic access");

        admin.addPermission(readUsers);
        admin.addPermission(writeReports);

        RoleManager roleManager = new RoleManager();
        roleManager.add(admin);
        roleManager.add(user);

        // Тестирование методов RoleManager
        System.out.println("Найти роль по имени: " + roleManager.findByName("Administrator"));
        System.out.println("Все роли: " + roleManager.findAll());
        System.out.println("Существует ли роль 'User'? " + roleManager.exists("User"));

        // Добавление и удаление прав
        roleManager.addPermissionToRole("User", readUsers);
        System.out.println("Роли с правом 'read/Users': " + roleManager.findRolesWithPermission("read", "Users"));

        // === 4. Тестирование AssignmentMetadata ===
        System.out.println("\n=== Тестирование AssignmentMetadata ===");
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("john_doe", "");
        System.out.println(meta1.format());
        System.out.println(meta2.format());

        // === 5. Создание пользователей для назначений ===
        RecUser alice = RecUser.validate("alice", "Alice Smith", "alice@example.com");
        RecUser bob = RecUser.validate("bob_dev", "Bob Developer", "bob@example.com");

        // === 6. Тестирование PermanentAssignment и AssignmentManager ===
        System.out.println("\n=== Тестирование PermanentAssignment и AssignmentManager ===");
        AssignmentManager assignmentManager = new AssignmentManager();

        PermanentAssignment permAssign = new PermanentAssignment(alice, admin, meta1);
        assignmentManager.add(permAssign);

        System.out.println("Permanent assignment ID: " + permAssign.assignmentId());
        System.out.println("Is active? " + permAssign.isActive());
        System.out.println("Is revoked? " + permAssign.isRevoked());

        // Отмена назначения и проверка
        permAssign.revoke();
        System.out.println("\nAfter revoke:");
        System.out.println("Is active? " + permAssign.isActive());
        System.out.println("Is revoked? " + permAssign.isRevoked());

        // Проверка в AssignmentManager
        System.out.println("Назначения для пользователя Alice: " + assignmentManager.findByUser(alice));

        // === 7. Тестирование TemporaryAssignment ===
        System.out.println("\n=== Тестирование TemporaryAssignment ===");
        AssignmentMetadata tempMeta = AssignmentMetadata.now("security", "Temporary dev access");
        String tomorrow = "2026-02-17 12:00";
        TemporaryAssignment tempAssign = new TemporaryAssignment(bob, admin, tempMeta, tomorrow);
        assignmentManager.add(tempAssign);

        System.out.println("Temporary assignment ID: " + tempAssign.assignmentId());
        System.out.println("Expires at: " + tempAssign.getExpiresAt());
        System.out.println("Is active (real time)? " + tempAssign.isActive());
        System.out.println("Is expired? " + tempAssign.isExpired());

        // Продление срока
        tempAssign.extend("2026-03-01 09:00");
        System.out.println("\nAfter extend to 2026-03-01:");
        System.out.println("New expires at: " + tempAssign.getExpiresAt());
        System.out.println("Is active now? " + tempAssign.isActive());

        // === 8. Тестирование фильтров AssignmentManager ===
        System.out.println("\n=== Тестирование фильтров AssignmentManager ===");
        AssignmentFilter activeFilter = assignment -> assignment.isActive();
        List<RoleAssignment> activeAssignments = assignmentManager.findAll(activeFilter, Comparator.comparing(RoleAssignment::assignmentId));
        System.out.println("Активные назначения: " + activeAssignments);

        AssignmentFilter userFilter = assignment -> assignment.user().username().equals("bob_dev");
        List<RoleAssignment> bobAssignments = assignmentManager.findByFilter(userFilter);
        System.out.println("Назначения для Bob: " + bobAssignments);

        // === 9. Тестирование методов проверки прав ===
        System.out.println("\n=== Тестирование методов проверки прав ===");
        System.out.println("Имеет ли Alice роль Administrator? " + assignmentManager.userHasRole(alice, admin));
        System.out.println("Имеет ли Bob право 'read/Users'? " + assignmentManager.userHasPermission(bob, "read", "Users"));

        // Получение всех прав пользователя
        Set<RecPermission> bobPermissions = assignmentManager.getUserPermissions(bob);
        System.out.println("Все права Bob: " + bobPermissions);

        // === 10. Тестирование просроченных назначений ===
        System.out.println("\n=== Тестирование просроченных назначений ===");
        List<RoleAssignment> expiredAssignments = assignmentManager.getExpiredAssignments();
        System.out.println("Просроченные назначения: " + expiredAssignments);

        // === 11. Дополнительно: проверка equals/hashCode по assignmentId ===
        System.out.println("\n=== Проверка equals/hashCode ===");
        PermanentAssignment sameAsPerm = new PermanentAssignment(alice, admin, meta1);
        System.out.println("permAssign.equals(sameAsPerm): " + permAssign.equals(sameAsPerm));
        System.out.println("permAssign.hashCode() == sameAsPerm.hashCode(): " + (permAssign.hashCode() == sameAsPerm.hashCode()));
    }
}
