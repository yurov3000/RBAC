package org.example;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private static AtomicLong counter = new AtomicLong(1);

    protected final String assignmentId;
    protected final RecUser user;
    protected final Role role;
    protected final AssignmentMetadata metadata;

    public AbstractRoleAssignment(RecUser user, Role role, AssignmentMetadata metadata) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Метаданные не могут быть null");
        }

        this.assignmentId = "assign_" + counter.getAndIncrement();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    // Геттеры (реализация интерфейса)
    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public RecUser user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    // Абстрактные методы — должны быть реализованы в подклассах
    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    // equals и hashCode по assignmentId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    // Метод summary()
    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        String reasonLine = (metadata.reason() != null && !metadata.reason().trim().isEmpty())
                ? "\nReason: " + metadata.reason()
                : "";

        return "[%s] %s assigned to %s by %s at %s%s\nStatus: %s"
                .formatted(
                        assignmentType(),
                        role.getName(),
                        user.username(),
                        metadata.assignedBy(),
                        metadata.assignedAt(),
                        reasonLine,
                        status
                );
    }
}