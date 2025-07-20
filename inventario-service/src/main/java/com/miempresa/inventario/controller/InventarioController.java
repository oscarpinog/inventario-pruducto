package com.miempresa.inventario.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miempresa.inventario.dtos.InventarioDto;
import com.miempresa.inventario.services.InventarioService;
import com.miempresa.inventario.wrappers.JsonApiData;
import com.miempresa.inventario.wrappers.JsonApiWrapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping("/{productoId}")
    public ResponseEntity<String> consultarInventarioPorProductoId(@PathVariable Long productoId) {
        String inventario = inventarioService.consultarInventarioPorProductoId(productoId);
        return ResponseEntity.ok(inventario);
    }

    @PostMapping("/{productoId}")
    public ResponseEntity<JsonApiWrapper<InventarioDto>> actualizarCantidadInventario(
            @PathVariable Long productoId,
            @RequestParam int cantidad
    ) {
        InventarioDto inventario = inventarioService.actualizarCantidadInventario(productoId, cantidad);
        return ResponseEntity.ok(wrapResponse("inventario", productoId.toString(), inventario));
    }

    @PostMapping("/compra")
    public ResponseEntity<JsonApiWrapper<InventarioDto>> comprarProducto(
            @RequestParam Long productoId,
            @RequestParam int cantidad
    ) {
        InventarioDto inventarioActualizado = inventarioService.comprarProducto(productoId, cantidad);
        return ResponseEntity.ok(wrapResponse("inventario", productoId.toString(), inventarioActualizado));
    }

    private <T> JsonApiWrapper<T> wrapResponse(String type, String id, T attributes) {
        JsonApiData<T> data = new JsonApiData<>();
        data.setType(type);
        data.setId(id);
        data.setAttributes(attributes);

        JsonApiWrapper<T> wrapper = new JsonApiWrapper<>();
        wrapper.setData(data);

        return wrapper;
    }
}