package com.perfulandia.productservice.service;

import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.model.Usuario; // Este es el DTO para la respuesta de UsuarioService
import com.perfulandia.productservice.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate; // Necesario para la comunicación

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final RestTemplate restTemplate;

    @Value("${usuarioservice.url=http://localhost:8080/api/usuarios}")
    private String usuarioServiceUrl;

    public ProductoService(ProductoRepository productoRepository, RestTemplate restTemplate){
        this.productoRepository = productoRepository;
        this.restTemplate = restTemplate;
    }

    // listar
    public List<Producto> listar(){
        return productoRepository.findAll();
    }

    // Guardar
    public Producto guardar(Producto producto){
        return productoRepository.save(producto);
    }

    // Buscar por id
    public Producto bucarPorId(long id){
        return productoRepository.findById(id).orElse(null);
    }

    // Eliminar por id
    public void eliminar(long id){
        productoRepository.deleteById(id);
    }

    public Usuario obtenerUsuarioExterno(long idUsuario) {
        try {
            String url = usuarioServiceUrl + "/" + idUsuario;
            return restTemplate.getForObject(url, Usuario.class);
        } catch (HttpClientErrorException.NotFound ex) {
            System.err.println("Usuario no encontrado en usuarioservice con ID: " + idUsuario + " - " + ex.getMessage());
            return null;
        } catch (Exception ex) {
            System.err.println("Error al comunicar con usuarioservice para obtener usuario ID " + idUsuario + ": " + ex.getMessage());
            throw new RuntimeException("Error de comunicación al intentar obtener datos del usuario.", ex);
        }
    }
}