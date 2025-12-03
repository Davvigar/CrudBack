package org.david.crm.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.david.crm.concurrent.AsyncReportService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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
        System.out.println("[ReportServlet] Iniciando generación de informe de clientes...");
        
        if (reportService == null) {
            System.err.println("[ReportServlet] ERROR: reportService es null!");
            sendErrorResponse(resp, "Error: Servicio de informes no disponible", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        try {
            Future<String> future = reportService.generarInformeClientes();
            System.out.println("[ReportServlet] Tarea de informe de clientes enviada al executor");
            
            // Enviar respuesta inmediata (el informe se genera en segundo plano)
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\": \"Generando informe de clientes en segundo plano...\", " +
                "\"status\": \"processing\"}");
        } catch (Exception e) {
            System.err.println("[ReportServlet] Error al iniciar informe de clientes: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(resp, "Error al iniciar informe: " + e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void generarInformeFacturas(HttpServletResponse resp) throws IOException {
        System.out.println("[ReportServlet] Iniciando generación de informe de facturas...");
        
        if (reportService == null) {
            System.err.println("[ReportServlet] ERROR: reportService es null!");
            sendErrorResponse(resp, "Error: Servicio de informes no disponible", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        CompletableFuture<String> future = reportService.generarInformeFacturas();
        System.out.println("[ReportServlet] Tarea de informe de facturas enviada al executor");
        
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

            }
            return null;
        });
        

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Generando informe de facturas en segundo plano...\", " +
            "\"status\": \"processing\"}");
    }
    
    private void generarInformeCompleto(HttpServletResponse resp) throws IOException {
        System.out.println("[ReportServlet] Iniciando generación de informe completo...");
        
        if (reportService == null) {
            System.err.println("[ReportServlet] ERROR: reportService es null!");
            sendErrorResponse(resp, "Error: Servicio de informes no disponible", 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        CompletableFuture<String> future = reportService.generarInformeCompleto();
        System.out.println("[ReportServlet] Tarea de informe completo enviada al executor");

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Generando informe completo en segundo plano...\", " +
            "\"status\": \"processing\"}");
    }
}

