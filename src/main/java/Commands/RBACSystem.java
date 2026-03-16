package Commands;

import Managers.AssignmentManager;
import Managers.RoleManager;
import Managers.UserManager;
import org.example.*;

import java.util.*;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.currentUser = null;
    }

    // Геттеры
    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.currentUser = username.trim();
    }

    // Инициализация системы
    public void initialize() {
        System.out.println("=== Инициализация RBAC-системы ===");

        // 1. Создаём предустановленные права
        RecPermission readUsers = new RecPermission("read", "users", "Чтение пользователей");
        RecPermission writeUsers = new RecPermission("write", "users", "Запись пользователей");
        RecPermission deleteUsers = new RecPermission("delete", "users", "Удаление пользователей");
        RecPermission readRoles = new RecPermission("read", "roles", "Чтение ролей");
        RecPermission writeRoles = new RecPermission("write", "roles", "Запись ролей");
        RecPermission readAssignments = new RecPermission("read", "assignments", "Чтение назначений");
        RecPermission writeAssignments = new RecPermission("write", "assignments", "Запись назначений");

        // 2. Создаём роли
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

        // 3. Создаём тестового администратора
        RecUser adminUser = RecUser.validate("admin", "System Administrator", "admin@system.local");
        userManager.add(adminUser);

        // 4. Назначаем роль Admin администратору
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, meta);
        assignmentManager.add(adminAssignment);

        // 5. Устанавливаем текущего пользователя
        setCurrentUser("admin");

        System.out.println("Система инициализирована успешно!");
        System.out.println(generateStatistics());
    }

    // Генерация статистики
    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Статистика RBAC-системы ===\n");
        sb.append("Пользователей: ").append(userManager.count()).append("\n");
        sb.append("Ролей: ").append(roleManager.count()).append("\n");
        sb.append("Назначений: ").append(assignmentManager.count()).append("\n");
        sb.append("Активных назначений: ").append(assignmentManager.getActiveAssignments().size()).append("\n");
        sb.append("Просроченных назначений: ").append(assignmentManager.getExpiredAssignments().size()).append("\n");
        sb.append("Текущий пользователь: ").append(currentUser != null ? currentUser : "не авторизован").append("\n");
        return sb.toString();
    }
}