package org.david.crm.concurrent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class AsyncReportService { // genera informes asincronos con executor service y scheduled executor service sin bloquear el hilo principal
    
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
        // Programar limpieza periódica de informes antiguos usando ScheduledExecutorService
        scheduler.scheduleAtFixedRate(() -> {
            limpiarInformesAntiguos();
        }, 0, 24, TimeUnit.HOURS);
    }
    
    // Elimina informes con más de 7 días de antigüedad.

    private void limpiarInformesAntiguos() {
        try {
            Path directorioActual = Paths.get(".");
            long tiempoLimite = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000); // 7 días
            
            int archivosEliminados = 0;
            
            File[] archivos = directorioActual.toFile().listFiles((dir, name) -> 
                name.startsWith("informe_") && name.endsWith(".txt")
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
            
            System.out.println("[LIMPIEZA PERIÓDICA] Eliminados " + archivosEliminados + " informes antiguos.");
        } catch (Exception e) {
            System.err.println("Error al limpiar informes antiguos: " + e.getMessage());
        }
    }

   
    public Future<String> generarInformeClientes() {
        return executorService.submit(() -> {
            try {
                Thread.sleep(2000); // Simula mucho tiempo de procesamiento
                List<Cliente> clientes = clienteRepository.findAll();
                
                String filename = "informe_clientes_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                
                try (FileWriter writer = new FileWriter(filename)) {
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
                
                return "Informe generado: " + filename;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error: Informe interrumpido";
            } catch (IOException e) {
                return "Error al generar informe: " + e.getMessage();
            }
        });
    }
    

    // CompletableFuture.supplyAsync()
    public CompletableFuture<String> generarInformeFacturas() { // va a corres en paralelo tres tareas diferentes y espera a que todas se completen
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
                List<Factura> facturas = facturaRepository.findAll();
                
                double totalFacturado = facturas.stream()
                    .mapToDouble(f -> f.getTotal() != null ? f.getTotal().doubleValue() : 0.0)
                    .sum();
                
                String filename = "informe_facturas_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                
                try (FileWriter writer = new FileWriter(filename)) {
                    writer.write("=== INFORME DE FACTURAS ===\n");
                    writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                    writer.write("Total de facturas: " + facturas.size() + "\n");
                    writer.write("Total facturado: " + totalFacturado + " €\n\n");
                }
                
                return "Informe de facturas generado: " + filename;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error: Informe interrumpido";
            } catch (IOException e) {
                return "Error al generar informe: " + e.getMessage();
            }
        }, executorService);
    }
    

    public CompletableFuture<String> generarInformeCompleto() {
        CompletableFuture<String> informeClientes = CompletableFuture.supplyAsync(() -> { // informe de clientes con complatable future
            try {
                Thread.sleep(1000);
                return "Clientes procesados: " + clienteRepository.findAll().size();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }, executorService);
        
        CompletableFuture<String> informeComerciales = CompletableFuture.supplyAsync(() -> { // informe de comerciales con complatable future
            try {
                Thread.sleep(1000);
                return "Comerciales procesados: " + comercialRepository.findAll().size();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }, executorService);
        
        CompletableFuture<String> informeFacturas = CompletableFuture.supplyAsync(() -> { // ubfirne de factyras con complatable future
            try {
                Thread.sleep(1000);
                return "Facturas procesadas: " + facturaRepository.findAll().size();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }, executorService);
        
        return CompletableFuture.allOf(informeClientes, informeComerciales, informeFacturas) // espera a que todos los informes se completen
            .thenApply(v -> {
                try {
                    String filename = "informe_completo_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                    
                    try (FileWriter writer = new FileWriter(filename)) {
                        writer.write("=== INFORME COMPLETO ===\n");
                        writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                        writer.write(informeClientes.join() + "\n");
                        writer.write(informeComerciales.join() + "\n");
                        writer.write(informeFacturas.join() + "\n");
                    }
                    
                    return "Informe completo generado: " + filename;
                } catch (IOException e) {
                    return "Error al generar informe completo: " + e.getMessage();
                }
            });
    }


    public Thread generarInformeClientesConThreadDedicado() {
        String filename = "informe_clientes_thread_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        ClienteReportThread thread = new ClienteReportThread(clienteRepository, filename);
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

