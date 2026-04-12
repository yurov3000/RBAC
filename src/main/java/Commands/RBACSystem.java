package Commands;

import Managers.AssignmentManager;
import Managers.RoleManager;
import Managers.UserManager;
import org.example.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private String currentUser;

    // Планировщик фоновых задач
    private final ScheduledExecutorService scheduler;
    private static final long CLEANUP_INTERVAL_SECONDS = 30;  // Проверка истёкших назначений каждые 30 сек
    private static final long STATS_INTERVAL_SECONDS = 60;    // Статистика каждые 60 сек

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.auditLog = new AuditLog();
        this.currentUser = null;

        // Инициализация планировщика
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RBAC-Scheduler");
            t.setDaemon(true);
            return t;
        });

        // Запуск фоновых задач после инициализации
        startBackgroundTasks();
    }

    // === Геттеры ===
    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() { return auditLog; }
    public String getCurrentUser() { return currentUser; }

    public void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.currentUser = username.trim();
    }

    // === Инициализация системы ===
    public void initialize() {
        System.out.println("=== Инициализация RBAC-системы ===");

        // Создаём предустановленные права
        RecPermission readUsers = new RecPermission("read", "users", "Чтение пользователей");
        RecPermission writeUsers = new RecPermission("write", "users", "Запись пользователей");
        RecPermission deleteUsers = new RecPermission("delete", "users", "Удаление пользователей");
        RecPermission readRoles = new RecPermission("read", "roles", "Чтение ролей");
        RecPermission writeRoles = new RecPermission("write", "roles", "Запись ролей");
        RecPermission readAssignments = new RecPermission("read", "assignments", "Чтение назначений");
        RecPermission writeAssignments = new RecPermission("write", "assignments", "Запись назначений");

        // Создаём роли
        Role adminRole = new Role("Admin", "Полный доступ к системе");
        Role managerRole = new Role("Manager", "Управление пользователями");
        Role viewerRole = new Role("Viewer", "Только просмотр");

        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(readAssignments);
        adminRole.addPermission(writeAssignments);

        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readRoles);
        managerRole.addPermission(readAssignments);

        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(readAssignments);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        // Создаём тестового администратора
        RecUser adminUser = RecUser.validate("admin", "System Administrator", "admin@system.local");
        userManager.add(adminUser);

        // Назначаем роль Admin администратору
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, meta);
        assignmentManager.add(adminAssignment);

        // Устанавливаем текущего пользователя
        setCurrentUser("admin");

        // Логирование инициализации
        auditLog.log("SYSTEM_INIT", "system", "RBAC", "Система инициализирована");

        System.out.println("Система инициализирована успешно!");
        System.out.println(generateStatistics());
    }

    // === Запуск фоновых задач ===
    private void startBackgroundTasks() {
        // Задача 1: Очистка/логирование истёкших временных назначений
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
                if (!expired.isEmpty()) {
                    auditLog.log(
                            "SCHEDULER_CLEANUP",
                            "SYSTEM",
                            "TemporaryAssignments",
                            "Обнаружено истёкших назначений: " + expired.size()
                    );
                    System.out.println("[Scheduler] Обнаружено истёкших назначений: " + expired.size());

                    // Логируем каждое истёкшее назначение
                    for (RoleAssignment ra : expired) {
                        if (ra instanceof TemporaryAssignment temp) {
                            auditLog.log(
                                    "ASSIGNMENT_EXPIRED",
                                    "SYSTEM",
                                    temp.user().username(),
                                    "Роль '" + temp.role().getName() + "' истекла " + temp.getExpiresAt()
                            );
                        }
                    }
                }
            } catch (Exception e) {
                auditLog.log("SCHEDULER_ERROR", "SYSTEM", "Cleanup", "Ошибка: " + e.getMessage());
                System.err.println("[Scheduler] Ошибка при очистке: " + e.getMessage());
            }
        }, 10, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // Задача 2: Периодическая статистика
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String stats = generateStatistics();
                System.out.println("\n[Scheduler Stats] " +
                        "Users: " + userManager.count() +
                        ", Roles: " + roleManager.count() +
                        ", Assignments: " + assignmentManager.count() +
                        ", Audit: " + auditLog.count());

                // Логируем статистику (опционально, чтобы не засорять лог)
                // auditLog.log("SCHEDULER_STATS", "SYSTEM", "RBAC", stats);
            } catch (Exception e) {
                System.err.println("[Scheduler] Ошибка статистики: " + e.getMessage());
            }
        }, 30, STATS_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    // === Генерация статистики ===
    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Статистика RBAC-системы ===\n");
        sb.append("Пользователей: ").append(userManager.count()).append("\n");
        sb.append("Ролей: ").append(roleManager.count()).append("\n");
        sb.append("Назначений: ").append(assignmentManager.count()).append("\n");
        sb.append("Записей в аудите: ").append(auditLog.count()).append("\n");
        sb.append("Активных назначений: ").append(assignmentManager.getActiveAssignments().size()).append("\n");
        sb.append("Просроченных назначений: ").append(assignmentManager.getExpiredAssignments().size()).append("\n");
        sb.append("Текущий пользователь: ").append(currentUser != null ? currentUser : "не авторизован").append("\n");
        return sb.toString();
    }

    // === Корректное завершение работы ===
    public void shutdown() {
        System.out.println("\nОстановка фоновых задач...");

        // Останавливаем планировщик
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    System.out.println("Планировщик остановлен принудительно");
                } else {
                    System.out.println("Планировщик остановлен корректно");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Останавливаем BackgroundExecutor
        BackgroundExecutor.shutdown();

        System.out.println("Система остановлена.");
    }
}