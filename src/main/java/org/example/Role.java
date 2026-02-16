package org.example;

import java.security.Permission;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {
    private static final Set<String> usedNames = new HashSet<>();
    private static long counter = 1;

    private String id;
    private String name;
    private String description;
    private Set<RecPermission> permissions = new HashSet<>();

    public Role(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название роли не может быть пустым.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание роли не может быть пустым.");
        }
        if (usedNames.contains(name)) {
            throw new IllegalArgumentException("Роль с названием '" + name + "' уже существует.");
        }

        this.id = "role_" + counter++; // или: UUID.randomUUID().toString()
        this.name = name;
        this.description = description;
        usedNames.add(name);
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Методы управления правами
    public void addPermission(RecPermission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Право не может быть null.");
        }
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Право не может быть null.");
        }
        permissions.remove(permission);
    }

    public boolean hasPermission(RecPermission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            return false;
        }
        for (RecPermission p : permissions) {
            if (p.matches(permissionName, resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<RecPermission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    // equals и hashCode по id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Role{id='" + id + "', name='" + name + "', description='" + description + "', permissions=" + permissions.size() + "}";
    }

    // Форматированный вывод
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");
        for (RecPermission p : permissions) {
            sb.append("- ").append(p.format()).append("\n");
        }
        return sb.toString().trim(); // убираем последний \n
    }

}
