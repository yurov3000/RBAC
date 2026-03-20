package org.example;

import Managers.AssignmentManager;
import Managers.RoleManager;
import Managers.UserManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("           ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ И РОЛЯМ                  \n");
        sb.append("           Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("           \n");
        sb.append("=============================================================\n\n");

        List<RecUser> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователи не найдены\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s %-25s %-30s %-15s%n", "Username", "Full Name", "Email", "Roles"));
        sb.append("═".repeat(90)).append("\n");

        for (RecUser user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            List<String> roleNames = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .map(a -> a.role().getName())
                    .collect(Collectors.toList());

            String rolesStr = roleNames.isEmpty() ? "Нет ролей" : String.join(", ", roleNames);

            sb.append(String.format("%-20s %-25s %-30s %-15s%n",
                    user.username(),
                    truncate(user.fullname(), 24),
                    truncate(user.email(), 29),
                    truncate(rolesStr, 14)));
        }

        sb.append("\n").append("Всего пользователей: ").append(users.size()).append("\n");
        long usersWithRoles = users.stream()
                .filter(u -> !assignmentManager.findByUser(u).stream()
                        .filter(RoleAssignment::isActive)
                        .collect(Collectors.toList()).isEmpty())
                .count();
        sb.append("Пользователей с ролями: ").append(usersWithRoles).append("\n");

        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================================================\n");
        sb.append("                 ОТЧЁТ ПО РОЛЯМ                            \n");
        sb.append("           Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("           \n");
        sb.append("==============================================================\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("Роли не найдены\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s %-30s %-15s %-10s%n", "Role Name", "Description", "Users Count", "Permissions"));
        sb.append("═".repeat(75)).append("\n");

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long activeUsers = assignments.stream()
                    .filter(RoleAssignment::isActive)
                    .count();

            sb.append(String.format("%-20s %-30s %-15d %-10d%n",
                    truncate(role.getName(), 19),
                    truncate(role.getDescription(), 29),
                    activeUsers,
                    role.getPermissions().size()));
        }

        sb.append("\n").append("Всего ролей: ").append(roles.size()).append("\n");

        // Топ-3 самых популярных ролей
        sb.append("\nТоп-3 популярных ролей:\n");
        Map<String, Long> rolePopularity = roles.stream()
                .collect(Collectors.toMap(
                        Role::getName,
                        role -> assignmentManager.findByRole(role).stream()
                                .filter(RoleAssignment::isActive)
                                .count()
                ));

        rolePopularity.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .forEach(entry -> sb.append("  ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append(" пользователей\n"));

        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================================================\n");
        sb.append("                 МАТРИЦА ПРАВ ДОСТУПА                       \n");
        sb.append("            Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("           \n");
        sb.append("==============================================================\n\n");

        List<RecUser> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователи не найдены\n");
            return sb.toString();
        }

        // Собираем все уникальные ресурсы
        Set<String> allResources = new TreeSet<>();
        for (RecUser user : users) {
            Set<RecPermission> permissions = assignmentManager.getUserPermissions(user);
            for (RecPermission p : permissions) {
                allResources.add(p.resource());
            }
        }

        if (allResources.isEmpty()) {
            sb.append("Права доступа не найдены\n");
            return sb.toString();
        }

        // Заголовок таблицы
        sb.append(String.format("%-20s", "Username"));
        for (String resource : allResources) {
            sb.append(String.format(" %-10s", truncate(resource.toUpperCase(), 10)));
        }
        sb.append("\n");
        sb.append("═".repeat(20 + allResources.size() * 11)).append("\n");

        // Строки пользователей
        for (RecUser user : users) {
            Set<RecPermission> permissions = assignmentManager.getUserPermissions(user);
            Set<String> userResources = permissions.stream()
                    .map(RecPermission::resource)
                    .collect(Collectors.toSet());

            sb.append(String.format("%-20s", user.username()));
            for (String resource : allResources) {
                String mark = userResources.contains(resource) ? "✓" : "✗";
                sb.append(String.format(" %-10s", mark));
            }
            sb.append("\n");
        }

        sb.append("\n✓ — есть доступ, ✗ — нет доступа\n");
        sb.append("Всего ресурсов: ").append(allResources.size()).append("\n");

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        if (report == null || report.trim().isEmpty()) {
            throw new IllegalArgumentException("Отчёт не может быть пустым");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
            System.out.println("Отчёт сохранён в файл: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения отчёта: " + e.getMessage(), e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}