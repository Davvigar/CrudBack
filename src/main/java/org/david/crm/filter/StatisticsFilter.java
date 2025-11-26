package org.david.crm.filter;

import java.io.IOException;

import org.david.crm.concurrent.stats.ApiStatistics;

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
public class StatisticsFilter implements Filter {
    
    @Inject
    private ApiStatistics apiStatistics; // estadisticas de la api incluye atomaticos para evitar race conditions
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        // Incrementar contador de peticiones totales 
        apiStatistics.incrementTotalRequests();
        
        try {
         
            chain.doFilter(request, response);
            
           
            int status = httpResponse.getStatus();
            if (status >= 200 && status < 300) {
                apiStatistics.incrementSuccessfulRequests();
            } else {
                apiStatistics.incrementFailedRequests();
            }
        } catch (Exception e) {
            // Si hay una excepción incrementar contador de fallos
            apiStatistics.incrementFailedRequests();
            throw e;
        } finally {
            // Calcular tiempo de respuesta y añadirlo al total
            long responseTime = System.currentTimeMillis() - startTime;
            apiStatistics.addResponseTime(responseTime);
        }
    }
}

