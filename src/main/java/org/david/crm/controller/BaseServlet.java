package org.david.crm.controller;

import java.io.IOException;
import java.io.PrintWriter;

import org.david.crm.util.JsonUtil;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public abstract class BaseServlet extends HttpServlet {
    
    protected void sendJsonResponse(HttpServletResponse resp, Object data, int status) throws IOException { // respueseta Json y serialaza
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        
        PrintWriter out = resp.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }
    
    protected void sendErrorResponse(HttpServletResponse resp, String message, int status) throws IOException { // error en formato Json
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        
        PrintWriter out = resp.getWriter();
        out.print("{\"error\": \"" + message + "\"}");
        out.flush();
    }
}

