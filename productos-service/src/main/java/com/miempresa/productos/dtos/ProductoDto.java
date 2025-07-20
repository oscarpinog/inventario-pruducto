package com.miempresa.productos.dtos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
}