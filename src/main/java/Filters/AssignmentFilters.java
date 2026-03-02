package Filters;

import org.example.AssignmentMetadata;
import org.example.RecUser;
import org.example.Role;
import org.example.TemporaryAssignment;

import java.time.LocalDateTime;

public class AssignmentFilters {

    // 1. Фильтр по конкретному пользователю
    public static AssignmentFilter byUser(RecUser user) {
        return assignment -> assignment.user().equals(user);
    }

    // 2. Фильтр по имени пользователя
    public static AssignmentFilter byUsername(String username) {
        return assignment -> assignment.user().username().equals(username);
    }

    // 3. Фильтр по конкретной роли
    public static AssignmentFilter byRole(Role role) {
        return assignment -> assignment.role().equals(role);
    }

    // 4. Фильтр по имени роли
    public static AssignmentFilter byRoleName(String roleName) {
        return assignment -> assignment.role().getName().equals(roleName);
    }

    // 5. Фильтр только по активным назначениям
    public static AssignmentFilter activeOnly() {
        return assignment -> assignment.isActive();
    }

    // 6. Фильтр только по неактивным назначениям
    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    // 7. Фильтр по типу назначения ("PERMANENT" или "TEMPORARY")
    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equals(type);
    }

    // 8. Фильтр по пользователю, который назначил роль
    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    // 9. Фильтр по дате назначения (назначенные после указанной даты)
    public static AssignmentFilter assignedAfter(String date) {
        return assignment -> {
            String assignedAt = assignment.metadata().assignedAt();
            return isDateAfter(assignedAt, date);
        };
    }

    // 10. Фильтр по дате истечения для временных назначений
    public static AssignmentFilter expiringBefore(String date) {
        return assignment -> {
            // Проверяем, что назначение временное
            if (!assignment.assignmentType().equals("TEMPORARY")) {
                return false;
            }
            TemporaryAssignment tempAssignment = (TemporaryAssignment) assignment;
            String expiresAt = tempAssignment.getExpiresAt();
            return isDateBefore(expiresAt, date);
        };
    }

    // Вспомогательные методы для сравнения дат
    private static boolean isDateAfter(String date1, String date2) {
        return LocalDateTime.parse(date1, AssignmentMetadata.FORMATTER)
                .isAfter(LocalDateTime.parse(date2, AssignmentMetadata.FORMATTER));
    }

    private static boolean isDateBefore(String date1, String date2) {
        return LocalDateTime.parse(date1, AssignmentMetadata.FORMATTER)
                .isBefore(LocalDateTime.parse(date2, AssignmentMetadata.FORMATTER));
    }
}
