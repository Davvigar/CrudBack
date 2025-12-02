package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.config.EntityManagerProducer;
import org.david.crm.model.Comercial;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ComercialRepository implements Repository<Comercial, Integer> {
    
    // Obtener EntityManager dinámicamente del ThreadLocal (creado por el filtro)
    private EntityManager getEntityManager() {
        EntityManager em = EntityManagerProducer.getCurrentEntityManager();
        if (em == null || !em.isOpen()) {
            throw new IllegalStateException("EntityManager no está disponible. El TransactionFilter debe ejecutarse primero.");
        }
        return em;
    }
    
    @Override
    public List<Comercial> findAll() {
        EntityManager em = getEntityManager();
        TypedQuery<Comercial> query = em.createQuery("SELECT c FROM Comercial c", Comercial.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Comercial> findById(Integer id) {
        EntityManager em = getEntityManager();
        Comercial comercial = em.find(Comercial.class, id);
        return Optional.ofNullable(comercial);
    }
    
    public Optional<Comercial> findByUsername(String username) {
        EntityManager em = getEntityManager();
        TypedQuery<Comercial> query = em.createQuery(
            "SELECT c FROM Comercial c WHERE LOWER(TRIM(c.username)) = LOWER(TRIM(:username))", Comercial.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Comercial save(Comercial comercial) {
        EntityManager em = getEntityManager();
        if (comercial.getComercialId() == null) {
            em.persist(comercial);
            return comercial;
        } else {
            return em.merge(comercial);
        }
    }
    
    @Override
    public void deleteById(Integer id) {
        EntityManager em = getEntityManager();
        findById(id).ifPresent(em::remove);
    }
    
    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }
}

