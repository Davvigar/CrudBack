package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.model.Comercial;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ComercialRepository implements Repository<Comercial, Integer> {
    
    @Inject
    private EntityManager em;
    
    @Override
    public List<Comercial> findAll() {
        TypedQuery<Comercial> query = em.createQuery("SELECT c FROM Comercial c", Comercial.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Comercial> findById(Integer id) {
        Comercial comercial = em.find(Comercial.class, id);
        return Optional.ofNullable(comercial);
    }
    
    public Optional<Comercial> findByUsername(String username) {
        TypedQuery<Comercial> query = em.createQuery(
            "SELECT c FROM Comercial c WHERE c.username = :username", Comercial.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Comercial save(Comercial comercial) {
        if (comercial.getComercialId() == null) {
            em.persist(comercial);
            return comercial;
        } else {
            return em.merge(comercial);
        }
    }
    
    @Override
    public void deleteById(Integer id) {
        findById(id).ifPresent(em::remove);
    }
    
    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }
}

