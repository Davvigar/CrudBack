package org.david.crm.concurrent;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


@ApplicationScoped
public class RateLimiter {
    

    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    

    private static final long WINDOW_MS = 60_000;
    
    public RateLimiter() {

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
    

    public boolean allowRequest(String clientId) {
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
        

        private volatile long windowStart = System.currentTimeMillis();
        
        // Lock para sincronizar el reset de la ventana
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

