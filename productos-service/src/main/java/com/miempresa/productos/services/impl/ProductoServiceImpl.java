package com.miempresa.productos.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.miempresa.productos.dtos.ProductoDto;
import com.miempresa.productos.entities.ProductoEntity;
import com.miempresa.productos.exceptions.RecursoNoEncontradoException;
import com.miempresa.productos.repositories.ProductoRepository;
import com.miempresa.productos.services.ProductoService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public ProductoDto crearProducto(ProductoDto productoDto) {
        log.info("Creando producto: {}", productoDto);
        ProductoEntity producto = new ProductoEntity();
        BeanUtils.copyProperties(productoDto, producto);

        ProductoEntity guardado = productoRepository.save(producto);
        log.info("Producto guardado con ID: {}", guardado.getId());

        return ProductoDto.builder()
                .id(guardado.getId())
                .nombre(guardado.getNombre())
                .descripcion(guardado.getDescripcion())
                .precio(guardado.getPrecio())
                .build();
    }

    @Override
    public ProductoDto obtenerProductoPorId(Long id) {
        log.info("Buscando producto con ID: {}", id);
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con ID: {}", id);
                    return new RecursoNoEncontradoException("Producto no encontrado con id " + id);
                });

        log.info("Producto encontrado: {}", producto.getNombre());

        return ProductoDto.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .build();
    }

    @Override
    public List<ProductoDto> listarProductos() {
        log.info("Listando todos los productos");
        List<ProductoEntity> productos = productoRepository.findAll();
        log.info("Total productos encontrados: {}", productos.size());

        return productos.stream().map(prod ->
                ProductoDto.builder()
                        .id(prod.getId())
                        .nombre(prod.getNombre())
                        .descripcion(prod.getDescripcion())
                        .precio(prod.getPrecio())
                        .build()
        ).collect(Collectors.toList());
    }
}
