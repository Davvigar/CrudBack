package org.david.crm.concurrent.stats;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ApiStatistics { // clase que encapsula las estadisticas de la api y las expota a un archivo
    
    private Path getReportsDirectory() {
        try {
            // Intentar crear en el directorio del proyecto (C:\Users\david\Desktop\CrudProject\informes)
            Path projectReports = Paths.get(System.getProperty("user.home"), "Desktop", "CrudProject", "informes");
            
            // Si no existe, crearlo
            if (!Files.exists(projectReports)) {
                Files.createDirectories(projectReports);
                System.out.println("[ApiStatistics] ✓ Carpeta de informes creada en: " + projectReports.toAbsolutePath());
            } else {
                System.out.println("[ApiStatistics] 📁 Carpeta de informes ya existe en: " + projectReports.toAbsolutePath());
            }
            
            return projectReports;
        } catch (IOException e) {
            System.err.println("[ApiStatistics] ✗ Error al crear directorio de informes: " + e.getMessage());
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "informes");
            System.out.println("[ApiStatistics] ⚠ Usando directorio temporal: " + tempDir.toAbsolutePath());
            return tempDir;
        }
    }
    
    private String getReportPath(String filename) {
        Path reportsDir = getReportsDirectory();
        Path filePath = reportsDir.resolve(filename);
        System.out.println("[ApiStatistics] 📄 Archivo de estadísticas se guardará en: " + filePath.toAbsolutePath());
        return filePath.toString();
    }
    
    // contaadires atomicos 
    
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
   
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    
    private final AtomicInteger logsWritten = new AtomicInteger(0);
    
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    
    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }
    
   
    public void incrementSuccessfulRequests() {
        successfulRequests.incrementAndGet();
    }
   
    public void incrementFailedRequests() {
        failedRequests.incrementAndGet();
    }
    
   
    public void incrementLogsWritten() {
        logsWritten.incrementAndGet();
    }
    
    
    public void addResponseTime(long milliseconds) {
        totalResponseTime.addAndGet(milliseconds);
    }
    
    
    public int getTotalRequests() {
        return totalRequests.get();
    }
    
  
    public int getSuccessfulRequests() {
        return successfulRequests.get();
    }
    
  
    public int getFailedRequests() {
        return failedRequests.get();
    }
    
    
    public int getLogsWritten() {
        return logsWritten.get();
    }
    
   
    public double getAverageResponseTime() {
        int total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalResponseTime.get() / total;
    }
    
    
    public void reset() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        logsWritten.set(0);
        totalResponseTime.set(0);
    }
    
 // excportar estadisiticas a un archivo con hilo creado con lambda 
    public void exportSummaryAsync(String fileName) {
        Thread exportThread = new Thread(() -> {
            StatisticsSummary summary = getSummary();
            String filepath = getReportPath(fileName);
            try (FileWriter writer = new FileWriter(filepath)) {
                writer.write("=== Estadísticas API ===\n");
                writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                writer.write("Total: " + summary.getTotalRequests() + "\n");
                writer.write("OK: " + summary.getSuccessfulRequests() + "\n");
                writer.write("Errores: " + summary.getFailedRequests() + "\n");
                writer.write("Logs: " + summary.getLogsWritten() + "\n");
                writer.write("Promedio respuesta: " + summary.getAverageResponseTime() + " ms\n");
            } catch (IOException e) {
                System.err.println("No se pudo exportar las estadísticas: " + e.getMessage());
            }
        }, "StatsExportThread");
        exportThread.setDaemon(true); // no bloquea el apagado de la aplicacion y se detinen cuaando la JVM se cierra
        exportThread.start();
    }
    
    // encapsula las stats como un snapshot en un momnento dado
    public StatisticsSummary getSummary() {
        return new StatisticsSummary(
            totalRequests.get(),
            successfulRequests.get(),
            failedRequests.get(),
            logsWritten.get(),
            getAverageResponseTime()
        );
    }
    
    
    public static class StatisticsSummary {
        private final int totalRequests;
        private final int successfulRequests;
        private final int failedRequests;
        private final int logsWritten;
        private final double averageResponseTime;
        
        public StatisticsSummary(
        int totalRequests, int successfulRequests, 
         int failedRequests, int logsWritten, 
         double averageResponseTime) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.logsWritten = logsWritten;
            this.averageResponseTime = averageResponseTime;
        }
        
        // Getters
        public int getTotalRequests() { return totalRequests; }
        public int getSuccessfulRequests() { return successfulRequests; }
        public int getFailedRequests() { return failedRequests; }
        public int getLogsWritten() { return logsWritten; }
        public double getAverageResponseTime() { return averageResponseTime; }
    }
}

