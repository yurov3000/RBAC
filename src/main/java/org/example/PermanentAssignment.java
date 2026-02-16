package org.example;

public class PermanentAssignment extends AbstractRoleAssignment {
    private boolean revoked = false;

    public PermanentAssignment(RecUser user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }

    @Override
    public boolean isActive() {
        return !revoked; // активен, пока не отменён
    }

    @Override
    public String assignmentType() {
        return "PERMANENT";
    }

    // Метод для ручной отмены назначения
    public void revoke() {
        this.revoked = true;
    }

    // Геттер для проверки статуса отмены
    public boolean isRevoked() {
        return revoked;
    }
}