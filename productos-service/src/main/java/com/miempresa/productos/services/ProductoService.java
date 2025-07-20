package com.miempresa.productos.services;

import java.util.List;

import com.miempresa.productos.dtos.ProductoDto;

public interface ProductoService {
    ProductoDto crearProducto(ProductoDto dto);
    ProductoDto obtenerProductoPorId(Long id);
    List<ProductoDto> listarProductos();
}
