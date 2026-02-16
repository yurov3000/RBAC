package org.example;

public record RecPermission (String name, String resource, String description){

    public RecPermission(String name, String resource, String description) {
        this.name = name.toUpperCase();
        this.resource = resource.toLowerCase();
        this.description = description;

        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя права не может быть пустым.");
        }
        if (this.resource == null || this.resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым.");
        }
        if (this.description == null || this.description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым.");
        }
        if (this.name.contains(" ")) {
            throw new IllegalArgumentException("Имя права не должно содержать пробелов.");
        }
    }

    // Форматированный вывод
    public String format() {
        return "%s on %s: %s".formatted(name, resource, description);
    }

    // Поиск по паттернам
    public boolean matches(String namePattern, String resourcePattern) {
        return name.contains(namePattern.toUpperCase()) && resource.contains(resourcePattern.toLowerCase());
    }
}
