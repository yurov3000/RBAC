package org.example;

public record RecUser(String username, String fullname, String email){
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";

    public static RecUser validate(String username, String fullname, String email) {
        // Проверка username
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("Неверный формат имени пользователя. " +
                    "Допустимы 3-20 символов: буквы, цифры, подчёркивание");
        }

        // Проверка fullname
        ValidationUtils.requireNonEmpty(fullname, "Полное имя");

        // Проверка email
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        // Нормализация данных
        String normalizedUsername = ValidationUtils.normalizeString(username);
        String normalizedFullname = ValidationUtils.normalizeString(fullname);
        String normalizedEmail = ValidationUtils.normalizeStringLower(email);

        return new RecUser(normalizedUsername, normalizedFullname, normalizedEmail);
    }

    public String format(){
        return "%s (%s) <%s>".formatted(this.username,this.fullname,this.email);
    }

}
