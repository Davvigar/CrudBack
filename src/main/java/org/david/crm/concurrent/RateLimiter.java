package org.david.crm.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class RateLimiter { // limite de requests por minuto por cliente
    

    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>(); // utilzaria la ip del cliente como clave y un contador de requests por minuto como valor
    

    private static final int MAX_REQUESTS_PER_MINUTE = 100; // maximo de requests por minuto
    

    private static final long WINDOW_MS = 60_000; // ventana de tiempo en milisegundos
    
    public RateLimiter() {

        Thread cleanerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(WINDOW_MS); // limpia el contador de requests por minuto cada ventana de tiempo
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
    

    public boolean allowRequest(String clientId) { // permite una request si el contador de requests por minuto es menor al maximo
        RequestCounter counter = requestCounters.computeIfAbsent(
            clientId, 
            k -> new RequestCounter()
        );
        
        return counter.incrementAndCheck();
    }
    

    public int getRemainingRequests(String clientId) {
        RequestCounter counter = requestCounters.get(clientId);
        if (counter == null) {
            return MAX_REQUESTS_PER_MINUTE;
        }
        return counter.getRemaining();
    }
    

    private static class RequestCounter {

        private final AtomicInteger count = new AtomicInteger(0);
        
        // con volatile
        private volatile long windowStart = System.currentTimeMillis(); // Hilo 1: Lee windowStart (valor viejo en caché) → Ventana expirada? NO
                                                                        //Hilo 2: Actualiza windowStart (valor nuevo) → Reset
                                                                        //Hilo 1: Sigue usando valor viejo → Lógica incorrecta


                                                                    // con volatile y lock
                                                                        //Hilo 1: Lee windowStart (fuerza lectura de memoria) → Ventana expirada? SÍ
                                                                        //Hilo 1: Entra al lock
                                                                        //Hilo 2: Espera...
                                                                        //Hilo 1: Actualiza windowStart → Reset
                                                                        //Hilo 2: Lee windowStart actualizado → Lógica correcta
        

        private final ReentrantLock lock = new ReentrantLock();


        public boolean incrementAndCheck() {
            long now = System.currentTimeMillis();
            
           
            if (now - windowStart > WINDOW_MS) {
                lock.lock();
                try {

                    if (now - windowStart > WINDOW_MS) {
                        count.set(0);
                        windowStart = now;
                    }
                } finally {
                    lock.unlock();
                }
            }
            

            int current = count.incrementAndGet();
            return current <= MAX_REQUESTS_PER_MINUTE;
        }
        

        public int getRemaining() {
            int current = count.get();
            return Math.max(0, MAX_REQUESTS_PER_MINUTE - current);
        }
    }
}

