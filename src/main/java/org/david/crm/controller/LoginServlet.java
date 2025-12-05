package org.david.crm.controller;

import java.io.IOException;
import java.util.Optional;

import org.david.crm.model.Cliente;
import org.david.crm.model.Comercial;
import org.david.crm.service.ClienteService;
import org.david.crm.service.ComercialService;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/login")
@ApplicationScoped
public class LoginServlet extends HttpServlet {

    @Inject
    private ComercialService comercialService;

    @Inject
    private ClienteService clienteService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        if (username == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ERROR: Credenciales faltantes.");
            return;
        }

        
        username = username.trim();
        password = password.trim();

        try {
            // Intentar con comercial
            Optional<Comercial> comercialOpt = comercialService.findByUsername(username);
            if (comercialOpt.isPresent()) {
                Comercial comercial = comercialOpt.get();
                String storedHash = comercial.getPasswordHash();
                
                // Verificar contraseña con BCrypt o comparación directa 
                boolean passwordMatches = false;
                if (storedHash != null) {
                    storedHash = storedHash.trim(); // Limpiar espacios del hash almacenado
                    // Si el hash empieza con $2a$ o $2b$, es un hash BCrypt
                    if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
                        passwordMatches = BCrypt.checkpw(password, storedHash);
                    } else {
                       
                        passwordMatches = storedHash.equals(password);
                    }
                }
                
                if (passwordMatches) {
                    // Crear sesión HTTP
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userId", comercial.getComercialId());
                    session.setAttribute("userRole", comercial.getRol().toString());
                    session.setAttribute("userType", "comercial");
                    session.setAttribute("userName", comercial.getNombre());
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(comercial.getRol() + "," + comercial.getNombre() + "," + comercial.getComercialId());
                    return;
                }
            }

            Optional<Cliente> clienteOpt = clienteService.findByUsername(username);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                String storedHash = cliente.getPasswordHash();
                
               
                boolean passwordMatches = false;
                if (storedHash != null) {
                    storedHash = storedHash.trim(); 
                    if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
                        passwordMatches = BCrypt.checkpw(password, storedHash);
                    } else {
                       
                        passwordMatches = storedHash.equals(password);
                    }
                }
                
                if (passwordMatches) {
                    // Crear sesión HTTP para cliente
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userId", cliente.getClienteId());
                    session.setAttribute("userRole", "cliente");
                    session.setAttribute("userType", "cliente");
                    session.setAttribute("userName", cliente.getNombre());
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("cliente," + cliente.getNombre() + "," + cliente.getClienteId());
                    return;
                }
            }

            // Si no coincide ninguno
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("INVALID");

        } catch (Exception e) {
          
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("ERROR: Fallo interno en login. Detalle: " + e.getMessage());
        }
    }
}
