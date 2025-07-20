package com.miempresa.inventario.services;


import com.miempresa.inventario.dtos.InventarioDto;

public interface InventarioService {
   
	String consultarInventarioPorProductoId(Long productoId);
    
    InventarioDto actualizarCantidadInventario(Long productoId, int nuevaCantidad);
   
    InventarioDto comprarProducto(Long productoId, int cantidad);
}
