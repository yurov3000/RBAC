package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import Managers.AssignmentManager;
import java.lang.reflect.Field;

class MainTest {

    private RecPermission readUsers;
    private RecPermission writeReports;
    private Role adminRole;
    private Role userRole;
    private RecUser alice;
    private RecUser bob;
    private AssignmentMetadata metaAdmin;
    private AssignmentMetadata metaBob;

    @BeforeEach
    void setUp() throws Exception {
        // Очистка статического состояния Role
        Field field = Role.class.getDeclaredField("usedNames");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> usedNames = (Set<String>) field.get(null);
        usedNames.clear();

        // Очистка счётчика (опционально)
        Field counterField = Role.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        counterField.setLong(null, 1);

        // Инициализация объектов с валидными данными
        readUsers = new RecPermission("read", "Users", "Позволяет читать пользователей");
        writeReports = new RecPermission("write", "REPORTS", "Позволяет создавать отчёты");
        adminRole = new Role("Administrator", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeReports);
        userRole = new Role("User", "Basic access");
        alice = RecUser.validate("alice", "Alice Smith", "alice@example.com");
        bob = RecUser.validate("bob_dev", "Bob Developer", "bob@example.com");
        metaAdmin = AssignmentMetadata.now("admin", "test");
        metaBob = AssignmentMetadata.now("sec", "test");
    }

    // === Тесты RecUser ===

    @Test
    void testValidRecUser() {
        RecUser user = RecUser.validate("john_doe", "John Doe", "johndoe@example.com");
        assertEquals("john_doe (John Doe) <johndoe@example.com>", user.format());
    }

