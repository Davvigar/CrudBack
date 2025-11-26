package org.david.crm.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.david.crm.concurrent.stats.ApiStatistics;

import java.io.IOException;

/**
 * Servlet para obtener estadísticas de la API
 * Demuestra el uso de recursos atómicos
 */
@WebServlet("/api/estadisticas")
@ApplicationScoped
public class StatisticsServlet extends BaseServlet {
    
    @Inject
    private ApiStatistics apiStatistics;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        ApiStatistics.StatisticsSummary summary = apiStatistics.getSummary();
        
        String json = String.format(
            "{\"totalRequests\": %d, " +
            "\"successfulRequests\": %d, " +
            "\"failedRequests\": %d, " +
            "\"logsWritten\": %d, " +
            "\"averageResponseTime\": %.2f}",
            summary.getTotalRequests(),
            summary.getSuccessfulRequests(),
            summary.getFailedRequests(),
            summary.getLogsWritten(),
            summary.getAverageResponseTime()
        );
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        apiStatistics.reset();
        sendJsonResponse(resp, "{\"message\": \"Estadísticas reseteadas\"}", 
            HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String fileName = req.getParameter("file");
        if (fileName == null || fileName.isBlank()) {
            fileName = "estadisticas_" + System.currentTimeMillis() + ".txt";
        }
        apiStatistics.exportSummaryAsync(fileName);
        sendJsonResponse(resp, 
            String.format("{\"message\": \"Exportando estadísticas en segundo plano\", \"file\": \"%s\"}", fileName),
            HttpServletResponse.SC_ACCEPTED);
    }
}

