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
        // IMPORTANTE: Este método se llama cuando CDI inyecta el EntityManager
        // Los repositorios son ApplicationScoped, así que esto se ejecuta al crear el bean
        // Por eso siempre debemos obtener del ThreadLocal (actualizado por el filtro)
        
        EntityManager em = emThreadLocal.get();
        if (em != null && em.isOpen()) {
            return em;
        }
        
        // Si no hay EntityManager en ThreadLocal o está cerrado, crear uno nuevo
        // Esto puede pasar si se inyecta fuera de un contexto de request o el filtro no corrió
        em = emf.createEntityManager();
        emThreadLocal.set(em);
        return em;
    }
    
    // Método helper para obtener el EntityManager del ThreadLocal
    public static EntityManager getCurrentEntityManager() {
        return emThreadLocal.get();
    }
    
    public static void setEntityManager(EntityManager em) {
        emThreadLocal.set(em);
    }
    
    public static void removeEntityManager() {
        EntityManager em = emThreadLocal.get();
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e) {
                // Ignorar errores al cerrar
            }
        }
        emThreadLocal.remove();
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}

