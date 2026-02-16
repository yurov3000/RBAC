package org.example;

public record RecUser(String username, String fullname, String email){
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";

    public static RecUser validate(String username, String fullname, String email) throws IllegalArgumentException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не должно быть пустым");
        }
        if (!username.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException("Не подходит формат имени");
        }
        if (fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не должно быть пустым");
        }
        if (email == null || email.isBlank() || !email.contains("@") || !email.contains(".") || email.indexOf('@') > email.lastIndexOf('.')) {
            throw new IllegalArgumentException("Email введен неверным способом");
        }

        return new RecUser(username, fullname, email);
    }

    public String format(){
        return "%s (%s) <%s>".formatted(this.username,this.fullname,this.email);
    }
}
