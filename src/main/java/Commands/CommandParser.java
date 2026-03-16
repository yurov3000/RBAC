package Commands;

import java.util.*;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    // Зарегистрировать команду
    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя команды не может быть пустым");
        }
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    // Выполнить команду по имени
    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.trim().isEmpty()) {
            System.out.println("Команда не может быть пустой");
            return;
        }
        String name = commandName.toLowerCase().trim();
        Command command = commands.get(name);
        if (command == null) {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка доступных команд");
            return;
        }
        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Ошибка выполнения команды: " + e.getMessage());
        }
    }

    // Вывести справку по всем командам
    public void printHelp() {
        System.out.println("\n=== Доступные команды ===");
        int maxLength = commandDescriptions.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("  %-"+maxLength+"s — %s%n", entry.getKey(), entry.getValue());
        }
        System.out.println();
    }

    // Распарсить ввод и выполнить команду
    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        executeCommand(commandName, scanner, system);
    }

    // Получить количество зарегистрированных команд
    public int getCommandCount() {
        return commands.size();
    }

    //Проверить, существует ли команда
    public boolean hasCommand(String name) {
        return commands.containsKey(name.toLowerCase());
    }
}