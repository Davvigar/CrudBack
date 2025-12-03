package org.david.crm.concurrent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.david.crm.concurrent.threads.ClienteReportThread;
import org.david.crm.model.Cliente;
import org.david.crm.model.Factura;
import org.david.crm.repository.ClienteRepository;
import org.david.crm.repository.ComercialRepository;
import org.david.crm.repository.FacturaRepository;
import org.david.crm.config.EntityManagerProducer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;


@ApplicationScoped
public class AsyncReportService {
    
    private static final String REPORTS_DIR = "informes";
    
    private Path getReportsDirectory() {
        try {
            // Intentar crear en el directorio del proyecto (C:\Users\david\Desktop\CrudProject\informes)
            Path projectReports = Paths.get(System.getProperty("user.home"), "Desktop", "CrudProject", "informes");
            
            // Si no existe, crearlo
            if (!Files.exists(projectReports)) {
                Files.createDirectories(projectReports);
                System.out.println("[AsyncReportService] ✓ Carpeta de informes creada en: " + projectReports.toAbsolutePath());
            } else {
                System.out.println("[AsyncReportService] 📁 Carpeta de informes ya existe en: " + projectReports.toAbsolutePath());
            }
            
            return projectReports;
        } catch (Exception e) {
            System.err.println("[AsyncReportService] ✗ Error al crear directorio de informes: " + e.getMessage());
            e.printStackTrace();
            // Fallback: usar directorio temporal
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "informes");
            System.out.println("[AsyncReportService] ⚠ Usando directorio temporal: " + tempDir.toAbsolutePath());
            return tempDir;
        }
    }
    
    private String getReportPath(String filename) {
        Path reportsDir = getReportsDirectory();
        Path filePath = reportsDir.resolve(filename);
        System.out.println("[AsyncReportService] Archivo de informe se guardará en: " + filePath.toAbsolutePath());
        return filePath.toString();
    } // genera informes asincronos con executor service y scheduled executor service sin bloquear el hilo principal
    
    @Inject
    private ClienteRepository clienteRepository;
    
    @Inject
    private ComercialRepository comercialRepository;
    
    @Inject
    private FacturaRepository facturaRepository;
    
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    // ScheduledExecutorService para tareas periódicas limpieza de informes antiguos
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public AsyncReportService() {
        System.out.println("[AsyncReportService] Constructor llamado - Inicializando servicio de informes...");
        // Programar limpieza periódica de informes antiguos usando ScheduledExecutorService
        scheduler.scheduleAtFixedRate(() -> {
            limpiarInformesAntiguos();
        }, 0, 24, TimeUnit.HOURS);
        System.out.println("[AsyncReportService] Servicio inicializado correctamente");
    }
    
    // Elimina informes con más de 7 días de antigüedad.

    private void limpiarInformesAntiguos() {
        try {
            Path directorioInformes = Paths.get(REPORTS_DIR);
            if (!Files.exists(directorioInformes)) {
                return; // No hay directorio de informes, no hay nada que limpiar
            }
            
            long tiempoLimite = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000); // 7 días
            
            int archivosEliminados = 0;
            
            File[] archivos = directorioInformes.toFile().listFiles((dir, name) -> 
                (name.startsWith("informe_") || name.startsWith("estadisticas_")) && name.endsWith(".txt")
            );
            
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.lastModified() < tiempoLimite) {
                        if (archivo.delete()) {
                            archivosEliminados++;
                        }
                    }
                }
            }
            
            if (archivosEliminados > 0) {
                System.out.println("[LIMPIEZA PERIÓDICA] Eliminados " + archivosEliminados + " informes antiguos.");
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar informes antiguos: " + e.getMessage());
        }
    }

   
    public Future<String> generarInformeClientes() {
        System.out.println("[AsyncReportService] generarInformeClientes() llamado");
        
        if (clienteRepository == null) {
            System.err.println("[AsyncReportService] ERROR: clienteRepository es null!");
            return executorService.submit(() -> "Error: Repositorio no disponible");
        }
        
        return executorService.submit(() -> {
            EntityManager em = null;
            try {
                System.out.println("[AsyncReportService] Iniciando generación de informe de clientes en thread...");
                
                // Crear EntityManager para este thread
                em = EntityManagerProducer.getEntityManagerFactory().createEntityManager();
                EntityManagerProducer.setEntityManager(em);
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                
                Thread.sleep(2000); // Simula mucho tiempo de procesamiento
                System.out.println("[AsyncReportService] Obteniendo clientes de la base de datos...");
                List<Cliente> clientes = clienteRepository.findAll();
                System.out.println("[AsyncReportService] Se obtuvieron " + clientes.size() + " clientes");
                
                tx.commit();
                
                String filename = "informe_clientes_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                String filepath = getReportPath(filename);
                System.out.println("[AsyncReportService] Ruta del informe: " + filepath);
                
                try (FileWriter writer = new FileWriter(filepath)) {
                    writer.write("=== INFORME DE CLIENTES ===\n");
                    writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                    writer.write("Total de clientes: " + clientes.size() + "\n\n");
                    
                    for (Cliente cliente : clientes) {
                        writer.write(String.format("ID: %d | Username: %s | Nombre: %s %s | Email: %s\n",
                            cliente.getClienteId(),
                            cliente.getUsername(),
                            cliente.getNombre(),
                            cliente.getApellidos(),
                            cliente.getEmail()));
                    }
                }
                
                System.out.println("[AsyncReportService] ✓ Informe de clientes generado exitosamente: " + filepath);
                return "Informe generado: " + filepath;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[AsyncReportService] ✗ Informe interrumpido: " + e.getMessage());
                e.printStackTrace();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error: Informe interrumpido";
            } catch (IOException e) {
                System.err.println("[AsyncReportService] ✗ Error de IO al generar informe: " + e.getMessage());
                e.printStackTrace();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error al generar informe: " + e.getMessage();
            } catch (Exception e) {
                System.err.println("[AsyncReportService] ✗ Error inesperado al generar informe: " + e.getMessage());
                e.printStackTrace();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error inesperado: " + e.getMessage();
            } finally {
                // Limpiar EntityManager del thread
                if (em != null) {
                    EntityManagerProducer.removeEntityManager();
                }
            }
        });
    }
    

    // CompletableFuture.supplyAsync()
    public CompletableFuture<String> generarInformeFacturas() { // va a corres en paralelo tres tareas diferentes y espera a que todas se completen
        System.out.println("[AsyncReportService] generarInformeFacturas() llamado");
        
        if (facturaRepository == null) {
            System.err.println("[AsyncReportService] ERROR: facturaRepository es null!");
            return CompletableFuture.completedFuture("Error: Repositorio no disponible");
        }
        
        return CompletableFuture.supplyAsync(() -> {
            EntityManager em = null;
            try {
                System.out.println("[AsyncReportService] Iniciando generación de informe de facturas en thread...");
                
                // Crear EntityManager para este thread
                em = EntityManagerProducer.getEntityManagerFactory().createEntityManager();
                EntityManagerProducer.setEntityManager(em);
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                
                Thread.sleep(1500);
                System.out.println("[AsyncReportService] Obteniendo facturas de la base de datos...");
                List<Factura> facturas = facturaRepository.findAll();
                System.out.println("[AsyncReportService] Se obtuvieron " + facturas.size() + " facturas");
                
                tx.commit();
                
                double totalFacturado = facturas.stream()
                    .mapToDouble(f -> f.getTotal() != null ? f.getTotal().doubleValue() : 0.0)
                    .sum();
                
                String filename = "informe_facturas_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                String filepath = getReportPath(filename);
                System.out.println("[AsyncReportService] Ruta del informe: " + filepath);
                
                try (FileWriter writer = new FileWriter(filepath)) {
                    writer.write("=== INFORME DE FACTURAS ===\n");
                    writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                    writer.write("Total de facturas: " + facturas.size() + "\n");
                    writer.write("Total facturado: " + totalFacturado + " €\n\n");
                }
                
                System.out.println("[AsyncReportService] ✓ Informe de facturas generado exitosamente: " + filepath);
                return "Informe de facturas generado: " + filepath;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[AsyncReportService] ✗ Informe interrumpido: " + e.getMessage());
                e.printStackTrace();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error: Informe interrumpido";
            } catch (IOException e) {
                System.err.println("[AsyncReportService] ✗ Error de IO al generar informe: " + e.getMessage());
                e.printStackTrace();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error al generar informe: " + e.getMessage();
            } catch (Exception e) {
                System.err.println("[AsyncReportService] ✗ Error inesperado al generar informe: " + e.getMessage());
                e.printStackTrace();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error inesperado: " + e.getMessage();
            } finally {
                // Limpiar EntityManager del thread
                if (em != null) {
                    EntityManagerProducer.removeEntityManager();
                }
            }
        }, executorService);
    }
    

    public CompletableFuture<String> generarInformeCompleto() {
        CompletableFuture<String> informeClientes = CompletableFuture.supplyAsync(() -> { // informe de clientes con complatable future
            EntityManager em = null;
            try {
                em = EntityManagerProducer.getEntityManagerFactory().createEntityManager();
                EntityManagerProducer.setEntityManager(em);
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                
                Thread.sleep(1000);
                int count = clienteRepository.findAll().size();
                tx.commit();
                return "Clientes procesados: " + count;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error";
            } catch (Exception e) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error: " + e.getMessage();
            } finally {
                if (em != null) {
                    EntityManagerProducer.removeEntityManager();
                }
            }
        }, executorService);
        
        CompletableFuture<String> informeComerciales = CompletableFuture.supplyAsync(() -> { // informe de comerciales con complatable future
            EntityManager em = null;
            try {
                em = EntityManagerProducer.getEntityManagerFactory().createEntityManager();
                EntityManagerProducer.setEntityManager(em);
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                
                Thread.sleep(1000);
                int count = comercialRepository.findAll().size();
                tx.commit();
                return "Comerciales procesados: " + count;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error";
            } catch (Exception e) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error: " + e.getMessage();
            } finally {
                if (em != null) {
                    EntityManagerProducer.removeEntityManager();
                }
            }
        }, executorService);
        
        CompletableFuture<String> informeFacturas = CompletableFuture.supplyAsync(() -> { // ubfirne de factyras con complatable future
            EntityManager em = null;
            try {
                em = EntityManagerProducer.getEntityManagerFactory().createEntityManager();
                EntityManagerProducer.setEntityManager(em);
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                
                Thread.sleep(1000);
                int count = facturaRepository.findAll().size();
                tx.commit();
                return "Facturas procesadas: " + count;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error";
            } catch (Exception e) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                return "Error: " + e.getMessage();
            } finally {
                if (em != null) {
                    EntityManagerProducer.removeEntityManager();
                }
            }
        }, executorService);
        
        return CompletableFuture.allOf(informeClientes, informeComerciales, informeFacturas) // espera a que todos los informes se completen
            .thenApply(v -> {
                try {
                    String filename = "informe_completo_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                    String filepath = getReportPath(filename);
                    
                    try (FileWriter writer = new FileWriter(filepath)) {
                        writer.write("=== INFORME COMPLETO ===\n");
                        writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                        writer.write(informeClientes.join() + "\n");
                        writer.write(informeComerciales.join() + "\n");
                        writer.write(informeFacturas.join() + "\n");
                    }
                    
                    System.out.println("[AsyncReportService] ✓ Informe completo generado exitosamente: " + filepath);
                    return "Informe completo generado: " + filepath;
                } catch (IOException e) {
                    System.err.println("[AsyncReportService] ✗ Error de IO al generar informe completo: " + e.getMessage());
                    e.printStackTrace();
                    return "Error al generar informe completo: " + e.getMessage();
                } catch (Exception e) {
                    System.err.println("[AsyncReportService] ✗ Error inesperado al generar informe completo: " + e.getMessage());
                    e.printStackTrace();
                    return "Error inesperado: " + e.getMessage();
                }
            });
    }


    public Thread generarInformeClientesConThreadDedicado() {
        String filename = "informe_clientes_thread_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        String filepath = getReportPath(filename);
        ClienteReportThread thread = new ClienteReportThread(clienteRepository, filepath);
        thread.start();
        return thread;
    }
    
 
    public Future<Integer> contarClientesAsync() {
        return executorService.submit(new Callable<Integer>() { // contar clientes asíncrono con callable y executor service
            @Override
            public Integer call() throws Exception {
                Thread.sleep(500);
                return clienteRepository.findAll().size();
            }
        });
    }
    
    public void shutdown() {
        executorService.shutdown();
        scheduler.shutdown();
    }
}

