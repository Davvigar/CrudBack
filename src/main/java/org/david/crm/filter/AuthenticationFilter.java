package org.david.crm.filter;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/api/*")
@ApplicationScoped
public class AuthenticationFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización si es necesaria
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // Permitir login sin autenticación
        if (requestURI.endsWith("/login")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Verificar sesión
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write("{\"error\": \"No autenticado. Por favor, inicie sesión.\"}");
            return;
        }
        
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        String userType = (String) session.getAttribute("userType");
        
        httpRequest.setAttribute("userId", userId);
        httpRequest.setAttribute("userRole", userRole);
        httpRequest.setAttribute("userType", userType);
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
      
    }
}

