package org.david.crm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.david.crm.model.Producto;
import org.david.crm.model.Seccion;
import org.david.crm.repository.ProductoRepository;
import org.david.crm.repository.SeccionRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductoService {
    
    @Inject
    private ProductoRepository productoRepository;
    
    @Inject
    private SeccionRepository seccionRepository;
    
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }
    
    public Optional<Producto> findById(Integer id) {
        return productoRepository.findById(id);
    }
    
    public List<Producto> findBySeccionId(Integer seccionId) {
        return productoRepository.findBySeccionId(seccionId);
    }
    
    public Producto save(Producto producto) {
        if (producto.getSeccion() != null && producto.getSeccion().getSeccionId() != null) {
            Optional<Seccion> seccionOpt = seccionRepository.findById(
                producto.getSeccion().getSeccionId());
            seccionOpt.ifPresent(producto::setSeccion);
        }
        return productoRepository.save(producto);
    }
    
    public Optional<Producto> update(Integer id, Producto producto) {
        return productoRepository.findById(id)
            .map(existing -> {
                producto.setProductoId(id);
                if (producto.getSeccion() != null && producto.getSeccion().getSeccionId() != null) {
                    Optional<Seccion> seccionOpt = seccionRepository.findById(
                        producto.getSeccion().getSeccionId());
                    seccionOpt.ifPresent(producto::setSeccion);
                }
                return productoRepository.save(producto);
            });
    }
    
    public boolean deleteById(Integer id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

