package Commands;

import static org.junit.jupiter.api.Assertions.*;

import org.example.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Scanner;

import java.lang.reflect.Field;
import java.util.Set;

class CommandTest {
    private CommandParser parser;
    private RBACSystem system;
    private Scanner scanner;

    @BeforeEach
    void setUp() throws Exception {  // ← важно: throws Exception
        // ОЧИСТКА статического состояния Role
        Field field = Role.class.getDeclaredField("usedNames");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> usedNames = (Set<String>) field.get(null);
        usedNames.clear();
        // Сброс счётчика ID (опционально, но полезно)
        Field counterField = Role.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        counterField.setLong(null, 1);

        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        scanner = new Scanner(System.in);
        CommandRegistry.registerAllCommands(parser, system);
        }

    @Test
    void testRegisterCommand() {
        assertEquals(25, parser.getCommandCount(), "Должно быть зарегистрировано 25 команд");
    }

    @Test
    void testHasCommand() {
        assertTrue(parser.hasCommand("help"));
        assertTrue(parser.hasCommand("user-list"));
        assertTrue(parser.hasCommand("role-create"));
        assertTrue(parser.hasCommand("assign-role"));
        assertFalse(parser.hasCommand("nonexistent"));
    }

    @Test
    void testPrintHelp() {
        // Просто проверяем, что метод не выбрасывает исключений
        assertDoesNotThrow(() -> parser.printHelp());
    }

    @Test
    void testParseAndExecuteUnknownCommand() {
        // Проверяем, что неизвестная команда обрабатывается корректно
        assertDoesNotThrow(() -> parser.parseAndExecute("unknown-command", scanner, system));
    }

    @Test
    void testParseAndExecuteEmptyInput() {
        assertDoesNotThrow(() -> parser.parseAndExecute("", scanner, system));
        assertDoesNotThrow(() -> parser.parseAndExecute(null, scanner, system));
    }

    @Test
    void testRegisterCommandNullName() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.registerCommand(null, "desc", (s, sys) -> {}));
    }

    @Test
    void testRegisterCommandNullCommand() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.registerCommand("test", "desc", null));
    }

    @Test
    void testUserListCommand() {
        assertDoesNotThrow(() -> parser.executeCommand("user-list", scanner, system));
    }

    @Test
    void testRoleListCommand() {
        assertDoesNotThrow(() -> parser.executeCommand("role-list", scanner, system));
    }

    @Test
    void testStatsCommand() {
        assertDoesNotThrow(() -> parser.executeCommand("stats", scanner, system));
    }

    @Test
    void testAssignmentListCommand() {
        assertDoesNotThrow(() -> parser.executeCommand("assignment-list", scanner, system));
    }

    @Test
    void testHelpCommand() {
        assertDoesNotThrow(() -> parser.executeCommand("help", scanner, system));
    }

    @Test
    void testPermissionsUserCommand() {
        assertDoesNotThrow(() -> parser.executeCommand("permissions-user", scanner, system));
    }

    @Test
    void testCommandCaseInsensitive() {
        assertTrue(parser.hasCommand("HELP"));
        assertTrue(parser.hasCommand("User-List"));
        assertTrue(parser.hasCommand("ROLE-CREATE"));
    }
}