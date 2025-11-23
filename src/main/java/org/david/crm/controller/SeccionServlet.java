package org.david.crm.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.david.crm.model.Seccion;
import org.david.crm.service.SeccionService;
import org.david.crm.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/secciones/*")
@ApplicationScoped
public class SeccionServlet extends BaseServlet {
    
    @Inject
    private SeccionService seccionService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Seccion> secciones = seccionService.findAll();
            sendJsonResponse(resp, secciones, HttpServletResponse.SC_OK);
        } else {
            String idStr = pathInfo.substring(1);
            try {
                Integer id = Integer.parseInt(idStr);
                Optional<Seccion> seccionOpt = seccionService.findById(id);
                if (seccionOpt.isPresent()) {
                    sendJsonResponse(resp, seccionOpt.get(), HttpServletResponse.SC_OK);
                } else {
                    sendErrorResponse(resp, "Sección no encontrada", HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String json = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            Seccion seccion = JsonUtil.fromJson(json, Seccion.class);
            Seccion saved = seccionService.save(seccion);
            sendJsonResponse(resp, saved, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al crear sección: " + e.getMessage(), 
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
            Seccion seccion = JsonUtil.fromJson(json, Seccion.class);
            
            Optional<Seccion> updated = seccionService.update(id, seccion);
            if (updated.isPresent()) {
                sendJsonResponse(resp, updated.get(), HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Sección no encontrada", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al actualizar sección: " + e.getMessage(), 
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
            boolean deleted = seccionService.deleteById(id);
            if (deleted) {
                sendJsonResponse(resp, "{\"message\": \"Sección eliminada\"}", HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Sección no encontrada", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

