package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.model.Factura;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FacturaRepository implements Repository<Factura, String> {
    
    @Inject
    private EntityManager em;
    
    @Override
    public List<Factura> findAll() {
        TypedQuery<Factura> query = em.createQuery("SELECT f FROM Factura f", Factura.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Factura> findById(String id) {
        Factura factura = em.find(Factura.class, id);
        return Optional.ofNullable(factura);
    }
    
    public List<Factura> findByClienteId(Integer clienteId) {
        TypedQuery<Factura> query = em.createQuery(
            "SELECT f FROM Factura f WHERE f.cliente.clienteId = :clienteId", Factura.class);
        query.setParameter("clienteId", clienteId);
        return query.getResultList();
    }
    
    public List<Factura> findByComercialId(Integer comercialId) {
        TypedQuery<Factura> query = em.createQuery(
            "SELECT f FROM Factura f WHERE f.comercial.comercialId = :comercialId", Factura.class);
        query.setParameter("comercialId", comercialId);
        return query.getResultList();
    }
    
    @Override
    public Factura save(Factura factura) {
        if (factura.getFacturaId() == null || !existsById(factura.getFacturaId())) {
            em.persist(factura);
            return factura;
        } else {
            return em.merge(factura);
        }
    }
    
    @Override
    public void deleteById(String id) {
        findById(id).ifPresent(em::remove);
    }
    
    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
}

