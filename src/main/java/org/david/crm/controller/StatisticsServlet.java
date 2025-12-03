package org.david.crm.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.david.crm.concurrent.stats.ApiStatistics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/api/estadisticas")
@ApplicationScoped
public class StatisticsServlet extends BaseServlet {
    
    @Inject
    private ApiStatistics apiStatistics;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        ApiStatistics.StatisticsSummary summary = apiStatistics.getSummary();
        
        // Usar DecimalFormat con Locale.US para asegurar punto decimal en JSON
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        String avgTimeStr = df.format(summary.getAverageResponseTime());
        
        String json = String.format(
            "{\"totalRequests\": %d, " +
            "\"successfulRequests\": %d, " +
            "\"failedRequests\": %d, " +
            "\"logsWritten\": %d, " +
            "\"averageResponseTime\": %s}",
            summary.getTotalRequests(),
            summary.getSuccessfulRequests(),
            summary.getFailedRequests(),
            summary.getLogsWritten(),
            avgTimeStr
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

