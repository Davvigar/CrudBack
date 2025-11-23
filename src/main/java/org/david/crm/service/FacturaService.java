package org.david.crm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.david.crm.model.Factura;
import org.david.crm.model.Cliente;
import org.david.crm.model.Comercial;
import org.david.crm.model.Producto;
import org.david.crm.repository.FacturaRepository;
import org.david.crm.repository.ClienteRepository;
import org.david.crm.repository.ComercialRepository;
import org.david.crm.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FacturaService {
    
    @Inject
    private FacturaRepository facturaRepository;
    
    @Inject
    private ClienteRepository clienteRepository;
    
    @Inject
    private ComercialRepository comercialRepository;
    
    @Inject
    private ProductoRepository productoRepository;
    
    public List<Factura> findAll() {
        return facturaRepository.findAll();
    }
    
    public Optional<Factura> findById(String id) {
        return facturaRepository.findById(id);
    }
    
    public List<Factura> findByClienteId(Integer clienteId) {
        return facturaRepository.findByClienteId(clienteId);
    }
    
    public List<Factura> findByComercialId(Integer comercialId) {
        return facturaRepository.findByComercialId(comercialId);
    }
    
    public Factura save(Factura factura) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(
            factura.getCliente().getClienteId());
        clienteOpt.ifPresent(factura::setCliente);
        
        if (factura.getComercial() != null && factura.getComercial().getComercialId() != null) {
            Optional<Comercial> comercialOpt = comercialRepository.findById(
                factura.getComercial().getComercialId());
            comercialOpt.ifPresent(factura::setComercial);
        }
        
        Optional<Producto> productoOpt = productoRepository.findById(
            factura.getProducto().getProductoId());
        productoOpt.ifPresent(factura::setProducto);
        
        return facturaRepository.save(factura);
    }
    
    public Optional<Factura> update(String id, Factura factura) {
        return facturaRepository.findById(id)
            .map(existing -> {
                factura.setFacturaId(id);
                
                Optional<Cliente> clienteOpt = clienteRepository.findById(
                    factura.getCliente().getClienteId());
                clienteOpt.ifPresent(factura::setCliente);
                
                if (factura.getComercial() != null && factura.getComercial().getComercialId() != null) {
                    Optional<Comercial> comercialOpt = comercialRepository.findById(
                        factura.getComercial().getComercialId());
                    comercialOpt.ifPresent(factura::setComercial);
                }
                
                Optional<Producto> productoOpt = productoRepository.findById(
                    factura.getProducto().getProductoId());
                productoOpt.ifPresent(factura::setProducto);
                
                return facturaRepository.save(factura);
            });
    }
    
    public boolean deleteById(String id) {
        if (facturaRepository.existsById(id)) {
            facturaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

