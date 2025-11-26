package org.david.crm.concurrent;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rate Limiter para controlar el número de peticiones por minuto
 * Resuelve el problema de Rate Limiting de API
 * Utiliza recursos atómicos y locks para sincronización
 */
@ApplicationScoped
public class RateLimiter {
    
    // Mapa concurrente para almacenar contadores por IP
    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    // Límite de peticiones por minuto
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    
    // Tiempo de ventana en milisegundos (1 minuto)
    private static final long WINDOW_MS = 60_000;
    
    public RateLimiter() {
        // Hilo dedicado usando clase anónima para limpieza periódica
        Thread cleanerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(WINDOW_MS);
                        requestCounters.clear();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "RateLimitCleanerThread");
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }
    
    /**
     * Verifica si una petición está permitida
     * @param clientId Identificador del cliente (IP, usuario, etc.)
     * @return true si la petición está permitida, false si se excede el límite
     */
    public boolean allowRequest(String clientId) {
        RequestCounter counter = requestCounters.computeIfAbsent(
            clientId, 
            k -> new RequestCounter()
        );
        
        return counter.incrementAndCheck();
    }
    
    /**
     * Obtiene el número de peticiones restantes para un cliente
     */
    public int getRemainingRequests(String clientId) {
        RequestCounter counter = requestCounters.get(clientId);
        if (counter == null) {
            return MAX_REQUESTS_PER_MINUTE;
        }
        return counter.getRemaining();
    }
    
    /**
     * Clase interna que maneja el contador de peticiones por cliente
     * Utiliza AtomicInteger y ReentrantLock para sincronización
     */
    private static class RequestCounter {
        // Contador atómico de peticiones
        private final AtomicInteger count = new AtomicInteger(0);
        
        // Timestamp de inicio de la ventana
        private volatile long windowStart = System.currentTimeMillis();
        
        // Lock para sincronizar el reset de la ventana
        private final ReentrantLock lock = new ReentrantLock();
        
        /**
         * Incrementa el contador y verifica si está dentro del límite
         * Utiliza synchronized implícito a través de AtomicInteger
         */
        public boolean incrementAndCheck() {
            long now = System.currentTimeMillis();
            
            // Si ha pasado más de un minuto, resetear la ventana
            if (now - windowStart > WINDOW_MS) {
                lock.lock();
                try {
                    // Double-check después de adquirir el lock
                    if (now - windowStart > WINDOW_MS) {
                        count.set(0);
                        windowStart = now;
                    }
                } finally {
                    lock.unlock();
                }
            }
            
            // Incrementar contador atómico
            int current = count.incrementAndGet();
            return current <= MAX_REQUESTS_PER_MINUTE;
        }
        
        /**
         * Obtiene el número de peticiones restantes
         */
        public int getRemaining() {
            int current = count.get();
            return Math.max(0, MAX_REQUESTS_PER_MINUTE - current);
        }
    }
}

