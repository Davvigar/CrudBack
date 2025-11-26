package org.david.crm.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.david.crm.concurrent.AsyncReportService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Servlet para generar informes de forma asíncrona
 * Demuestra el uso de ExecutorService y CompletableFuture
 */
@WebServlet("/api/informes/*")
@ApplicationScoped
public class ReportServlet extends BaseServlet {
    
    @Inject
    private AsyncReportService reportService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "Tipo de informe requerido: /clientes, /facturas, /completo", 
                HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String tipo = pathInfo.substring(1);
        
        try {
            switch (tipo) {
                case "clientes":
                    generarInformeClientes(resp);
                    break;
                case "facturas":
                    generarInformeFacturas(resp);
                    break;
                case "completo":
                    generarInformeCompleto(resp);
                    break;
                default:
                    sendErrorResponse(resp, "Tipo de informe no válido", 
                        HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al generar informe: " + e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void generarInformeClientes(HttpServletResponse resp) throws IOException {
        Future<String> future = reportService.generarInformeClientes();
        
        // Enviar respuesta inmediata (el informe se genera en segundo plano)
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Generando informe de clientes en segundo plano...\", " +
            "\"status\": \"processing\"}");
    }
    
    private void generarInformeFacturas(HttpServletResponse resp) throws IOException {
        CompletableFuture<String> future = reportService.generarInformeFacturas();
        
        // Manejar el resultado de forma asíncrona
        future.thenAccept(result -> {
            try {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"message\": \"" + result + "\"}");
            } catch (IOException e) {
                // Error al escribir respuesta
            }
        }).exceptionally(ex -> {
            try {
                sendErrorResponse(resp, "Error: " + ex.getMessage(), 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e) {
                // Error al enviar error
            }
            return null;
        });
        
        // Respuesta inmediata
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Generando informe de facturas en segundo plano...\", " +
            "\"status\": \"processing\"}");
    }
    
    private void generarInformeCompleto(HttpServletResponse resp) throws IOException {
        CompletableFuture<String> future = reportService.generarInformeCompleto();
        
        // Respuesta inmediata
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Generando informe completo en segundo plano...\", " +
            "\"status\": \"processing\"}");
    }
}

