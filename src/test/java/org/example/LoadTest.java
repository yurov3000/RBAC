package org.example;

import Managers.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class LoadTest {
    @Test
    void testConcurrencyStress() throws InterruptedException {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        int threadsCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        List<Runnable> tasks = new ArrayList<>();

        // Генерация задач
        for (int i = 0; i < threadsCount; i++) {
            final int threadId = i;
            tasks.add(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    try {
                        // Создание пользователя
                        String username = "user_t" + threadId + "_op" + j;
                        RecUser user = RecUser.validate(username, "Name", username + "@test.com");
                        userManager.add(user);

                        // Создание роли
                        Role role = new Role("role_t" + threadId + "_op" + j, "Desc");
                        roleManager.add(role);

                        // Назначение
                        AssignmentMetadata meta = AssignmentMetadata.now("system", "load test");
                        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
                        assignmentManager.add(assign);

                        // Поиск (чтение)
                        if (j % 10 == 0) {
                            assignmentManager.findByUser(user);
                        }
                    } catch (Exception e) {
                        // В идеале логируем ошибку, но для теста главное - отсутствие падения
                        e.printStackTrace();
                        fail("Exception in thread " + threadId + ": " + e.getMessage());
                    }
                }
            });
        }

        // Запуск
        long start = System.currentTimeMillis();
        for (Runnable task : tasks) {
            executor.submit(task);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS), "Тест не завершился вовремя");
        long end = System.currentTimeMillis();

        System.out.println("Нагрузочный тест пройден за " + (end - start) + " мс");
        assertEquals(threadsCount * operationsPerThread, userManager.count());
        assertEquals(threadsCount * operationsPerThread, roleManager.count());
        assertEquals(threadsCount * operationsPerThread, assignmentManager.count());
    }
}