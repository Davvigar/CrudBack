package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.model.Producto;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductoRepository implements Repository<Producto, Integer> {
    
    @Inject
    private EntityManager em;
    
    @Override
    public List<Producto> findAll() {
        TypedQuery<Producto> query = em.createQuery("SELECT p FROM Producto p", Producto.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Producto> findById(Integer id) {
        Producto producto = em.find(Producto.class, id);
        return Optional.ofNullable(producto);
    }
    
    public List<Producto> findBySeccionId(Integer seccionId) {
        TypedQuery<Producto> query = em.createQuery(
            "SELECT p FROM Producto p WHERE p.seccion.seccionId = :seccionId", Producto.class);
        query.setParameter("seccionId", seccionId);
        return query.getResultList();
    }
    
    @Override
    public Producto save(Producto producto) {
        if (producto.getProductoId() == null) {
            em.persist(producto);
            return producto;
        } else {
            return em.merge(producto);
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

