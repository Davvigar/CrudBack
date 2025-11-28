package org.david.crm.filter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.david.crm.config.EntityManagerProducer;

import java.io.IOException;

public class TransactionFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización si es necesaria
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        EntityManager em = null;
        EntityTransaction transaction = null;
        boolean transactionActive = false;
        boolean committed = false;
        
        try {
            // Crear y configurar EntityManager ANTES de que CDI lo inyecte
            em = EntityManagerProducer.getEntityManagerFactory().createEntityManager();
            EntityManagerProducer.setEntityManager(em);
            
            // Iniciar transacción
            transaction = em.getTransaction();
            transaction.begin();
            transactionActive = true;
            
            // Continuar con la cadena de filtros (servlets usarán el EM del ThreadLocal)
            chain.doFilter(request, response);
            
            // Si llegamos aquí sin excepción, hacer commit
            if (transaction.isActive() && transactionActive) {
                transaction.commit();
                committed = true;
                transactionActive = false;
            }
            
        } catch (Exception e) {
            // Si hay error, hacer rollback
            if (transaction != null && transaction.isActive() && transactionActive) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    // Ignorar errores de rollback
                }
            }
            // Re-lanzar la excepción como ServletException
            if (e instanceof ServletException) {
                throw (ServletException) e;
            } else if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new ServletException("Error en transacción: " + e.getMessage(), e);
            }
        } finally {

            EntityManagerProducer.removeEntityManager();
        }
    }
    
    @Override
    public void destroy() {
    // Limpieza si es necesaria}
    }
}

