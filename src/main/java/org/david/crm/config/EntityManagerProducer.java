package org.david.crm.config;

import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerProducer {
    
    private static final String PERSISTENCE_UNIT_NAME = "crmPU";
    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> emThreadLocal = new ThreadLocal<>();
    
    static {
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }
    
    @Produces
    public EntityManager createEntityManager() {
        EntityManager em = emThreadLocal.get();
        if (em == null) {
            em = emf.createEntityManager();
            emThreadLocal.set(em);
        }
        return em;
    }
    
    public static void setEntityManager(EntityManager em) {
        emThreadLocal.set(em);
    }
    
    public static void removeEntityManager() {
        emThreadLocal.remove();
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}

