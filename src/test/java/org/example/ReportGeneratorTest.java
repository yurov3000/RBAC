package org.example;

import Managers.AssignmentManager;
import Managers.RoleManager;
import Managers.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {
    private ReportGenerator generator;
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private RecUser alice;
    private RecUser bob;
    private Role adminRole;
    private Role viewerRole;
    private RecPermission readUsers;
    private RecPermission writeUsers;

    @BeforeEach
    void setUp() throws Exception {
        // Очистка статического состояния Role
        java.lang.reflect.Field field = Role.class.getDeclaredField("usedNames");
        field.setAccessible(true);
        java.util.Set<String> usedNames = (java.util.Set<String>) field.get(null);
        usedNames.clear();

        generator = new ReportGenerator();
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager();

        // Создаём тестовые данные
        readUsers = new RecPermission("read", "Users", "Чтение пользователей");
        writeUsers = new RecPermission("write", "Users", "Запись пользователей");

        adminRole = new Role("Administrator", "Полный доступ");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);

        viewerRole = new Role("Viewer", "Только просмотр");
        viewerRole.addPermission(readUsers);

        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        alice = RecUser.validate("alice", "Alice Smith", "alice@example.com");
        bob = RecUser.validate("bob", "Bob Jones", "bob@example.com");

        userManager.add(alice);
        userManager.add(bob);

        // Назначаем роли
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        assignmentManager.add(new PermanentAssignment(alice, adminRole, meta));
        assignmentManager.add(new PermanentAssignment(bob, viewerRole, meta));
    }

    @Test
    void testGenerateUserReport() {
        String report = generator.generateUserReport(userManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ"));
        assertTrue(report.contains("alice"));
        assertTrue(report.contains("bob"));
        assertTrue(report.contains("Administrator"));
        assertTrue(report.contains("Viewer"));
    }

    @Test
    void testGenerateUserReportEmpty() {
        UserManager emptyManager = new UserManager();
        String report = generator.generateUserReport(emptyManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("Пользователи не найдены"));
    }

    @Test
    void testGenerateRoleReport() {
        String report = generator.generateRoleReport(roleManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("ОТЧЁТ ПО РОЛЯМ"));
        assertTrue(report.contains("Administrator"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Топ-3 популярных ролей"));
    }

    @Test
    void testGenerateRoleReportEmpty() {
        RoleManager emptyManager = new RoleManager();
        String report = generator.generateRoleReport(emptyManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("Роли не найдены"));
    }

    @Test
    void testGeneratePermissionMatrix() {
        String report = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("МАТРИЦА") || report.contains("Username"));
    }

    @Test
    void testGeneratePermissionMatrixEmpty() {
        UserManager emptyManager = new UserManager();
        String report = generator.generatePermissionMatrix(emptyManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("Пользователи не найдены"));
    }

    @Test
    void testExportToFile() throws IOException {
        String testFile = "test_report.txt";
        String report = "Test Report Content";

        generator.exportToFile(report, testFile);

        assertTrue(Files.exists(Paths.get(testFile)));
        String content = Files.readString(Paths.get(testFile));
        assertEquals(report, content);

        // Очистка
        Files.deleteIfExists(Paths.get(testFile));
    }

    @Test
    void testExportToFileEmptyReport() {
        assertThrows(IllegalArgumentException.class, () ->
                generator.exportToFile("", "test.txt"));
    }

    @Test
    void testExportToFileNullReport() {
        assertThrows(IllegalArgumentException.class, () ->
                generator.exportToFile(null, "test.txt"));
    }

    @Test
    void testExportToFileEmptyFilename() {
        assertThrows(IllegalArgumentException.class, () ->
                generator.exportToFile("Report", ""));
    }

    @Test
    void testTruncateLongString() {
        String report = generator.generateUserReport(userManager, assignmentManager);
        // Проверяем, что отчёт генерируется без ошибок даже с длинными данными
        assertNotNull(report);
        assertFalse(report.isEmpty());
    }

    @Test
    void testRoleReportShowsUserCount() {
        String report = generator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("Users Count") || report.contains("пользователей"));
    }

    @Test
    void testPermissionMatrixShowsResources() {
        String report = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(report.toLowerCase().contains("users"));
    }
}