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
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        String clienteId = req.getParameter("clienteId");
        String comercialId = req.getParameter("comercialId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Factura> facturas;
            
            // Si es admin, puede ver todas las facturas
            if ("pseudoadmin".equals(userRole)) {
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
            } else if ("comercial".equals(userRole)) {
                // Si es comercial, solo ve sus facturas
                facturas = facturaService.findByComercialId(userId);
            } else {
                // Cliente solo puede ver sus propias facturas
                facturas = facturaService.findByClienteId(userId);
            }
            
            sendJsonResponse(resp, facturas, HttpServletResponse.SC_OK);
        } else {
            String id = pathInfo.substring(1);
            Optional<Factura> facturaOpt = facturaService.findById(id);
            if (facturaOpt.isPresent()) {
                Factura factura = facturaOpt.get();
                
                // Verificar permisos
                if ("comercial".equals(userRole)) {
                    if (factura.getComercial() == null || !factura.getComercial().getComercialId().equals(userId)) {
                        sendErrorResponse(resp, "Acceso denegado: no tiene permiso para ver esta factura", HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } else if ("cliente".equals(userRole)) {
                    if (factura.getCliente() == null || !factura.getCliente().getClienteId().equals(userId)) {
                        sendErrorResponse(resp, "Acceso denegado: no tiene permiso para ver esta factura", HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }
                
                sendJsonResponse(resp, factura, HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Factura no encontrada", HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        // Solo admin y comerciales pueden crear facturas
        if (!"pseudoadmin".equals(userRole) && !"comercial".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: no tiene permiso para crear facturas", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Factura factura = JsonUtil.fromJson(json, Factura.class);
            
            // Si es comercial (no admin), forzar que la factura pertenezca a él
            if ("comercial".equals(userRole)) {
                if (factura.getComercial() == null || factura.getComercial().getComercialId() == null) {
                    // Si no viene comercial, asignarlo automáticamente
                    org.david.crm.model.Comercial comercialRef = new org.david.crm.model.Comercial();
                    comercialRef.setComercialId(userId);
                    factura.setComercial(comercialRef);
                } else if (!factura.getComercial().getComercialId().equals(userId)) {
                    // Si intenta asignar a otro comercial, denegar
                    sendErrorResponse(resp, "No puede asignar facturas a otros comerciales", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
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
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Solo admin y comerciales pueden actualizar facturas
        if (!"pseudoadmin".equals(userRole) && !"comercial".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: no tiene permiso para actualizar facturas", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String id = pathInfo.substring(1);
            
            // Verificar que la factura existe y pertenece al comercial (si no es admin)
            Optional<Factura> existingOpt = facturaService.findById(id);
            if (existingOpt.isEmpty()) {
                sendErrorResponse(resp, "Factura no encontrada", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if ("comercial".equals(userRole)) {
                Factura existing = existingOpt.get();
                if (existing.getComercial() == null || !existing.getComercial().getComercialId().equals(userId)) {
                    sendErrorResponse(resp, "Acceso denegado: no puede modificar facturas de otros comerciales", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Factura factura = JsonUtil.fromJson(json, Factura.class);
            
            // Si es comercial, asegurar que no cambie el comercial asignado
            if ("comercial".equals(userRole)) {
                Factura existing = existingOpt.get();
                if (factura.getComercial() == null || factura.getComercial().getComercialId() == null) {
                    // Mantener el comercial original
                    factura.setComercial(existing.getComercial());
                } else if (!factura.getComercial().getComercialId().equals(userId)) {
                    // No puede cambiar el comercial
                    sendErrorResponse(resp, "No puede cambiar el comercial asignado", HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            
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
        String userRole = (String) req.getAttribute("userRole");
        Integer userId = (Integer) req.getAttribute("userId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Solo admin puede eliminar facturas
        if (!"pseudoadmin".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: solo administradores pueden eliminar facturas", HttpServletResponse.SC_FORBIDDEN);
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

