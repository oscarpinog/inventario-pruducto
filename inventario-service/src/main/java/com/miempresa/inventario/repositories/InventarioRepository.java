package com.miempresa.inventario.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.miempresa.inventario.entities.InventarioEntity;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<InventarioEntity, Long> {
    Optional<InventarioEntity> findByProductoId(Long productoId);
}
