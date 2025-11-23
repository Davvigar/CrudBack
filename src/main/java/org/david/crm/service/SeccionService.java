package org.david.crm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.david.crm.model.Seccion;
import org.david.crm.repository.SeccionRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SeccionService {
    
    @Inject
    private SeccionRepository seccionRepository;
    
    public List<Seccion> findAll() {
        return seccionRepository.findAll();
    }
    
    public Optional<Seccion> findById(Integer id) {
        return seccionRepository.findById(id);
    }
    
    public Seccion save(Seccion seccion) {
        return seccionRepository.save(seccion);
    }
    
    public Optional<Seccion> update(Integer id, Seccion seccion) {
        return seccionRepository.findById(id)
            .map(existing -> {
                seccion.setSeccionId(id);
                return seccionRepository.save(seccion);
            });
    }
    
    public boolean deleteById(Integer id) {
        if (seccionRepository.existsById(id)) {
            seccionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

