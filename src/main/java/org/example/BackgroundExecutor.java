package org.example;

import java.util.concurrent.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BackgroundExecutor {
    private static final int CORE_POOL_SIZE = 4;
    private static final ExecutorService executor = Executors.newFixedThreadPool(CORE_POOL_SIZE);

    // Очередь для логов
    private static final Queue<LogEntry> logQueue = new ConcurrentLinkedQueue<>();
    private static final Thread logWriterThread;
    private static volatile boolean running = true;

    static {
        // Поток-обработчик логов
        logWriterThread = new Thread(() -> {
            while (running || !logQueue.isEmpty()) {
                LogEntry entry = logQueue.poll();
                if (entry != null) {
                    // Здесь реальная запись в файл или консоль (имитация задержки)
                    System.out.println("[LOG WRITER] " + entry.timestamp() + " - " + entry.action());
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                } else {
                    try { Thread.sleep(50); } catch (InterruptedException e) {}
                }
            }
        }, "AuditLog-Writer");
        logWriterThread.setDaemon(true);
        logWriterThread.start();
    }

    public static void execute(Runnable task) {
        executor.submit(task);
    }

    public static void submitLog(LogEntry entry) {
        logQueue.offer(entry);
    }

    public static void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    // Внутренний record для очереди (если AuditEntry не подходит напрямую)
    public record LogEntry(String timestamp, String action, String details) {}
}