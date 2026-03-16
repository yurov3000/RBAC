package Commands;

import Managers.AssignmentManager;
import Managers.RoleManager;
import Managers.UserManager;
import org.example.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser, RBACSystem system) {
        registerUserCommands(parser, system);
        registerRoleCommands(parser, system);
        registerAssignmentCommands(parser, system);
        registerPermissionCommands(parser, system);
        registerServiceCommands(parser, system);
    }

    // === КОМАНДЫ УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ ===

    private static void registerUserCommands(CommandParser parser, RBACSystem system) {
        // user-list
        parser.registerCommand("user-list", "Вывести список всех пользователей", (scanner, sys) -> {
            UserManager um = sys.getUserManager();
            System.out.println("\n=== Пользователи ===");
            if (um.count() == 0) {
                System.out.println("Пользователи не найдены");
                return;
            }
            System.out.printf("%-20s %-25s %-30s%n", "Username", "Full Name", "Email");
            System.out.println("=".repeat(75));
            for (RecUser user : um.findAll()) {
                System.out.printf("%-20s %-25s %-30s%n", user.username(), user.fullname(), user.email());
            }
            System.out.println("Всего: " + um.count());
        });

        // user-create
        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Full Name: ");
            String fullname = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            try {
                RecUser user = RecUser.validate(username, fullname, email);
                sys.getUserManager().add(user);
                System.out.println("Пользователь создан: " + user.format());
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // user-view
        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            System.out.println("\n=== Информация о пользователе ===");
            System.out.println(user.format());
            System.out.println("\nНазначенные роли:");
            var assignments = sys.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("  Нет назначенных ролей");
            } else {
                for (var a : assignments) {
                    System.out.printf("  - %s (%s) [%s]%n", a.role().getName(), a.assignmentType(), a.isActive() ? "ACTIVE" : "INACTIVE");
                }
            }
            System.out.println("\nВсе права:");
            var permissions = sys.getAssignmentManager().getUserPermissions(user);
            if (permissions.isEmpty()) {
                System.out.println("  Нет прав");
            } else {
                for (var p : permissions) {
                    System.out.println("  - " + p.format());
                }
            }
        });

        // user-update
        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            System.out.print("New Full Name [" + user.fullname() + "]: ");
            String fullname = scanner.nextLine().trim();
            if (fullname.isEmpty()) fullname = user.fullname();
            System.out.print("New Email [" + user.email() + "]: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = user.email();
            try {
                RecUser updated = RecUser.validate(username, fullname, email);
                sys.getUserManager().update(user.username(), updated.fullname(), email);
                System.out.println("Пользователь обновлён");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // user-delete
        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            System.out.print("Вы уверены? Введите 'да' для подтверждения: ");
            String confirm = scanner.nextLine().trim();
            if (!"да".equalsIgnoreCase(confirm)) {
                System.out.println("Удаление отменено");
                return;
            }
            // Удалить все назначения
            var assignments = sys.getAssignmentManager().findByUser(user);
            for (var a : assignments) {
                sys.getAssignmentManager().remove(a);
            }
            sys.getUserManager().remove(user);
            System.out.println("✓ Пользователь удалён");
        });

        // user-search
        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, sys) -> {
            System.out.println("\n=== Поиск пользователей ===");
            System.out.println("1. По username");
            System.out.println("2. По email");
            System.out.println("3. По домену email");
            System.out.println("4. По полному имени");
            System.out.print("Выберите фильтр (1-4): ");
            String choice = scanner.nextLine().trim();
            System.out.print("Значение: ");
            String value = scanner.nextLine().trim();
            List<RecUser> results = switch (choice) {
                case "1" -> sys.getUserManager().findAll().stream()
                        .filter(u -> u.username().contains(value))
                        .toList();
                case "2" -> sys.getUserManager().findAll().stream()
                        .filter(u -> u.email().contains(value))
                        .toList();
                case "3" -> sys.getUserManager().findAll().stream()
                        .filter(u -> u.email().endsWith("@" + value))
                        .toList();
                case "4" -> sys.getUserManager().findAll().stream()
                        .filter(u -> u.fullname().contains(value))
                        .toList();
                default -> new ArrayList<>();
            };
            System.out.println("\nНайдено: " + results.size());
            for (RecUser u : results) {
                System.out.println("  " + u.format());
            }
        });
    }

    // === КОМАНДЫ УПРАВЛЕНИЯ РОЛЯМИ ===

    private static void registerRoleCommands(CommandParser parser, RBACSystem system) {
        // role-list
        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, sys) -> {
            System.out.println("\n=== Роли ===");
            RoleManager rm = sys.getRoleManager();
            if (rm.count() == 0) {
                System.out.println("Роли не найдены");
                return;
            }
            System.out.printf("%-20s %-15s %-10s%n", "Name", "Permissions", "ID");
            System.out.println("=".repeat(45));
            for (Role role : rm.findAll()) {
                System.out.printf("%-20s %-15d %-10s%n", role.getName(), role.getPermissions().size(), role.getId());
            }
        });

        // role-create
        parser.registerCommand("role-create", "Создать новую роль", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            System.out.print("Описание: ");
            String desc = scanner.nextLine().trim();
            try {
                Role role = new Role(name, desc);
                sys.getRoleManager().add(role);
                System.out.println("✓ Роль создана: " + role.getName());
                // Предложить добавить права
                while (true) {
                    System.out.print("Добавить право? (да/нет): ");
                    String add = scanner.nextLine().trim();
                    if (!"да".equalsIgnoreCase(add)) break;
                    System.out.print("Имя права: ");
                    String pName = scanner.nextLine().trim();
                    System.out.print("Ресурс: ");
                    String pRes = scanner.nextLine().trim();
                    System.out.print("Описание: ");
                    String pDesc = scanner.nextLine().trim();
                    RecPermission perm = new RecPermission(pName, pRes, pDesc);
                    role.addPermission(perm);
                    System.out.println("✓ Право добавлено");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("✗ Ошибка: " + e.getMessage());
            }
        });

        // role-view
        parser.registerCommand("role-view", "Просмотр роли", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            Role role = sys.getRoleManager().findByUserName(name);
            if (role == null) {
                System.out.println("Роль не найдена");
                return;
            }
            System.out.println("\n" + role.format());
        });

        // role-update
        parser.registerCommand("role-update", "Обновить роль", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            Role role = sys.getRoleManager().findByUserName(name);
            if (role == null) {
                System.out.println("Роль не найдена");
                return;
            }
            System.out.print("Новое название [" + role.getName() + "]: ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty() && !newName.equals(role.getName())) {
                System.out.println("⚠ Изменение имени роли не поддерживается");
            }
            System.out.print("Новое описание [" + role.getDescription() + "]: ");
            String newDesc = scanner.nextLine().trim();
            if (!newDesc.isEmpty()) {
                // Требуется метод в Role для обновления описания
                System.out.println("✓ Описание обновлено (требуется реализация в Role)");
            }
        });

        // role-delete
        parser.registerCommand("role-delete", "Удалить роль", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            Role role = sys.getRoleManager().findByUserName(name);
            if (role == null) {
                System.out.println("Роль не найдена");
                return;
            }
            // Проверка назначений
            var assignments = sys.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("⚠ Роль назначена " + assignments.size() + " пользователям:");
                for (var a : assignments) {
                    System.out.println("  - " + a.user().username());
                }
                System.out.print("Продолжить удаление? (да/нет): ");
                String confirm = scanner.nextLine().trim();
                if (!"да".equalsIgnoreCase(confirm)) {
                    System.out.println("Удаление отменено");
                    return;
                }
                // Удалить назначения
                for (var a : assignments) {
                    sys.getAssignmentManager().remove(a);
                }
            }
            sys.getRoleManager().remove(role);
            System.out.println("✓ Роль удалена");
        });

        // role-add-permission
        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            Role role = sys.getRoleManager().findByUserName(name);
            if (role == null) {
                System.out.println("Роль не найдена");
                return;
            }
            System.out.print("Имя права: ");
            String pName = scanner.nextLine().trim();
            System.out.print("Ресурс: ");
            String pRes = scanner.nextLine().trim();
            System.out.print("Описание: ");
            String pDesc = scanner.nextLine().trim();
            RecPermission perm = new RecPermission(pName, pRes, pDesc);
            role.addPermission(perm);
            System.out.println("✓ Право добавлено к роли");
        });

        // role-remove-permission
        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            Role role = sys.getRoleManager().findByUserName(name);
            if (role == null) {
                System.out.println("Роль не найдена");
                return;
            }
            var perms = role.getPermissions();
            if (perms.isEmpty()) {
                System.out.println("Нет прав для удаления");
                return;
            }
            System.out.println("\nПрава роли:");
            int i = 1;
            for (var p : perms) {
                System.out.println(i++ + ". " + p.format());
            }
            System.out.print("Номер права для удаления: ");
            int num = Integer.parseInt(scanner.nextLine().trim());
            if (num < 1 || num > perms.size()) {
                System.out.println("Неверный номер");
                return;
            }
            RecPermission toRemove = perms.toArray(new RecPermission[0])[num - 1];
            role.removePermission(toRemove);
            System.out.println("✓ Право удалено");
        });

        // role-search
        parser.registerCommand("role-search", "Поиск ролей", (scanner, sys) -> {
            System.out.println("\n=== Поиск ролей ===");
            System.out.println("1. По имени");
            System.out.println("2. По наличию права");
            System.out.println("3. По минимальному количеству прав");
            System.out.print("Выберите фильтр (1-3): ");
            String choice = scanner.nextLine().trim();
            List<Role> results = switch (choice) {
                case "1" -> {
                    System.out.print("Имя (содержит): ");
                    String name = scanner.nextLine().trim();
                    yield sys.getRoleManager().findAll().stream()
                            .filter(r -> r.getName().contains(name))
                            .toList();
                }
                case "2" -> {
                    System.out.print("Имя права: ");
                    String pName = scanner.nextLine().trim();
                    System.out.print("Ресурс: ");
                    String pRes = scanner.nextLine().trim();
                    yield sys.getRoleManager().findAll().stream()
                            .filter(r -> r.hasPermission(pName, pRes))
                            .toList();
                }
                case "3" -> {
                    System.out.print("Мин. количество прав: ");
                    int min = Integer.parseInt(scanner.nextLine().trim());
                    yield sys.getRoleManager().findAll().stream()
                            .filter(r -> r.getPermissions().size() >= min)
                            .toList();
                }
                default -> new ArrayList<>();
            };
            System.out.println("\nНайдено: " + results.size());
            for (Role r : results) {
                System.out.println("  - " + r.getName() + " (" + r.getPermissions().size() + " прав)");
            }
        });
    }

    // === КОМАНДЫ УПРАВЛЕНИЯ НАЗНАЧЕНИЯМИ ===

    private static void registerAssignmentCommands(CommandParser parser, RBACSystem system) {
        // assign-role
        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            System.out.println("\nДоступные роли:");
            int i = 1;
            for (Role r : sys.getRoleManager().findAll()) {
                System.out.println(i++ + ". " + r.getName());
            }
            System.out.print("Выберите роль (номер): ");
            int roleNum = Integer.parseInt(scanner.nextLine().trim());
            Role[] roles = sys.getRoleManager().findAll().toArray(new Role[0]);
            if (roleNum < 1 || roleNum > roles.length) {
                System.out.println("Неверный номер");
                return;
            }
            Role role = roles[roleNum - 1];
            System.out.print("Тип назначения (permanent/temporary): ");
            String type = scanner.nextLine().trim().toLowerCase();
            System.out.print("Причина: ");
            String reason = scanner.nextLine().trim();
            AssignmentMetadata meta = AssignmentMetadata.now(sys.getCurrentUser(), reason);
            try {
                if ("temporary".equals(type)) {
                    System.out.print("Дата истечения (YYYY-MM-DD HH:mm): ");
                    String expires = scanner.nextLine().trim();
                    TemporaryAssignment assign = new TemporaryAssignment(user, role, meta, expires);
                    sys.getAssignmentManager().add(assign);
                } else {
                    PermanentAssignment assign = new PermanentAssignment(user, role, meta);
                    sys.getAssignmentManager().add(assign);
                }
                System.out.println("✓ Роль назначена");
            } catch (IllegalArgumentException e) {
                System.out.println("✗ Ошибка: " + e.getMessage());
            }
        });

        // revoke-role
        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            var assignments = sys.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();
            if (assignments.isEmpty()) {
                System.out.println("Нет активных назначений");
                return;
            }
            System.out.println("\nАктивные назначения:");
            int i = 1;
            for (var a : assignments) {
                System.out.println(i++ + ". " + a.role().getName() + " (" + a.assignmentType() + ")");
            }
            System.out.print("Выберите назначение (номер): ");
            int num = Integer.parseInt(scanner.nextLine().trim());
            if (num < 1 || num > assignments.size()) {
                System.out.println("Неверный номер");
                return;
            }
            RoleAssignment selected = assignments.get(num - 1);
            if (selected instanceof PermanentAssignment perm) {
                perm.revoke();
                System.out.println("✓ Роль отозвана");
            } else {
                System.out.println("⚠ Временные назначения не отзывются, только истекают");
            }
        });

        // assignment-list
        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, sys) -> {
            System.out.println("\n=== Назначения ===");
            var assignments = sys.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("Назначений нет");
                return;
            }
            System.out.printf("%-15s %-20s %-15s %-10s %-20s%n", "User", "Role", "Type", "Status", "Assigned At");
            System.out.println("=".repeat(80));
            for (var a : assignments) {
                System.out.printf("%-15s %-20s %-15s %-10s %-20s%n",
                        a.user().username(),
                        a.role().getName(),
                        a.assignmentType(),
                        a.isActive() ? "ACTIVE" : "INACTIVE",
                        a.metadata().assignedAt());
            }
        });

        // assignment-list-user
        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            var assignments = sys.getAssignmentManager().findByUser(user);
            System.out.println("\nНазначения для " + username + ":");
            for (var a : assignments) {
                System.out.println("  - " + a.role().getName() + " (" + a.assignmentType() + ") [" + (a.isActive() ? "ACTIVE" : "INACTIVE") + "]");
            }
        });

        // assignment-list-role
        parser.registerCommand("assignment-list-role", "Пользователи с конкретной ролью", (scanner, sys) -> {
            System.out.print("Название роли: ");
            String roleName = scanner.nextLine().trim();
            Role role = sys.getRoleManager().findByUserName(roleName);
            if (role == null) {
                System.out.println("Роль не найдена");
                return;
            }
            var assignments = sys.getAssignmentManager().findByRole(role);
            System.out.println("\nПользователи с ролью " + roleName + ":");
            for (var a : assignments) {
                System.out.println("  - " + a.user().username() + " [" + (a.isActive() ? "ACTIVE" : "INACTIVE") + "]");
            }
        });

        // assignment-active
        parser.registerCommand("assignment-active", "Только активные назначения", (scanner, sys) -> {
            var assignments = sys.getAssignmentManager().getActiveAssignments();
            System.out.println("\nАктивные назначения: " + assignments.size());
            for (var a : assignments) {
                System.out.println("  - " + a.user().username() + " → " + a.role().getName());
            }
        });

        // assignment-expired
        parser.registerCommand("assignment-expired", "Истёкшие временные назначения", (scanner, sys) -> {
            var assignments = sys.getAssignmentManager().getExpiredAssignments();
            System.out.println("\nИстёкшие назначения: " + assignments.size());
            for (var a : assignments) {
                System.out.println("  - " + a.user().username() + " → " + a.role().getName());
            }
        });

        // assignment-extend
        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, sys) -> {
            System.out.print("Assignment ID: ");
            String id = scanner.nextLine().trim();
            System.out.print("Новая дата истечения: ");
            String newDate = scanner.nextLine().trim();
            try {
                sys.getAssignmentManager().extendTemporaryAssignment(id, newDate);
                System.out.println("✓ Назначение продлено");
            } catch (Exception e) {
                System.out.println("✗ Ошибка: " + e.getMessage());
            }
        });

        // assignment-search
        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (scanner, sys) -> {
            System.out.println("\n=== Поиск назначений ===");
            System.out.println("1. По пользователю");
            System.out.println("2. По роли");
            System.out.println("3. По типу");
            System.out.println("4. По статусу");
            System.out.print("Выберите фильтр (1-4): ");
            String choice = scanner.nextLine().trim();
            List<RoleAssignment> results = switch (choice) {
                case "1" -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    RecUser user = sys.getUserManager().findByName(username);
                    yield user != null ? sys.getAssignmentManager().findByUser(user) : new ArrayList<>();
                }
                case "2" -> {
                    System.out.print("Role name: ");
                    String roleName = scanner.nextLine().trim();
                    Role role = sys.getRoleManager().findByUserName(roleName);
                    yield role != null ? sys.getAssignmentManager().findByRole(role) : new ArrayList<>();
                }
                case "3" -> {
                    System.out.print("Тип (PERMANENT/TEMPORARY): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    yield sys.getAssignmentManager().findAll().stream()
                            .filter(a -> a.assignmentType().equals(type))
                            .toList();
                }
                case "4" -> {
                    System.out.print("Статус (active/inactive): ");
                    String status = scanner.nextLine().trim().toLowerCase();
                    yield sys.getAssignmentManager().findAll().stream()
                            .filter(a -> "active".equals(status) ? a.isActive() : !a.isActive())
                            .toList();
                }
                default -> new ArrayList<>();
            };
            System.out.println("\nНайдено: " + results.size());
        });
    }

    // === КОМАНДЫ ПРОСМОТРА ПРАВ ===

    private static void registerPermissionCommands(CommandParser parser, RBACSystem system) {
        // permissions-user
        parser.registerCommand("permissions-user", "Все права конкретного пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            var permissions = sys.getAssignmentManager().getUserPermissions(user);
            System.out.println("\nПрава пользователя " + username + ":");
            // Группировка по ресурсам
            Map<String, List<RecPermission>> byResource = permissions.stream()
                    .collect(Collectors.groupingBy(RecPermission::resource));
            for (var entry : byResource.entrySet()) {
                System.out.println("  " + entry.getKey() + ":");
                for (var p : entry.getValue()) {
                    System.out.println("    - " + p.name() + ": " + p.description());
                }
            }
        });

        // permissions-check
        parser.registerCommand("permissions-check", "Проверить право пользователя", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            RecUser user = sys.getUserManager().findByName(username);
            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }
            System.out.print("Permission name: ");
            String pName = scanner.nextLine().trim();
            System.out.print("Resource: ");
            String resource = scanner.nextLine().trim();
            boolean hasPermission = sys.getAssignmentManager().userHasPermission(user, pName.toUpperCase(), resource.toLowerCase());
            if (hasPermission) {
                System.out.println("✓ Право есть");
                // Найти роль
                var assignments = sys.getAssignmentManager().findByUser(user);
                for (var a : assignments) {
                    if (a.role().hasPermission(pName, resource)) {
                        System.out.println("  Из роли: " + a.role().getName());
                    }
                }
            } else {
                System.out.println("✗ Права нет");
            }
        });
    }

    // === СЛУЖЕБНЫЕ КОМАНДЫ ===

    private static void registerServiceCommands(CommandParser parser, RBACSystem system) {
        // help
        parser.registerCommand("help", "Справка по командам", (scanner, sys) -> {
            parser.printHelp();
        });

        // stats
        parser.registerCommand("stats", "Статистика системы", (scanner, sys) -> {
            System.out.println("\n" + sys.generateStatistics());
            // Дополнительно
            AssignmentManager am = sys.getAssignmentManager();
            int total = am.count();
            int active = am.getActiveAssignments().size();
            int expired = am.getExpiredAssignments().size();
            System.out.println("Назначений: " + total + " (активных: " + active + ", истёкших: " + expired + ")");
            // Среднее ролей на пользователя
            int users = sys.getUserManager().count();
            if (users > 0) {
                double avg = (double) total / users;
                System.out.printf("Среднее назначений на пользователя: %.2f%n", avg);
            }
            // Топ-3 ролей
            System.out.println("\nТоп-3 ролей по назначениям:");
            Map<String, Long> roleCount = am.findAll().stream()
                    .collect(Collectors.groupingBy(a -> a.role().getName(), Collectors.counting()));
            roleCount.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(3)
                    .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
        });

        // clear
        parser.registerCommand("clear", "Очистить экран", (scanner, sys) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("Экран очищен");
        });

        // exit
        parser.registerCommand("exit", "Выход из программы", (scanner, sys) -> {
            System.out.print("Вы уверены? (да/нет): ");
            String confirm = scanner.nextLine().trim();
            if ("да".equalsIgnoreCase(confirm)) {
                System.out.println("Выход из системы...");
                System.exit(0);
            } else {
                System.out.println("Выход отменён");
            }
        });

        // save (опционально)
        parser.registerCommand("save", "Сохранить данные в файл", (scanner, sys) -> {
            System.out.println("⚠ Функция сохранения не реализована");
        });

        // load (опционально)
        parser.registerCommand("load", "Загрузить данные из файла", (scanner, sys) -> {
            System.out.println("⚠ Функция загрузки не реализована");
        });
    }
}