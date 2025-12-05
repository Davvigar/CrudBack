package org.david.crm.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.david.crm.concurrent.AsyncLogService;
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
    
    @Inject
    private AsyncLogService logService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Cliente> clientes;
            
            // Si es admin, puede ver todos los clientes
            if ("pseudoadmin".equals(userRole)) {
                clientes = clienteService.findAll();
                logService.logAsync("GET /api/clientes - Admin: " + clientes.size() + " clientes recuperados");
            } else if ("comercial".equals(userRole)) {
                // Si es comercial, solo ve sus clientes
                clientes = clienteService.findByComercialId(userId);
                logService.logAsync("GET /api/clientes - Comercial ID " + userId + ": " + clientes.size() + " clientes encontrados");
            } else {
                // Cliente solo puede ver su propia información (no implementado en esta vista)
                sendErrorResponse(resp, "Acceso denegado", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            sendJsonResponse(resp, clientes, HttpServletResponse.SC_OK);
        } else {
            String idStr = pathInfo.substring(1);
            try {
                Integer id = Integer.parseInt(idStr);
                Optional<Cliente> clienteOpt = clienteService.findById(id);
                if (clienteOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    
                    // Verificar permisos: admin puede ver cualquier cliente, comercial solo los suyos
                    if (!"pseudoadmin".equals(userRole) && "comercial".equals(userRole)) {
                        if (cliente.getComercial() == null || !cliente.getComercial().getComercialId().equals(userId)) {
                            sendErrorResponse(resp, "Acceso denegado: no tiene permiso para ver este cliente", HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }
                    }
                    
                    logService.logAsync("GET /api/clientes/" + id + " - Cliente recuperado: " + cliente.getNombre());
                    sendJsonResponse(resp, cliente, HttpServletResponse.SC_OK);
                } else {
                    logService.logAsync("GET /api/clientes/" + id + " - Cliente no encontrado");
                    sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                logService.logAsync("GET /api/clientes - Error: ID inválido: " + pathInfo);
                sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Solo admin y comerciales pueden crear clientes
        if (!"pseudoadmin".equals(userRole) && !"comercial".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: no tiene permiso para crear clientes", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Cliente cliente = JsonUtil.fromJson(json, Cliente.class);
            
            // Si es comercial (no admin), forzar que el cliente pertenezca a él
            if ("comercial".equals(userRole)) {
                if (cliente.getComercial() == null || cliente.getComercial().getComercialId() == null) {
                    // Si no viene comercial, asignarlo automáticamente
                    org.david.crm.model.Comercial comercialRef = new org.david.crm.model.Comercial();
                    comercialRef.setComercialId(userId);
                    cliente.setComercial(comercialRef);
                } else if (!cliente.getComercial().getComercialId().equals(userId)) {
                    // Si intenta asignar a otro comercial, denegar
                    sendErrorResponse(resp, "No puede asignar clientes a otros comerciales", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            Cliente saved = clienteService.save(cliente);
            logService.logAsync("POST /api/clientes - Cliente creado: ID=" + saved.getClienteId() + ", Username=" + saved.getUsername());
            sendJsonResponse(resp, saved, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            logService.logCriticoAsync("POST /api/clientes - Error al crear cliente: " + e.getMessage());
            sendErrorResponse(resp, "Error al crear cliente: " + e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Solo admin y comerciales pueden actualizar clientes
        if (!"pseudoadmin".equals(userRole) && !"comercial".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: no tiene permiso para actualizar clientes", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String idStr = pathInfo.substring(1);
            Integer id = Integer.parseInt(idStr);
            
            // Verificar que el cliente existe y pertenece al comercial (si no es admin)
            Optional<Cliente> existingOpt = clienteService.findById(id);
            if (existingOpt.isEmpty()) {
                sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if ("comercial".equals(userRole)) {
                Cliente existing = existingOpt.get();
                if (existing.getComercial() == null || !existing.getComercial().getComercialId().equals(userId)) {
                    sendErrorResponse(resp, "Acceso denegado: no puede modificar clientes de otros comerciales", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Cliente cliente = JsonUtil.fromJson(json, Cliente.class);
            
            // Si es comercial, asegurar que no cambie el comercial asignado
            if ("comercial".equals(userRole)) {
                Cliente existing = existingOpt.get();
                if (cliente.getComercial() == null || cliente.getComercial().getComercialId() == null) {
                    // Mantener el comercial original
                    cliente.setComercial(existing.getComercial());
                } else if (!cliente.getComercial().getComercialId().equals(userId)) {
                    // No puede cambiar el comercial
                    sendErrorResponse(resp, "No puede cambiar el comercial asignado", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            Optional<Cliente> updated = clienteService.update(id, cliente);
            if (updated.isPresent()) {
                logService.logAsync("PUT /api/clientes/" + id + " - Cliente actualizado: " + updated.get().getNombre());
                sendJsonResponse(resp, updated.get(), HttpServletResponse.SC_OK);
            } else {
                logService.logAsync("PUT /api/clientes/" + id + " - Cliente no encontrado");
                sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            logService.logAsync("PUT /api/clientes - Error: ID inválido: " + pathInfo);
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logService.logCriticoAsync("PUT /api/clientes/" + pathInfo.substring(1) + " - Error al actualizar: " + e.getMessage());
            sendErrorResponse(resp, "Error al actualizar cliente: " + e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Solo admin puede eliminar clientes
        if (!"pseudoadmin".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: solo administradores pueden eliminar clientes", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String idStr = pathInfo.substring(1);
            Integer id = Integer.parseInt(idStr);
            boolean deleted = clienteService.deleteById(id);
            if (deleted) {
                logService.logAsync("DELETE /api/clientes/" + id + " - Cliente eliminado correctamente");
                sendJsonResponse(resp, "{\"message\": \"Cliente eliminado\"}", HttpServletResponse.SC_OK);
            } else {
                logService.logAsync("DELETE /api/clientes/" + id + " - Cliente no encontrado para eliminar");
                sendErrorResponse(resp, "Cliente no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            logService.logAsync("DELETE /api/clientes - Error: ID inválido: " + pathInfo);
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

