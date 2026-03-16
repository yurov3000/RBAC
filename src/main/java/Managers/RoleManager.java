package Managers;

import Filters.RoleFilter;
import org.example.RecPermission;
import org.example.Role;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    // Основное хранилище ролей (ключ — id роли)
    private final Map<String, Role> rolesById = new HashMap<>();
    // Индекс для быстрого поиска по имени роли (ключ — имя роли)
    private final Map<String, Role> rolesByName = new HashMap<>();

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }

        String roleId = role.getId();
        String roleName = role.getName();

        // Проверка уникальности id и имени роли
        if (rolesById.containsKey(roleId)) {
            throw new IllegalArgumentException("Роль с id '" + roleId + "' уже существует");
        }
        if (rolesByName.containsKey(roleName)) {
            throw new IllegalArgumentException("Роль с именем '" + roleName + "' уже существует");
        }

        rolesById.put(roleId, role);
        rolesByName.put(roleName, role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;

        String roleId = role.getId();
        String roleName = role.getName();

        // Проверка, назначена ли роль пользователям
        if (isRoleAssignedToUsers(role)) {
            throw new IllegalStateException("Нельзя удалить роль '" + roleName + "', так как она назначена пользователям");
        }

        boolean removedById = rolesById.remove(roleId) != null;
        boolean removedByName = rolesByName.remove(roleName) != null;

        return removedById && removedByName;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    /**
     * Найти роль по имени
     */
    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя роли не может быть пустым");
        }
        return Optional.ofNullable(rolesByName.get(name));
    }

    /**
     * Найти роли по фильтру
     */
    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }

        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    /**
     * Найти все роли с фильтрацией и сортировкой
     */
    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }
        if (sorter == null) {
            throw new IllegalArgumentException("Компаратор не может быть null");
        }

        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    /**
     * Проверить существование роли по имени
     */
    public boolean exists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return rolesByName.containsKey(name);
    }

    /**
     * Добавить право доступа к роли
     */
    public void addPermissionToRole(String roleName, RecPermission permission) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя роли не может быть пустым");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Право доступа не может быть null");
        }

        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new NoSuchElementException("Роль с именем '" + roleName + "' не найдена");
        }

        role.addPermission(permission);
    }

    /**
     * Удалить право доступа из роли
     */
    public void removePermissionFromRole(String roleName, RecPermission permission) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя роли не может быть пустым");
        }
        if (permission == null) {
            throw new IllegalArgumentException("Право доступа не может быть null");
        }

        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new NoSuchElementException("Роль с именем '" + roleName + "' не найдена");
        }

        role.removePermission(permission);
    }

    /**
     * Найти роли, содержащие указанное право доступа
     */
    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя права не может быть пустым");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым");
        }

        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    //Проверка, назначена ли роль пользователям
    private boolean isRoleAssignedToUsers(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }
        return false;
    }
    public Role findByUserName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        // Если у вас есть метод, возвращающий Optional<Role>:
        return findByName(name).orElse(null);
    }
}
