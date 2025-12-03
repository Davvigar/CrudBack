package org.david.crm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.david.crm.config.EntityManagerProducer;
import org.david.crm.model.Cliente;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements Repository<Cliente, Integer> {
    
    // Obtener EntityManager dinámicamente del ThreadLocal (creado por el filtro)
    private EntityManager getEntityManager() {
        EntityManager em = EntityManagerProducer.getCurrentEntityManager();
        if (em == null || !em.isOpen()) {
            throw new IllegalStateException("EntityManager no está disponible. El TransactionFilter debe ejecutarse primero.");
        }
        return em;
    }
    
    @Override
    public List<Cliente> findAll() {
        EntityManager em = getEntityManager();
        TypedQuery<Cliente> query = em.createQuery("SELECT c FROM Cliente c LEFT JOIN FETCH c.comercial", Cliente.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Cliente> findById(Integer id) {
        EntityManager em = getEntityManager();
        Cliente cliente = em.find(Cliente.class, id);
        return Optional.ofNullable(cliente);
    }
    
    public Optional<Cliente> findByUsername(String username) {
        EntityManager em = getEntityManager();
        // Búsqueda case-insensitive y con trim para evitar problemas
        TypedQuery<Cliente> query = em.createQuery(
            "SELECT c FROM Cliente c WHERE LOWER(TRIM(c.username)) = LOWER(TRIM(:username))", Cliente.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }
    
    public List<Cliente> findByComercialId(Integer comercialId) {
        EntityManager em = getEntityManager();
        TypedQuery<Cliente> query = em.createQuery(
            "SELECT c FROM Cliente c WHERE c.comercial.comercialId = :comercialId", Cliente.class);
        query.setParameter("comercialId", comercialId);
        return query.getResultList();
    }
    
    @Override
    public Cliente save(Cliente cliente) {
        EntityManager em = getEntityManager();
        if (cliente.getClienteId() == null) {
            em.persist(cliente);
            return cliente;
        } else {
            return em.merge(cliente);
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

