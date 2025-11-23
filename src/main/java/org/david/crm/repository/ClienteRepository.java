package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.model.Cliente;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements Repository<Cliente, Integer> {
    
    @Inject
    private EntityManager em;
    
    @Override
    public List<Cliente> findAll() {
        TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c", Cliente.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Cliente> findById(Integer id) {
        Cliente cliente = em.find(Cliente.class, id);
        return Optional.ofNullable(cliente);
    }
    
    public Optional<Cliente> findByUsername(String username) {
        TypedQuery<Cliente> query = em.createQuery(
            "SELECT c FROM Cliente c WHERE c.username = :username", Cliente.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }
    
    public List<Cliente> findByComercialId(Integer comercialId) {
        TypedQuery<Cliente> query = em.createQuery(
            "SELECT c FROM Cliente c WHERE c.comercial.comercialId = :comercialId", Cliente.class);
        query.setParameter("comercialId", comercialId);
        return query.getResultList();
    }
    
    @Override
    public Cliente save(Cliente cliente) {
        if (cliente.getClienteId() == null) {
            em.persist(cliente);
            return cliente;
        } else {
            return em.merge(cliente);
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

