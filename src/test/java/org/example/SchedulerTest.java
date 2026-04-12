package org.example;

import Commands.RBACSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    private RBACSystem system;

    @BeforeEach
    void setUp() throws Exception {
        // Очистка статического состояния Role
        Field usedNamesField = Role.class.getDeclaredField("usedNames");
        usedNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> usedNames = (Set<String>) usedNamesField.get(null);
        usedNames.clear();

        Field counterField = Role.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        ((AtomicLong) counterField.get(null)).set(1);

        system = new RBACSystem();
        system.initialize();
    }

    @AfterEach
    void tearDown() {
        try {
            if (system != null) {
                system.shutdown();
            }
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    @Test
    void testSchedulerExists() throws Exception {
        Field schedulerField = system.getClass().getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        Object scheduler = schedulerField.get(system);
        assertNotNull(scheduler, "Планировщик должен быть создан");
        assertTrue(scheduler instanceof ScheduledExecutorService);
    }

    @Test
    void testExpiredAssignmentsDetection() throws InterruptedException {
        RecUser user = RecUser.validate("test_user", "Test User", "test@example.com");
        system.getUserManager().add(user);

        Role role = new Role("TestRole", "Test description");
        system.getRoleManager().add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Load test");

        // Используем полный формат даты
        String pastDate = DateUtils.addDays(DateUtils.getCurrentDateTime(), -1);
        TemporaryAssignment expired = new TemporaryAssignment(user, role, meta, pastDate);
        system.getAssignmentManager().add(expired);

        // Ждём с повторными проверками
        int maxAttempts = 15;
        boolean found = false;

        for (int i = 0; i < maxAttempts && !found; i++) {
            Thread.sleep(1000);
            var expiredList = system.getAssignmentManager().getExpiredAssignments();
            found = expiredList.stream()
                    .anyMatch(ra -> ra.assignmentId().equals(expired.assignmentId()));
        }

        assertTrue(found, "Истёкшее назначение должно быть обнаружено планировщиком");
    }

    @Test
    void testActiveAssignmentNotMarkedExpired() throws InterruptedException {
        RecUser user = RecUser.validate("future_user", "Future User", "future@example.com");
        system.getUserManager().add(user);

        Role role = new Role("FutureRole", "Future description");
        system.getRoleManager().add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Future test");

        String futureDate = DateUtils.addDays(DateUtils.getCurrentDateTime(), 30);
        TemporaryAssignment active = new TemporaryAssignment(user, role, meta, futureDate);
        system.getAssignmentManager().add(active);

        // Ждём цикл планировщика
        Thread.sleep(12000);

        var expiredList = system.getAssignmentManager().getExpiredAssignments();
        boolean found = expiredList.stream()
                .anyMatch(ra -> ra.assignmentId().equals(active.assignmentId()));
        assertFalse(found, "Активное назначение не должно быть в списке истёкших");
    }

    @Test
    void testShutdownStopsScheduler() throws Exception {
        system.shutdown();

        Field schedulerField = system.getClass().getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) schedulerField.get(system);

        assertTrue(scheduler.isShutdown() || scheduler.isTerminated(),
                "Планировщик должен быть остановлен после shutdown()");
    }
}