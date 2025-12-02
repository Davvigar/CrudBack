package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.config.EntityManagerProducer;
import org.david.crm.model.Producto;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductoRepository implements Repository<Producto, Integer> {
    
    // Obtener EntityManager dinámicamente del ThreadLocal (creado por el filtro)
    private EntityManager getEntityManager() {
        EntityManager em = EntityManagerProducer.getCurrentEntityManager();
        if (em == null || !em.isOpen()) {
            throw new IllegalStateException("EntityManager no está disponible. El TransactionFilter debe ejecutarse primero.");
        }
        return em;
    }
    
    @Override
    public List<Producto> findAll() {
        EntityManager em = getEntityManager();
        TypedQuery<Producto> query = em.createQuery("SELECT p FROM Producto p", Producto.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Producto> findById(Integer id) {
        EntityManager em = getEntityManager();
        Producto producto = em.find(Producto.class, id);
        return Optional.ofNullable(producto);
    }
    
    public List<Producto> findBySeccionId(Integer seccionId) {
        EntityManager em = getEntityManager();
        TypedQuery<Producto> query = em.createQuery(
            "SELECT p FROM Producto p WHERE p.seccion.seccionId = :seccionId", Producto.class);
        query.setParameter("seccionId", seccionId);
        return query.getResultList();
    }
    
    @Override
    public Producto save(Producto producto) {
        EntityManager em = getEntityManager();
        if (producto.getProductoId() == null) {
            em.persist(producto);
            return producto;
        } else {
            return em.merge(producto);
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

