package com.miempresa.inventario.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.miempresa.inventario.dtos.InventarioDto;
import com.miempresa.inventario.services.InventarioService;

@ExtendWith(MockitoExtension.class) // Habilita la integración de Mockito con JUnit 5
class InventarioControllerTest {

    @Mock // Crea un mock del InventarioService
    private InventarioService inventarioService;

    @InjectMocks // Inyecta los mocks en el InventarioController
    private InventarioController inventarioController;

    private MockMvc mockMvc; // Objeto para realizar solicitudes HTTP simuladas

    @BeforeEach // Se ejecuta antes de cada prueba
    void setUp() {
        // Configura MockMvc para el controlador, permitiendo probarlo de forma aislada
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).build();
    }


    @Test
    @DisplayName("Debe consultar inventario por productoId y devolver un String con estado 200 OK")
    void consultarInventarioPorProductoId_shouldReturnOkStatusAndString() throws Exception {
        // GIVEN: Un productoId y una respuesta esperada del servicio
        Long productoId = 123L;
        String inventarioInfo = "Inventario para producto 123: 50 unidades";

        // Cuando el servicio es llamado con cualquier Long, devuelve el String esperado
        when(inventarioService.consultarInventarioPorProductoId(anyLong())).thenReturn(inventarioInfo);

        // WHEN: Realizamos la solicitud HTTP GET
        mockMvc.perform(get("/api/inventario/{productoId}", productoId)
                .contentType(MediaType.APPLICATION_JSON)) // Aunque devuelve String, el cliente podría enviar JSON
                // THEN: Verificamos el estado y el contenido
                .andExpect(status().isOk()) // Esperamos un estado HTTP 200 OK
                .andExpect(content().string(inventarioInfo)); // Verificamos que el contenido del cuerpo sea el String esperado
    }


    @Test
    @DisplayName("Debe actualizar la cantidad del inventario y devolver el DTO actualizado con JsonApiWrapper")
    void actualizarCantidadInventario_shouldReturnUpdatedInventarioDtoWithJsonApiWrapper() throws Exception {
        // GIVEN: Datos para la actualización
        Long productoId = 456L;
        int cantidad = 75;
        InventarioDto inventarioActualizado = InventarioDto.builder()
                .id(1L)
                .productoId(productoId)
                .cantidad(cantidad)
                .build();

        // Cuando el servicio es llamado, devuelve el DTO actualizado
        when(inventarioService.actualizarCantidadInventario(anyLong(), anyInt()))
                .thenReturn(inventarioActualizado);

        // WHEN: Realizamos la solicitud HTTP POST con PathVariable y RequestParam
        mockMvc.perform(post("/api/inventario/{productoId}", productoId)
                .param("cantidad", String.valueOf(cantidad)) // Añadimos el RequestParam
                .contentType(MediaType.APPLICATION_JSON)) // Tipo de contenido esperado en la respuesta
                // THEN: Verificamos el estado y el formato JsonApi con los datos del DTO
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.id").value(productoId.toString())) // El ID del JsonApiWrapper es el productoId
                .andExpect(jsonPath("$.data.attributes.id").value(inventarioActualizado.getId())) // El ID del DTO de Inventario
                .andExpect(jsonPath("$.data.attributes.productoId").value(inventarioActualizado.getProductoId()))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(inventarioActualizado.getCantidad()));
    }


    @Test
    @DisplayName("Debe procesar la compra de un producto y devolver el DTO actualizado con JsonApiWrapper")
    void comprarProducto_shouldReturnUpdatedInventarioDtoWithJsonApiWrapper() throws Exception {
        // GIVEN: Datos para la compra
        Long productoId = 789L;
        int cantidad = 5;
        InventarioDto inventarioActualizadoPostCompra = InventarioDto.builder()
                .id(2L)
                .productoId(productoId)
                .cantidad(95) // Suponiendo que había 100 y se compraron 5
                .build();

        // Cuando el servicio es llamado, devuelve el DTO con la cantidad actualizada
        when(inventarioService.comprarProducto(anyLong(), anyInt()))
                .thenReturn(inventarioActualizadoPostCompra);

        // WHEN: Realizamos la solicitud HTTP POST con RequestParams
        mockMvc.perform(post("/api/inventario/compra")
                .param("productoId", String.valueOf(productoId))
                .param("cantidad", String.valueOf(cantidad))
                .contentType(MediaType.APPLICATION_JSON))
                // THEN: Verificamos el estado y el formato JsonApi con los datos del DTO
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.id").value(productoId.toString()))
                .andExpect(jsonPath("$.data.attributes.id").value(inventarioActualizadoPostCompra.getId()))
                .andExpect(jsonPath("$.data.attributes.productoId").value(inventarioActualizadoPostCompra.getProductoId()))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(inventarioActualizadoPostCompra.getCantidad()));
    }
}