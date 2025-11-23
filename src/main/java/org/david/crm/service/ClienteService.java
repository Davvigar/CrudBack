package org.david.crm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
            Optional<Comercial> comercialOpt = comercialRepository.findById(
                cliente.getComercial().getComercialId());
            comercialOpt.ifPresent(cliente::setComercial);
        }
        return clienteRepository.save(cliente);
    }
    
    public Optional<Cliente> update(Integer id, Cliente cliente) {
        return clienteRepository.findById(id)
            .map(existing -> {
                cliente.setClienteId(id);
                if (cliente.getComercial() != null && cliente.getComercial().getComercialId() != null) {
                    Optional<Comercial> comercialOpt = comercialRepository.findById(
                        cliente.getComercial().getComercialId());
                    comercialOpt.ifPresent(cliente::setComercial);
                }
                return clienteRepository.save(cliente);
            });
    }
    
    public boolean deleteById(Integer id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

