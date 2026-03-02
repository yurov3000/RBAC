package Filters;

import org.example.RecPermission;

public class RoleFilters {

    // 1. Фильтр по точному совпадению имени роли
    public static RoleFilter byName(String name) {
        return role -> name.equals(role.getName());
    }

    // 2. Фильтр по подстроке в имени роли (игнорируя регистр)
    public static RoleFilter byNameContains(String substring) {
        String lowerSubstring = substring.toLowerCase();
        return role -> role.getName().toLowerCase().contains(lowerSubstring);
    }

    // 3. Фильтр по наличию конкретного права (RecPermission)
    public static RoleFilter hasPermission(RecPermission permission) {
        return role -> role.hasPermission(permission);
    }

    // 4. Фильтр по наличию права с указанным именем и ресурсом
    public static RoleFilter hasPermission(String permissionName, String resource) {
        return role -> role.hasPermission(permissionName, resource);
    }

    // 5. Фильтр по минимальному количеству прав у роли
    public static RoleFilter hasAtLeastNPermissions(int n) {
        return role -> role.getPermissions().size() >= n;
    }
}
