package org.david.crm.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.david.crm.model.Producto;
import org.david.crm.service.ProductoService;
import org.david.crm.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/productos/*")
@ApplicationScoped
public class ProductoServlet extends BaseServlet {
    
    @Inject
    private ProductoService productoService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String seccionId = req.getParameter("seccionId");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Producto> productos;
            if (seccionId != null) {
                try {
                    Integer id = Integer.parseInt(seccionId);
                    productos = productoService.findBySeccionId(id);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, "seccionId inválido", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else {
                productos = productoService.findAll();
            }
            sendJsonResponse(resp, productos, HttpServletResponse.SC_OK);
        } else {
            String idStr = pathInfo.substring(1);
            try {
                Integer id = Integer.parseInt(idStr);
                Optional<Producto> productoOpt = productoService.findById(id);
                if (productoOpt.isPresent()) {
                    sendJsonResponse(resp, productoOpt.get(), HttpServletResponse.SC_OK);
                } else {
                    sendErrorResponse(resp, "Producto no encontrado", HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userRole = (String) req.getAttribute("userRole");
        
        // Solo admin puede crear productos
        if (!"pseudoadmin".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: solo administradores pueden crear productos", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Producto producto = JsonUtil.fromJson(json, Producto.class);
            Producto saved = productoService.save(producto);
            sendJsonResponse(resp, saved, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al crear producto: " + e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userRole = (String) req.getAttribute("userRole");
        
        // Solo admin puede actualizar productos
        if (!"pseudoadmin".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: solo administradores pueden actualizar productos", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            String idStr = pathInfo.substring(1);
            Integer id = Integer.parseInt(idStr);
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Producto producto = JsonUtil.fromJson(json, Producto.class);
            
            Optional<Producto> updated = productoService.update(id, producto);
            if (updated.isPresent()) {
                sendJsonResponse(resp, updated.get(), HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Producto no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al actualizar producto: " + e.getMessage(), 
                HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userRole = (String) req.getAttribute("userRole");
        
        // Solo admin puede eliminar productos
        if (!"pseudoadmin".equals(userRole)) {
            sendErrorResponse(resp, "Acceso denegado: solo administradores pueden eliminar productos", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "ID requerido", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            String idStr = pathInfo.substring(1);
            Integer id = Integer.parseInt(idStr);
            boolean deleted = productoService.deleteById(id);
            if (deleted) {
                sendJsonResponse(resp, "{\"message\": \"Producto eliminado\"}", HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Producto no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

