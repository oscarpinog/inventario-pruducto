package com.miempresa.productos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miempresa.productos.entities.ProductoEntity;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
}