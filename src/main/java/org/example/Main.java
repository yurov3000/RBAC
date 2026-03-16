package org.example;

import Commands.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        // Инициализация системы
//        RBACSystem system = new RBACSystem();
//        system.initialize();
//        system.setCurrentUser("admin");
//
//        // Инициализация парсера команд
//        CommandParser parser = new CommandParser();
//        CommandRegistry.registerAllCommands(parser, system);
//
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("==========================================");
//        System.out.println("|     RBAC Management System v1.0        |");
//        System.out.println("|     Введите 'help' для списка команд   |");
//        System.out.println("==========================================");
//
//        while (true) {
//            System.out.print("\nrbac> ");
//            String input = scanner.nextLine().trim();
//            if (input.isEmpty()) {
//                continue;
//            }
//            if ("exit".equalsIgnoreCase(input)) {
//                System.out.print("Вы уверены? (да/нет): ");
//                String confirm = scanner.nextLine().trim();
//                if ("да".equalsIgnoreCase(confirm)) {
//                    System.out.println("Выход из системы...");
//                    break;
//                } else {
//                    continue;
//                }
//            }
//            parser.parseAndExecute(input, scanner, system);
//        }
//
//        scanner.close();
//        System.out.println("До свидания!");
    }
}
