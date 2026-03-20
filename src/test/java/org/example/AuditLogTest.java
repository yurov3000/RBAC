package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void testLogEntry() {
        auditLog.log("USER_CREATE", "admin", "john_doe", "New user created");
        assertEquals(1, auditLog.count());
    }

    @Test
    void testGetAll() {
        auditLog.log("USER_CREATE", "admin", "user1", "Details 1");
        auditLog.log("ROLE_CREATE", "admin", "Manager", "Details 2");
        auditLog.log("USER_DELETE", "admin", "user2", "Details 3");

        List<AuditEntry> all = auditLog.getAll();
        assertEquals(3, all.size());
    }

    @Test
    void testGetByPerformer() {
        auditLog.log("USER_CREATE", "admin", "user1", "Details");
        auditLog.log("ROLE_CREATE", "manager", "Role1", "Details");
        auditLog.log("USER_DELETE", "admin", "user2", "Details");

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());

        List<AuditEntry> managerEntries = auditLog.getByPerformer("manager");
        assertEquals(1, managerEntries.size());
    }

    @Test
    void testGetByPerformerCaseInsensitive() {
        auditLog.log("USER_CREATE", "Admin", "user1", "Details");
        List<AuditEntry> entries = auditLog.getByPerformer("ADMIN");
        assertEquals(1, entries.size());
    }

    @Test
    void testGetByAction() {
        auditLog.log("USER_CREATE", "admin", "user1", "Details");
        auditLog.log("USER_CREATE", "admin", "user2", "Details");
        auditLog.log("ROLE_CREATE", "admin", "Role1", "Details");

        List<AuditEntry> userCreateEntries = auditLog.getByAction("USER_CREATE");
        assertEquals(2, userCreateEntries.size());
    }

    @Test
    void testGetByActionCaseInsensitive() {
        auditLog.log("USER_CREATE", "admin", "user1", "Details");
        List<AuditEntry> entries = auditLog.getByAction("user_create");
        assertEquals(1, entries.size());
    }

    @Test
    void testLogEmptyAction() {
        assertThrows(IllegalArgumentException.class, () ->
                auditLog.log("", "admin", "target", "details"));
    }

    @Test
    void testLogEmptyPerformer() {
        assertThrows(IllegalArgumentException.class, () ->
                auditLog.log("ACTION", "", "target", "details"));
    }

    @Test
    void testLogNullAction() {
        assertThrows(IllegalArgumentException.class, () ->
                auditLog.log(null, "admin", "target", "details"));
    }

    @Test
    void testClear() {
        auditLog.log("USER_CREATE", "admin", "user1", "Details");
        auditLog.log("ROLE_CREATE", "admin", "Role1", "Details");
        assertEquals(2, auditLog.count());

        auditLog.clear();
        assertEquals(0, auditLog.count());
    }

    @Test
    void testSaveToFile() throws IOException {
        String testFile = "test_audit.log";
        auditLog.log("USER_CREATE", "admin", "user1", "Test details");
        auditLog.saveToFile(testFile);

        assertTrue(Files.exists(Paths.get(testFile)));
        List<String> lines = Files.readAllLines(Paths.get(testFile));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("USER_CREATE"));
        assertTrue(lines.get(0).contains("admin"));

        // Очистка
        Files.deleteIfExists(Paths.get(testFile));
    }

    @Test
    void testSaveToFileEmptyFilename() {
        assertThrows(IllegalArgumentException.class, () ->
                auditLog.saveToFile(""));
    }

    @Test
    void testLoadFromFile() throws IOException {
        String testFile = "test_audit_load.log";
        auditLog.log("USER_CREATE", "admin", "user1", "Test details");
        auditLog.saveToFile(testFile);

        AuditLog newLog = new AuditLog();
        newLog.loadFromFile(testFile);
        assertEquals(1, newLog.count());

        // Очистка
        Files.deleteIfExists(Paths.get(testFile));
    }

    @Test
    void testLoadFromFileNotFound() {
        AuditLog newLog = new AuditLog();
        assertDoesNotThrow(() -> newLog.loadFromFile("nonexistent_file.log"));
    }

    @Test
    void testAuditEntryFormat() {
        AuditEntry entry = AuditEntry.now("TEST_ACTION", "test_user", "target", "details");
        String formatted = entry.format();
        assertTrue(formatted.contains("TEST_ACTION"));
        assertTrue(formatted.contains("test_user"));
        assertTrue(formatted.contains("target"));
        assertTrue(formatted.contains("details"));
    }

    @Test
    void testAuditEntryTimestampNotNull() {
        AuditEntry entry = AuditEntry.now("ACTION", "performer", "target", "details");
        assertNotNull(entry.timestamp());
        assertFalse(entry.timestamp().isEmpty());
    }
}