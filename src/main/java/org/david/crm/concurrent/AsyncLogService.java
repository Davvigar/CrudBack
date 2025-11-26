package org.david.crm.concurrent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.david.crm.concurrent.stats.ApiStatistics;
import org.david.crm.concurrent.threads.AuditLogRunnable;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AsyncLogService {
    
    @Inject
    private ApiStatistics apiStatistics;
    
    private final ExecutorService logExecutor = Executors.newFixedThreadPool(3);
    private final String logFile = "application.log";
    
    // Contador atómico para el número de logs escritos
    private final AtomicInteger logCount = new AtomicInteger(0);
    
    // lambda
    public void logAsync(String message) {
        logExecutor.submit(() -> {
            writeLog(message);
            apiStatistics.incrementLogsWritten();
        });
    }
    
    // clase anonima
    public void logAsyncWithAnonymousClass(String message) {
        logExecutor.submit(new Runnable() {
            @Override
            public void run() {
                writeLog("[ANONIMO] " + message);
                apiStatistics.incrementLogsWritten();
            }
        });
    }
    
    // clase que implementa runnable

    public void logAsyncWithRunnable(String message) {
        logExecutor.submit(new LogWriterRunnable(message, this));
    }
    
    /**
     * Forma dedicada usando Thread + implements Runnable.
     * Crea un hilo separado para tareas de auditoría.
     */
    public void logWithRunnableThread(String message) {
        Thread thread = new Thread(
            new AuditLogRunnable(logFile, message),
            "AuditLogRunnableThread"
        );
        thread.setDaemon(true);
        thread.start();
    }
    
    // clase que extiende thread
    
    public void logAsyncWithThread(String message) {
        LogWriterThread thread = new LogWriterThread(message, this);
        thread.start();
    }
    
   
    synchronized void writeLog(String message) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            String logEntry = String.format("[%s] %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                message);
            writer.write(logEntry);
            writer.flush();
            logCount.incrementAndGet();
        } catch (IOException e) {
            System.err.println("Error al escribir log: " + e.getMessage());
        }
    }
    
   
    public int getLogCount() {
        return logCount.get();
    }
    
    
    public void shutdown() {
        logExecutor.shutdown();
    }
    
    
    private static class LogWriterRunnable implements Runnable {
        private final String message;
        private final AsyncLogService logService;
        
        public LogWriterRunnable(String message, AsyncLogService logService) {
            this.message = message;
            this.logService = logService;
        }
        
        @Override
        public void run() {
            logService.writeLog("[RUNNABLE] " + message);
        }
    }
    
   
    private static class LogWriterThread extends Thread {
        private final String message;
        private final AsyncLogService logService;
        
        public LogWriterThread(String message, AsyncLogService logService) {
            super("LogWriterThread-" + System.currentTimeMillis());
            this.message = message;
            this.logService = logService;
        }
        
        @Override
        public void run() {
            // Demuestra diferentes estados del hilo
            System.out.println("Estado del hilo antes de escribir: " + this.getState());
            logService.writeLog("[THREAD] " + message);
            System.out.println("Estado del hilo después de escribir: " + this.getState());
        }
    }
}

