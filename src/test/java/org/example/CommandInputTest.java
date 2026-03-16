package org.example;

import Commands.CommandParser;
import Commands.CommandRegistry;
import Commands.RBACSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandInputTest {
    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() {
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
        String input = "Administrator\n";
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