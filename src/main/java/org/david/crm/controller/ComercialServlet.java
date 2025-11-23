package org.david.crm.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.david.crm.model.Comercial;
import org.david.crm.service.ComercialService;
import org.david.crm.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/comerciales/*")
@ApplicationScoped
public class ComercialServlet extends BaseServlet {
    
    @Inject
    private ComercialService comercialService;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Comercial> comerciales = comercialService.findAll();
            sendJsonResponse(resp, comerciales, HttpServletResponse.SC_OK);
        } else {
            String idStr = pathInfo.substring(1);
            try {
                Integer id = Integer.parseInt(idStr);
                Optional<Comercial> comercialOpt = comercialService.findById(id);
                if (comercialOpt.isPresent()) {
                    sendJsonResponse(resp, comercialOpt.get(), HttpServletResponse.SC_OK);
                } else {
                    sendErrorResponse(resp, "Comercial no encontrado", HttpServletResponse.SC_NOT_FOUND);
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
            Comercial comercial = JsonUtil.fromJson(json, Comercial.class);
            Comercial saved = comercialService.save(comercial);
            sendJsonResponse(resp, saved, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al crear comercial: " + e.getMessage(), 
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
            Comercial comercial = JsonUtil.fromJson(json, Comercial.class);
            
            Optional<Comercial> updated = comercialService.update(id, comercial);
            if (updated.isPresent()) {
                sendJsonResponse(resp, updated.get(), HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Comercial no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Error al actualizar comercial: " + e.getMessage(), 
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
            boolean deleted = comercialService.deleteById(id);
            if (deleted) {
                sendJsonResponse(resp, "{\"message\": \"Comercial eliminado\"}", HttpServletResponse.SC_OK);
            } else {
                sendErrorResponse(resp, "Comercial no encontrado", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

