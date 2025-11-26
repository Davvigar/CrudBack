package org.david.crm.concurrent.threads;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Runnable específico para el servicio de logs (implements Runnable).
 * Se utiliza para ilustrar una funcionalidad distinta orientada a auditoría.
 */
public class AuditLogRunnable implements Runnable {
    
    private final String logFile;
    private final String message;
    
    public AuditLogRunnable(String logFile, String message) {
        this.logFile = logFile;
        this.message = message;
    }
    
    @Override
    public void run() {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            String formatted = String.format("[%s] [AUDIT] %s%n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                message);
            writer.write(formatted);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error escribiendo log de auditoría: " + e.getMessage());
        }
    }
}

