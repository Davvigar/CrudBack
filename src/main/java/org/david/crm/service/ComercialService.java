package org.david.crm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.david.crm.model.Comercial;
import org.david.crm.repository.ComercialRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ComercialService {
    
    @Inject
    private ComercialRepository comercialRepository;
    
    public List<Comercial> findAll() {
        return comercialRepository.findAll();
    }
    
    public Optional<Comercial> findById(Integer id) {
        return comercialRepository.findById(id);
    }
    
    public Optional<Comercial> findByUsername(String username) {
        return comercialRepository.findByUsername(username);
    }
    
    public Comercial save(Comercial comercial) {
        return comercialRepository.save(comercial);
    }
    
    public Optional<Comercial> update(Integer id, Comercial comercial) {
        return comercialRepository.findById(id)
            .map(existing -> {
                comercial.setComercialId(id);
                return comercialRepository.save(comercial);
            });
    }
    
    public boolean deleteById(Integer id) {
        if (comercialRepository.existsById(id)) {
            comercialRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

