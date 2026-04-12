package Managers;

import org.example.*;

import Filters.AssignmentFilter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    // Основное хранилище назначений (ключ — assignmentId)
    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();

    // Для отслеживания активных назначений одной роли пользователю (ключ: username_roleId)
    private final Set<String> activeAssignmentsKeys = ConcurrentHashMap.newKeySet();

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение не может быть null");
        }

        RecUser user = assignment.user();
        Role role = assignment.role();

        if (user == null) throw new IllegalArgumentException("Пользователь не может быть null");
        if (role == null) throw new IllegalArgumentException("Роль не может быть null");

        String key = user.username() + "_" + role.getId();

        // 🔒 Атомарная проверка и добавление
        synchronized (this) {
            if (activeAssignmentsKeys.contains(key) && assignment.isActive()) {
                throw new IllegalArgumentException(
                        "Пользователь '" + user.username() + "' уже имеет активное назначение роли '" +
                                role.getName() + "'");
            }
            assignments.put(assignment.assignmentId(), assignment);
            if (assignment.isActive()) {
                activeAssignmentsKeys.add(key);
            }
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;

        String assignmentId = assignment.assignmentId();
        boolean removed = assignments.remove(assignmentId) != null;

        if (removed && assignment.isActive()) {
            String key = assignment.user().username() + "_" + assignment.role().getId();
            activeAssignmentsKeys.remove(key);
        }
        return removed;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
        activeAssignmentsKeys.clear();
    }

    public List<RoleAssignment> findByUser(RecUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        return assignments.values().stream()
                .filter(assignment -> user.username().equals(assignment.user().username()))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }

        return assignments.values().stream()
                .filter(assignment -> role.getId().equals(assignment.role().getId()))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }

        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        if (filter == null) {
            throw new IllegalArgumentException("Фильтр не может быть null");
        }
        if (sorter == null) {
            throw new IllegalArgumentException("Компаратор не может быть null");
        }

        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(assignment -> assignment instanceof TemporaryAssignment)
                .filter(assignment -> ((TemporaryAssignment) assignment).isExpired())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(RecUser user, Role role) {
        if (user == null || role == null) {
            return false;
        }

        return findByUser(user).stream()
                .anyMatch(assignment ->
                        role.getId().equals(assignment.role().getId()) &&
                                assignment.isActive()
                );
    }

    public boolean userHasPermission(RecUser user, String permissionName, String resource) {
        if (user == null || permissionName == null || resource == null) {
            return false;
        }

        Set<RecPermission> userPermissions = getUserPermissions(user);
        return userPermissions.stream()
                .anyMatch(permission ->
                        permissionName.equals(permission.name()) &&
                                resource.equals(permission.resource())
                );
    }


    public Set<RecPermission> getUserPermissions(RecUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        return findByUser(user).stream()
                .filter(RoleAssignment::isActive)
                .map(RoleAssignment::role)
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID назначения не может быть пустым");
        }

        Optional<RoleAssignment> assignmentOpt = findById(assignmentId);
        if (!assignmentOpt.isPresent()) {
            throw new NoSuchElementException("Назначение с ID '" + assignmentId + "' не найдено");
        }

        RoleAssignment assignment = assignmentOpt.get();
        if (!(assignment instanceof PermanentAssignment)) {
            throw new IllegalStateException("Можно отозвать только постоянные назначения");
        }

        ((PermanentAssignment) assignment).revoke();
        String key = assignment.user().username() + "_" + assignment.role().getId();
        activeAssignmentsKeys.remove(key);
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID назначения не может быть пустым");
        }
        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Новая дата окончания не может быть пустой");
        }

        Optional<RoleAssignment> assignmentOpt = findById(assignmentId);
        if (!assignmentOpt.isPresent()) {
            throw new NoSuchElementException("Назначение с ID '" + assignmentId + "' не найдено");
        }

        RoleAssignment assignment = assignmentOpt.get();
        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalStateException("Можно продлить только временные назначения");
        }

        ((TemporaryAssignment) assignment).extend(newExpirationDate);
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        if (filter == null) throw new IllegalArgumentException("Filter is null");
        return assignments.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }
}

