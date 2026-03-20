package org.example;

public record RecPermission(String name, String resource, String description) {

    public RecPermission(String name, String resource, String description) {
        // Валидация через ValidationUtils
        ValidationUtils.requireNonEmpty(name, "Имя права");
        ValidationUtils.requireNonEmpty(resource, "Ресурс");
        ValidationUtils.requireNonEmpty(description, "Описание");

        // Проверка: имя права не должно содержать пробелов
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Имя права не должно содержать пробелов");
        }

        // Нормализация и сохранение
        this.name = ValidationUtils.normalizeStringUpper(name);
        this.resource = ValidationUtils.normalizeStringLower(resource);
        this.description = ValidationUtils.normalizeString(description);
    }

    // Форматированный вывод
    public String format() {
        return "%s on %s: %s".formatted(name, resource, description);
    }

    // Поиск по паттернам (регистронезависимый)
    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null || resourcePattern == null) {
            return false;
        }
        return name.contains(ValidationUtils.normalizeStringUpper(namePattern)) &&
                resource.contains(ValidationUtils.normalizeStringLower(resourcePattern));
    }
}
