package Filters;

import org.example.RoleAssignment;

@FunctionalInterface
interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    // Default методы для комбинирования фильтров
    default AssignmentFilter and(AssignmentFilter other) {
        return assignment -> this.test(assignment) && other.test(assignment);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        return assignment -> this.test(assignment) || other.test(assignment);
    }
}
