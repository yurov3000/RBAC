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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //Отчёт по всем пользователям с их ролями (использует parallelStream)
    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║           ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ И РОЛЯМ (Parallel)       ║\n");
        sb.append("║           Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("           ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        List<RecUser> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователи не найдены\n");
            return sb.toString();
        }

        // Используем parallelStream для маппинга данных (ускоряет обработку при большом количестве пользователей)
        List<String> reportLines = users.parallelStream()
                .map(user -> {
                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);
                    List<String> roleNames = assignments.stream()
                            .filter(RoleAssignment::isActive)
                            .map(a -> a.role().getName())
                            .collect(Collectors.toList());

                    String rolesStr = roleNames.isEmpty() ? "Нет ролей" : String.join(", ", roleNames);

                    return String.format("%-20s %-25s %-30s %-15s",
                            user.username(),
                            truncate(user.fullname(), 24),
                            truncate(user.email(), 29),
                            truncate(rolesStr, 14));
                })
                .sorted() // Сортируем после параллельной обработки, чтобы вывод был упорядочен
                .collect(Collectors.toList());

        // Заголовок таблицы
        sb.append(String.format("%-20s %-25s %-30s %-15s%n", "Username", "Full Name", "Email", "Roles"));
        sb.append("═".repeat(90)).append("\n");

        // Вывод строк
        for (String line : reportLines) {
            sb.append(line).append("\n");
        }

        sb.append("\n").append("Всего пользователей: ").append(users.size()).append("\n");

        // Подсчет пользователей с ролями (можно тоже параллелить, но тут быстро)
        long usersWithRoles = users.stream()
                .filter(u -> !assignmentManager.findByUser(u).stream()
                        .filter(RoleAssignment::isActive)
                        .findAny().isPresent())
                .count();
        sb.append("Пользователей с ролями: ").append(usersWithRoles).append("\n");

        return sb.toString();
    }

    //Отчёт по ролям с количеством пользователей
    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================================================\n");
        sb.append("                 ОТЧЁТ ПО РОЛЯМ                             \n");
        sb.append("           Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("            \n");
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

    //Матрица прав (пользователи × ресурсы) с использованием parallelStream

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================================================\n");
        sb.append("                 МАТРИЦА ПРАВ ДОСТУПА (Parallel)            \n");
        sb.append("           Сформирован: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("            \n");
        sb.append("==============================================================\n\n");

        List<RecUser> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователи не найдены\n");
            return sb.toString();
        }

        // Собираем все уникальные ресурсы ПАРАЛЛЕЛЬНО
        // Используем ConcurrentSkipListSet или просто TreeSet после сбора, но для сбора лучше ConcurrentHashMap.newKeySet()
        Set<String> allResources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(RecPermission::resource)
                .collect(Collectors.toCollection(TreeSet::new)); // Сразу сортируем

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

        // Строки пользователей (последовательно для корректного вывода порядка)
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

    //Параллельная фильтрация назначений (новое требование задания)
    public List<RoleAssignment> findByFilterParallel(AssignmentManager assignmentManager, Filters.AssignmentFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }
        // Получаем все назначения и фильтруем их параллельно
        return assignmentManager.findAll().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    //Сохранение отчёта в файл
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

    //Утилитарный метод для обрезки длинных строк
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}