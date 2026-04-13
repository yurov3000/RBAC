package org.example;

import Commands.CommandParser;
import Commands.CommandRegistry;
import Commands.RBACSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class CommandInputTest {
    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() throws Exception {
        Field usedNamesField = Role.class.getDeclaredField("usedNames");
        usedNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> usedNames = (Set<String>) usedNamesField.get(null);
        usedNames.clear();

        Field counterField = Role.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        AtomicLong counter = (AtomicLong) counterField.get(null);
        counter.set(1L);

        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        CommandRegistry.registerAllCommands(parser, system);
    }

    @Test
    void testUserViewWithInput() {
        String input = "admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        assertDoesNotThrow(() -> parser.executeCommand("user-view", scanner, system));
    }

    @Test
    void testRoleViewWithInput() {
        String input = "Admin\n";  // ← Внимание: роль называется "Admin", не "Administrator"
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        assertDoesNotThrow(() -> parser.executeCommand("role-view", scanner, system));
    }

    @Test
    void testAssignmentListUserWithInput() {
        String input = "admin\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        assertDoesNotThrow(() -> parser.executeCommand("assignment-list-user", scanner, system));
    }

    @Test
    void testPermissionsCheckWithInput() {
        String input = "admin\nREAD\nusers\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        assertDoesNotThrow(() -> parser.executeCommand("permissions-check", scanner, system));
    }
}