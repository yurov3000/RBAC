package org.example;

import java.util.List;

class Main {
    static void main(String[] args) {
        List<String[]> testCases = List.of(
                new String[]{"john_doe", "John Doe", "johndoe@example.com"},
                new String[]{"invalid@username", "Test User", "test@example.com"},
                new String[]{"usr", "Short Username", "short@user.com"},
                new String[]{"very_long_username_with_more_than_twenty_characters", "Long Username", "long@example.com"},
                new String[]{"valid_user", "", "valid@email.com"},
                new String[]{"valid_user", "Valid Fullname", ""},
                new String[]{"valid_user", "Valid Fullname", "not_an_email"}
        );

        for (var caseData : testCases){
            try{
                var user = RecUser.validate(caseData[0], caseData[1], caseData[2]);
                System.out.println(user.format());
            } catch (IllegalArgumentException e){
                System.err.println(e.getMessage());
            }
        }

        // Примеры правильного создания объектов
        RecPermission readUsers = new RecPermission("read", "Users", "Позволяет читать пользователей");
        RecPermission writeReports = new RecPermission("write", "REPORTS", "Позволяет создавать отчёты");
        RecPermission writeUsers = new RecPermission("write", "Users", "Позволяет создавать и редактировать пользователей");

        // Вывод в нужном формате
        System.out.println(readUsers.format());
        System.out.println(writeReports.format());

        // Проверка совпадения по шаблону
        System.out.println(readUsers.matches("READ", "US")); // true
        System.out.println(writeReports.matches("WRI", "REP")); // true

        // Некорректные варианты создания (будут исключения):
        try {
            new RecPermission("", "Resource", "Description"); // Ошибка: имя права пустое
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        try {
            new RecPermission("Read Write", "Resource", "Description"); // Ошибка: имя права содержит пробел
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        System.out.println(" ");
        Role admin = new Role("Administrator", "Full system access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);

        System.out.println(admin.format());
    }
}
