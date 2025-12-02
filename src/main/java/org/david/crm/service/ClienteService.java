package org.david.crm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.david.crm.concurrent.AsyncLogService;
import org.david.crm.model.Cliente;
import org.david.crm.model.Comercial;
import org.david.crm.repository.ClienteRepository;
import org.david.crm.repository.ComercialRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteService {
    
    @Inject
    private ClienteRepository clienteRepository;
    
    @Inject
    private ComercialRepository comercialRepository;
    
    @Inject
    private EntityManager em;
    
    @Inject
    private AsyncLogService logService;
    
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }
    
    public Optional<Cliente> findById(Integer id) {
        return clienteRepository.findById(id);
    }
    
    public Optional<Cliente> findByUsername(String username) {
        return clienteRepository.findByUsername(username);
    }
    
    public List<Cliente> findByComercialId(Integer comercialId) {
        return clienteRepository.findByComercialId(comercialId);
    }
    
    public Cliente save(Cliente cliente) {
        if (cliente.getComercial() != null && cliente.getComercial().getComercialId() != null) {

            try {
                Comercial comercialRef = em.getReference(Comercial.class, cliente.getComercial().getComercialId());
                cliente.setComercial(comercialRef);
            } catch (Exception e) {
                // Si no existe, eliminar la referencia
                logService.logAsync("ClienteService.save() - Comercial ID " + cliente.getComercial().getComercialId() + " no encontrado, estableciendo a null");
                cliente.setComercial(null);
            }
        }
        Cliente saved = clienteRepository.save(cliente);
        logService.logAsync("ClienteService.save() - Cliente guardado: ID=" + saved.getClienteId() + ", Username=" + saved.getUsername());
        return saved;
    }
    

    public Optional<Cliente> update(Integer id, Cliente cliente) {
        try {
            return clienteRepository.findById(id)
                .map(existing -> {
                    // Preservar la versión para locking optimista
                    cliente.setClienteId(id);
                    cliente.setVersion(existing.getVersion());
                    
                    if (cliente.getComercial() != null && cliente.getComercial().getComercialId() != null) {
                        try {
                            Comercial comercialRef = em.getReference(Comercial.class, cliente.getComercial().getComercialId());
                            cliente.setComercial(comercialRef);
                        } catch (Exception e) {
                            cliente.setComercial(null);
                        }
                    }
                    Cliente updated = clienteRepository.save(cliente);
                    logService.logAsync("ClienteService.update() - Cliente actualizado: ID=" + updated.getClienteId());
                    return updated;
                });
        } catch (OptimisticLockException e) {
            // Lost Update detectado: otro usuario modificó el registro
            logService.logCriticoAsync("ClienteService.update() - OptimisticLockException para Cliente ID=" + id + ": El cliente fue modificado por otro usuario");
            throw new RuntimeException("El cliente fue modificado por otro usuario. Por favor, recarga los datos.", e);
        }
    }
    
    public boolean deleteById(Integer id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            logService.logAsync("ClienteService.deleteById() - Cliente eliminado: ID=" + id);
            return true;
        }
        logService.logAsync("ClienteService.deleteById() - Cliente no encontrado para eliminar: ID=" + id);
        return false;
    }
}

