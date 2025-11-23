package org.david.crm.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.david.crm.model.Factura;
import org.david.crm.service.FacturaService;
import org.david.crm.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/facturas/*")
@ApplicationScoped
public class FacturaServlet extends BaseServlet {
    
    @Inject
    private FacturaService facturaService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String clienteId = req.getParameter("clienteId");
        String comercialId = req.getParameter("comercialId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Factura> facturas;
            if (clienteId != null) {
                try {
                    Integer id = Integer.parseInt(clienteId);
                    facturas = facturaService.findByClienteId(id);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, "clienteId inválido", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else if (comercialId != null) {
                try {
                    Integer id = Integer.parseInt(comercialId);
                    facturas = facturaService.findByComercialId(id);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, "comercialId inválido", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else {
                facturas = facturaService.findAll();
            }
            sendJsonResponse(resp, facturas, HttpServletResponse.SC_OK);
        } else {
            String id = pathInfo.substring(1);
            Optional<Factura> facturaOpt = facturaService.findById(id);
            if (facturaOpt.isPresent()) {
                sendJsonResponse(resp, facturaOpt.get(), HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Factura no encontrada", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Factura factura = JsonUtil.fromJson(json, Factura.class);
            Factura saved = facturaService.save(factura);
            sendJsonResponse(resp, saved, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al crear factura: " + e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            String id = pathInfo.substring(1);
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Factura factura = JsonUtil.fromJson(json, Factura.class);
            
            Optional<Factura> updated = facturaService.update(id, factura);
            if (updated.isPresent()) {
                sendJsonResponse(resp, updated.get(), HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Factura no encontrada", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al actualizar factura: " + e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String id = pathInfo.substring(1);
        boolean deleted = facturaService.deleteById(id);
        if (deleted) {
            sendJsonResponse(resp, "{\"message\": \"Factura eliminada\"}", HttpServletResponse.SC_OK);
        } else {
            sendErrorResponse(resp, "Factura no encontrada", HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

