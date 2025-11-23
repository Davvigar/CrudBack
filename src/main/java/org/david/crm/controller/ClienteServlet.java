package org.david.crm.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.david.crm.model.Cliente;
import org.david.crm.service.ClienteService;
import org.david.crm.util.JsonUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/clientes/*")
@ApplicationScoped
public class ClienteServlet extends BaseServlet {
    
    @Inject
    private ClienteService clienteService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String comercialId = req.getParameter("comercialId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Cliente> clientes;
            if (comercialId != null) {
                try {
                    Integer id = Integer.parseInt(comercialId);
                    clientes = clienteService.findByComercialId(id);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, "comercialId inválido", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else {
                clientes = clienteService.findAll();
            }
            sendJsonResponse(resp, clientes, HttpServletResponse.SC_OK);
        } else {
            String idStr = pathInfo.substring(1);
            try {
                Integer id = Integer.parseInt(idStr);
                Optional<Cliente> clienteOpt = clienteService.findById(id);
                if (clienteOpt.isPresent()) {
                    sendJsonResponse(resp, clienteOpt.get(), HttpServletResponse.SC_OK);
                } else {
                    sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line); // concatena todas las lineas en un objeto cliente
            Cliente cliente = JsonUtil.fromJson(json, Cliente.class);
            Cliente saved = clienteService.save(cliente);
            sendJsonResponse(resp, saved, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al crear cliente: " + e.getMessage(), 
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
            String idStr = pathInfo.substring(1);
            Integer id = Integer.parseInt(idStr);
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Cliente cliente = JsonUtil.fromJson(json, Cliente.class);
            
            Optional<Cliente> updated = clienteService.update(id, cliente);
            if (updated.isPresent()) {
                sendJsonResponse(resp, updated.get(), HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al actualizar cliente: " + e.getMessage(), 
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
        
        try {
            String idStr = pathInfo.substring(1);
            Integer id = Integer.parseInt(idStr);
            boolean deleted = clienteService.deleteById(id);
            if (deleted) {
                sendJsonResponse(resp, "{\"message\": \"Cliente eliminado\"}", HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

