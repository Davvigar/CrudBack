package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.config.EntityManagerProducer;
import org.david.crm.model.Factura;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FacturaRepository implements Repository<Factura, String> {
    
    // Obtener EntityManager dinámicamente del ThreadLocal (creado por el filtro)
    private EntityManager getEntityManager() {
        EntityManager em = EntityManagerProducer.getCurrentEntityManager();
        if (em == null || !em.isOpen()) {
            throw new IllegalStateException("EntityManager no está disponible. El TransactionFilter debe ejecutarse primero.");
        }
        return em;
    }
    
    @Override
    public List<Factura> findAll() {
        EntityManager em = getEntityManager();
        TypedQuery<Factura> query = em.createQuery("SELECT f FROM Factura f", Factura.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Factura> findById(String id) {
        EntityManager em = getEntityManager();
        Factura factura = em.find(Factura.class, id);
        return Optional.ofNullable(factura);
    }
    
    public List<Factura> findByClienteId(Integer clienteId) {
        EntityManager em = getEntityManager();
        TypedQuery<Factura> query = em.createQuery(
            "SELECT f FROM Factura f WHERE f.cliente.clienteId = :clienteId", Factura.class);
        query.setParameter("clienteId", clienteId);
        return query.getResultList();
    }
    
    public List<Factura> findByComercialId(Integer comercialId) {
        EntityManager em = getEntityManager();
        TypedQuery<Factura> query = em.createQuery(
            "SELECT f FROM Factura f WHERE f.comercial.comercialId = :comercialId", Factura.class);
        query.setParameter("comercialId", comercialId);
        return query.getResultList();
    }
    
    @Override
    public Factura save(Factura factura) {
        EntityManager em = getEntityManager();
        if (factura.getFacturaId() == null || !existsById(factura.getFacturaId())) {
            em.persist(factura);
            return factura;
        } else {
            return em.merge(factura);
        }
    }
    
    @Override
    public void deleteById(String id) {
        EntityManager em = getEntityManager();
        findById(id).ifPresent(em::remove);
    }
    
    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}

