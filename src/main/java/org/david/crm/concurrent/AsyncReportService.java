package org.david.crm.concurrent;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

import org.david.crm.concurrent.threads.ClienteReportThread;
import org.david.crm.model.Cliente;
import org.david.crm.model.Factura;
import org.david.crm.repository.ClienteRepository;
import org.david.crm.repository.ComercialRepository;
import org.david.crm.repository.FacturaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class AsyncReportService {
    
    @Inject
    private ClienteRepository clienteRepository;
    
    @Inject
    private ComercialRepository comercialRepository;
    
    @Inject
    private FacturaRepository facturaRepository;
    
    // ExecutorService con pool de hilos fijo
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    // ScheduledExecutorService para tareas periódicas (limpieza de informes antiguos)
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public AsyncReportService() {
        // Programar limpieza periódica de informes antiguos usando ScheduledExecutorService
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("[LIMPIEZA PERIÓDICA] Limpiando informes antiguos...");
        
        }, 0, 24, TimeUnit.HOURS);
    }

   
    public Future<String> generarInformeClientes() {
        return executorService.submit(() -> {
            try {
                Thread.sleep(2000); // Simula procesamiento pesado
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
    public CompletableFuture<String> generarInformeFacturas() {
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
        CompletableFuture<String> informeClientes = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return "Clientes procesados: " + clienteRepository.findAll().size();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }, executorService);
        
        CompletableFuture<String> informeComerciales = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return "Comerciales procesados: " + comercialRepository.findAll().size();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }, executorService);
        
        CompletableFuture<String> informeFacturas = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return "Facturas procesadas: " + facturaRepository.findAll().size();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error";
            }
        }, executorService);
        
        return CompletableFuture.allOf(informeClientes, informeComerciales, informeFacturas)
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
        return executorService.submit(new Callable<Integer>() {
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

