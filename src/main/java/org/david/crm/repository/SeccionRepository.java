package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.model.Seccion;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SeccionRepository implements Repository<Seccion, Integer> {
    
    @Inject
    private EntityManager em;
    
    @Override
    public List<Seccion> findAll() {
        TypedQuery<Seccion> query = em.createQuery("SELECT s FROM Seccion s", Seccion.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Seccion> findById(Integer id) {
        Seccion seccion = em.find(Seccion.class, id);
        return Optional.ofNullable(seccion);
    }
    
    @Override
    public Seccion save(Seccion seccion) {
        if (seccion.getSeccionId() == null) {
            em.persist(seccion);
            return seccion;
        } else {
            return em.merge(seccion);
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

