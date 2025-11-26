package org.david.crm.concurrent.threads;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.david.crm.model.Cliente;
import org.david.crm.repository.ClienteRepository;

/**
 * Hilo dedicado (extends Thread) para generar informes de clientes.
 * Representa una funcionalidad distinta del resto de hilos del sistema.
 */
public class ClienteReportThread extends Thread {
    
    private final ClienteRepository clienteRepository;
    private final String outputFile;
    
    public ClienteReportThread(ClienteRepository clienteRepository, String outputFile) {
        super("ClienteReportThread-" + System.currentTimeMillis());
        this.clienteRepository = clienteRepository;
        this.outputFile = outputFile;
        setDaemon(true); // no bloquea el apagado de la aplicación
    }
    
    @Override
    public void run() {
        try {
            List<Cliente> clientes = clienteRepository.findAll();
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write("=== INFORME (Thread dedicado) ===\n");
                writer.write("Fecha: " + LocalDateTime.now() + "\n\n");
                writer.write("Total de clientes: " + clientes.size() + "\n\n");
                for (Cliente cliente : clientes) {
                    writer.write(
                        String.format("%d - %s (%s)\n",
                            cliente.getClienteId(),
                            cliente.getNombre(),
                            cliente.getEmail())
                    );
                }
            }
            System.out.println("Informe generado por hilo dedicado: " + outputFile);
        } catch (IOException e) {
            System.err.println("Error en ClienteReportThread: " + e.getMessage());
        }
    }
}

