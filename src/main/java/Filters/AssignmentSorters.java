package Filters;

import org.example.AssignmentMetadata;
import org.example.RoleAssignment;

import java.time.LocalDateTime;
import java.util.Comparator;

public class AssignmentSorters {

    // Сортировка по имени пользователя (username)
    public static Comparator<RoleAssignment> byUsername() {
        return (assignment1, assignment2) ->
                assignment1.user().username().compareTo(assignment2.user().username());
    }

    // Сортировка по имени роли (role name)
    public static Comparator<RoleAssignment> byRoleName() {
        return (assignment1, assignment2) ->
                assignment1.role().getName().compareTo(assignment2.role().getName());
    }

    // Сортировка по дате назначения (assignedAt)
    public static Comparator<RoleAssignment> byAssignmentDate() {
        return (assignment1, assignment2) -> {
            LocalDateTime date1 = LocalDateTime.parse(
                    assignment1.metadata().assignedAt(),
                    AssignmentMetadata.FORMATTER
            );
            LocalDateTime date2 = LocalDateTime.parse(
                    assignment2.metadata().assignedAt(),
                    AssignmentMetadata.FORMATTER
            );
            return date1.compareTo(date2);
        };
    }
}
