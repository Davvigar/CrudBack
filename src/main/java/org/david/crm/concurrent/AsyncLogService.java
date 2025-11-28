package org.david.crm.concurrent;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.david.crm.concurrent.stats.ApiStatistics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class AsyncLogService {
    
    @Inject
    private ApiStatistics apiStatistics;
    
    private final ExecutorService logExecutor = Executors.newFixedThreadPool(3);
    private final String logFile = "application.log";
    
    
    private final AtomicInteger logCount = new AtomicInteger(0);
    
    
    public void logAsync(String message) {
        logExecutor.submit(() -> {
            writeLog(message);
            apiStatistics.incrementLogsWritten();
        });
    }
    
 
    public void logCriticoAsync(String message) {
        Thread criticalThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeLog("[CRITICO] " + message);
                System.out.println("Log crítico escrito: " + message);
            }
        }, "CriticalLogThread");
        criticalThread.setDaemon(false);
        criticalThread.start();
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
}

