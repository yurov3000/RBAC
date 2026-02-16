package org.example;

public interface RoleAssignment {
    String assignmentId();
    RecUser user();
    Role role();
    AssignmentMetadata metadata();
    boolean isActive();
    String assignmentType(); // "PERMANENT" или "TEMPORARY"

    // Метод summary()
    String summary();
}
