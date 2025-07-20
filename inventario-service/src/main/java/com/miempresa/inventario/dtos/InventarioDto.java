package com.miempresa.inventario.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventarioDto {
    private Long id;
    private Long productoId;
    private Integer cantidad;
}