    @Test
    void testInvalidUsernameFormat() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> RecUser.validate("invalid@username", "Test User", "test@example.com")
        );
        assertTrue(ex.getMessage().contains("формат"));
    }

    @Test
    void testEmptyFullname() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> RecUser.validate("valid_user", "", "valid@email.com")
        );
        // Проверяем не точное совпадение, а наличие ключевого слова
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("полное имя") || msg.contains("fullname"));
    }

    // === Тесты RecPermission ===

    @Test
    void testRecPermissionFormat() {
        assertEquals("READ on users: Позволяет читать пользователей", readUsers.format());
    }

    @Test
    void testMatches() {
        assertTrue(readUsers.matches("READ", "US"));
        assertTrue(writeReports.matches("WRI", "REP"));
        assertFalse(readUsers.matches("WRITE", "US"));
    }

    // === Тесты Role ===

    @Test
    void testRoleFormat() {
        String formatted = adminRole.format();
        assertTrue(formatted.contains("Role: Administrator [ID: role_"));
        assertTrue(formatted.contains("Description: Full system access"));
        assertTrue(formatted.contains("Permissions (2):"));
        assertTrue(formatted.contains("- READ on users: Позволяет читать пользователей"));
        assertTrue(formatted.contains("- WRITE on reports: Позволяет создавать отчёты"));
    }

    @Test
    void testRoleEqualsAndHashCode() {
        Role sameIdRole = new Role("Temp", "Temp desc");
        // Принудительно установим тот же ID (только для теста, в реальности ID уникален)
        // Но лучше не трогать ID — equals работает по нему, а он генерируется автоматически
        // Поэтому сравним два разных объекта — они НЕ равны
        assertNotEquals(adminRole, userRole);
        assertNotEquals(adminRole.hashCode(), userRole.hashCode());
    }

    // === Тесты PermanentAssignment ===

    @Test
    void testPermanentAssignmentActiveByDefault() {
        PermanentAssignment assignment = new PermanentAssignment(alice, adminRole, metaAdmin);
        assertTrue(assignment.isActive());
        assertFalse(assignment.isRevoked());
        assertEquals("PERMANENT", assignment.assignmentType());
    }

    @Test
    void testPermanentAssignmentAfterRevoke() {
        PermanentAssignment assignment = new PermanentAssignment(alice, adminRole, metaAdmin);
        assignment.revoke();
        assertFalse(assignment.isActive());
        assertTrue(assignment.isRevoked());
    }

    // === Тесты TemporaryAssignment ===

    @Test
    void testTemporaryAssignmentIsActive() {
        String futureDate = "2030-12-31 23:59";
        TemporaryAssignment assignment = new TemporaryAssignment(bob, adminRole, metaBob, futureDate);
        assertTrue(assignment.isActive());
        assertFalse(assignment.isExpired());
    }

    @Test
    void testTemporaryAssignmentIsExpired() {
        String pastDate = "2020-01-01 00:00";
        TemporaryAssignment assignment = new TemporaryAssignment(bob, adminRole, metaBob, pastDate);
        assertFalse(assignment.isActive());
        assertTrue(assignment.isExpired());
    }

    @Test
    void testTemporaryAssignmentExtend() {
        TemporaryAssignment assignment = new TemporaryAssignment(bob, adminRole, metaBob, "2026-02-17 12:00");
        assignment.extend("2026-03-01 09:00");
        assertEquals("2026-03-01 09:00", assignment.getExpiresAt());
    }

    // === Тесты AssignmentMetadata ===

    @Test
    void testAssignmentMetadataNow() {
        AssignmentMetadata meta = AssignmentMetadata.now("tester", "JUnit test");
        assertNotNull(meta.assignedAt());
        assertEquals("tester", meta.assignedBy());
        assertEquals("JUnit test", meta.reason());
    }

    @Test
    void testAssignmentMetadataFormatWithReason() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Setup");
        String formatted = meta.format();
        assertTrue(formatted.contains("Assigned by admin at"));
        assertTrue(formatted.contains("Reason: Setup"));
    }

    @Test
    void testAssignmentMetadataFormatWithoutReason() {
        AssignmentMetadata meta = AssignmentMetadata.now("user", "");
        String formatted = meta.format();
        assertTrue(formatted.contains("no reason provided"));
    }

    // === Тесты AssignmentManager (если вы его реализовали) ===
    // Если у вас есть AssignmentManager — раскомментируйте и доработайте

    // === Тесты AssignmentManager ===

    @Test
    void testAssignmentManagerAddPermanentAssignment() {
        AssignmentManager manager = new AssignmentManager();
        PermanentAssignment assignment = new PermanentAssignment(alice, adminRole, metaAdmin);

        manager.add(assignment);

        assertEquals(1, manager.count());
        assertTrue(manager.findById(assignment.assignmentId()).isPresent());
        assertEquals(assignment, manager.findById(assignment.assignmentId()).get());

        // Проверка по пользователю
        List<RoleAssignment> aliceAssignments = manager.findByUser(alice);
        assertEquals(1, aliceAssignments.size());
        assertEquals(assignment, aliceAssignments.get(0));
    }

    @Test
    void testAssignmentManagerRevokePermanentAssignment() {
        AssignmentManager manager = new AssignmentManager();
        PermanentAssignment assignment = new PermanentAssignment(alice, adminRole, metaAdmin);
        manager.add(assignment);

        assertTrue(assignment.isActive());
        assertTrue(manager.userHasRole(alice, adminRole));

        // Отзыв через менеджер
        manager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
        assertFalse(manager.userHasRole(alice, adminRole));

        // Проверка активных назначений
        assertTrue(manager.getActiveAssignments().isEmpty());
    }

    @Test
    void testAssignmentManagerAddTemporaryAssignment() {
        AssignmentManager manager = new AssignmentManager();
        PermanentAssignment assignment = new PermanentAssignment(bob, adminRole, metaBob);
        manager.add(assignment);

        assertTrue(manager.userHasPermission(bob, "READ", "users"));
    }

    @Test
    void testAssignmentManagerExtendTemporaryAssignment() {
        AssignmentManager manager = new AssignmentManager();
        TemporaryAssignment assignment = new TemporaryAssignment(bob, adminRole, metaBob, "2026-02-17 12:00");
        manager.add(assignment);

        assertEquals("2026-02-17 12:00", assignment.getExpiresAt());

        manager.extendTemporaryAssignment(assignment.assignmentId(), "2026-03-01 09:00");

        assertEquals("2026-03-01 09:00", assignment.getExpiresAt());
    }

    @Test
    void testAssignmentManagerFilters() {
        AssignmentManager manager = new AssignmentManager();

        PermanentAssignment perm = new PermanentAssignment(alice, adminRole, metaAdmin);
        String futureDate = "2030-12-31 23:59";
        TemporaryAssignment temp = new TemporaryAssignment(bob, userRole, metaBob, futureDate);

        manager.add(perm);
        manager.add(temp);

        // Фильтр по активным
        List<RoleAssignment> active = manager.findAll(
                assignment -> assignment.isActive(),
                Comparator.comparing(RoleAssignment::assignmentId)
        );
        assertEquals(2, active.size());

        // Фильтр по пользователю
        List<RoleAssignment> bobAssignments = manager.findByFilter(
                assignment -> assignment.user().username().equals("bob_dev")
        );
        assertEquals(1, bobAssignments.size());
        assertEquals(temp, bobAssignments.get(0));
    }

    @Test
    void testAssignmentManagerUserPermissions() {
        AssignmentManager manager = new AssignmentManager(); // ← обязательно!
        PermanentAssignment assignment = new PermanentAssignment(alice, adminRole, metaAdmin);
        manager.add(assignment);

        assertTrue(manager.userHasPermission(alice, "READ", "users"));
        assertTrue(manager.userHasPermission(alice, "WRITE", "reports"));
        assertFalse(manager.userHasPermission(alice, "delete", "users"));

        Set<RecPermission> permissions = manager.getUserPermissions(alice);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains(readUsers));
        assertTrue(permissions.contains(writeReports));
    }

    @Test
    void testAssignmentManagerExpiredAssignments() {
        AssignmentManager manager = new AssignmentManager();
        // Просроченное назначение
        TemporaryAssignment expired = new TemporaryAssignment(bob, userRole, metaBob, "2020-01-01 00:00");
        // Активное
        PermanentAssignment active = new PermanentAssignment(alice, adminRole, metaAdmin);

        manager.add(expired);
        manager.add(active);

        List<RoleAssignment> expiredList = manager.getExpiredAssignments();
        assertEquals(1, expiredList.size());
        assertEquals(expired, expiredList.get(0));
    }

    @Test
    void testDuplicateActiveAssignmentNotAllowed() {
        AssignmentManager manager = new AssignmentManager();
        PermanentAssignment assign1 = new PermanentAssignment(alice, adminRole, metaAdmin);
        manager.add(assign1);

        // Попытка добавить второе активное назначение той же роли — должно выбросить исключение
        PermanentAssignment assign2 = new PermanentAssignment(alice, adminRole, metaAdmin);
        assertThrows(IllegalArgumentException.class, () -> manager.add(assign2));
    }

    @Test
    void testRevokeNonPermanentAssignmentFails() {
        AssignmentManager manager = new AssignmentManager();
        TemporaryAssignment temp = new TemporaryAssignment(bob, adminRole, metaBob, "2030-12-31 23:59");
        manager.add(temp);

        assertThrows(IllegalStateException.class, () -> manager.revokeAssignment(temp.assignmentId()));
    }

    @Test
    void testExtendNonTemporaryAssignmentFails() {
        AssignmentManager manager = new AssignmentManager();
        PermanentAssignment perm = new PermanentAssignment(alice, adminRole, metaAdmin);
        manager.add(perm);

        assertThrows(IllegalStateException.class, () -> manager.extendTemporaryAssignment(perm.assignmentId(), "2026-03-01"));
    }
}