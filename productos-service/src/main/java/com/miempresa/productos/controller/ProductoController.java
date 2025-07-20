package com.miempresa.productos.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miempresa.productos.dtos.ProductoDto;
import com.miempresa.productos.services.ProductoService;
import com.miempresa.productos.wrappers.JsonApiData;
import com.miempresa.productos.wrappers.JsonApiWrapper;

import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@SecurityRequirement(name = "apiKey")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    
    private static final String TYPE_PRODUCTO = "producto";
    
    @PostMapping
    public ResponseEntity<JsonApiWrapper<ProductoDto>> crearProducto(@RequestBody ProductoDto dto) {
        ProductoDto creado = productoService.crearProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toJsonApi(creado, TYPE_PRODUCTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonApiWrapper<ProductoDto>> obtenerProducto(@PathVariable Long id) {
        ProductoDto dto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(toJsonApi(dto, TYPE_PRODUCTO));
    }

    @GetMapping
    public ResponseEntity<List<JsonApiWrapper<ProductoDto>>> listarProductos() {
        List<ProductoDto> productos = productoService.listarProductos();
        List<JsonApiWrapper<ProductoDto>> respuesta = productos.stream()
            .map(dto -> toJsonApi(dto, TYPE_PRODUCTO))
            .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    private JsonApiWrapper<ProductoDto> toJsonApi(ProductoDto dto, String type) {
        JsonApiData<ProductoDto> data = new JsonApiData<>();
        data.setType(type);
        data.setId(dto.getId() != null ? dto.getId().toString() : null);
        data.setAttributes(dto);

        JsonApiWrapper<ProductoDto> wrapper = new JsonApiWrapper<>();
        wrapper.setData(data);
        return wrapper;
    }
}
