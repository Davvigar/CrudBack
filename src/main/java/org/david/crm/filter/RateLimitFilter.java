package org.david.crm.filter;

import java.io.IOException;

import org.david.crm.concurrent.RateLimiter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebFilter("/api/*")
@ApplicationScoped
public class RateLimitFilter implements Filter {
    
    @Inject
    private RateLimiter rateLimiter;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
       
        String clientId = getClientId(httpRequest);
        
        
        if (!rateLimiter.allowRequest(clientId)) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.getWriter().write(
                String.format("{\"error\": \"Rate limit excedido. Límite: 100 peticiones por minuto. Restantes: %d\"}",
                    rateLimiter.getRemainingRequests(clientId))
            );
            return;
        }
        
       
        httpResponse.setHeader("X-RateLimit-Remaining", 
            String.valueOf(rateLimiter.getRemainingRequests(clientId)));
        httpResponse.setHeader("X-RateLimit-Limit", "100");
        
       
        chain.doFilter(request, response);
    }
    
  
    private String getClientId(HttpServletRequest request) { 
        
        return request.getRemoteAddr(); 
    }
}

