package com.miempresa.productos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.productos.dtos.ProductoDto;
import com.miempresa.productos.services.ProductoService;

@ExtendWith(MockitoExtension.class) // Habilita la integración de Mockito con JUnit 5
class ProductoControllerTest {

    @Mock // Crea un mock (objeto simulado) del ProductoService
    private ProductoService productoService;

    @InjectMocks // Inyecta los mocks en la instancia de ProductoController que estamos probando
    private ProductoController productoController;

    private MockMvc mockMvc; // Objeto para simular solicitudes HTTP al controlador

    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setUp() {
        // Configura MockMvc para probar el controlador de forma aislada
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        // Inicializa ObjectMapper para serializar/deserializar JSON
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Debe crear un producto y devolver el estado 201 CREATED con el formato JsonApi")
    void crearProducto_shouldReturnCreatedStatusAndJsonApiFormat() throws Exception {
        // GIVEN: Preparamos los datos de entrada y salida esperados
        ProductoDto productoDtoEntrada = ProductoDto.builder()
                .nombre("Laptop Ultrabook")
                .descripcion("Portátil ligero y potente para profesionales")
                .precio(new BigDecimal("1250.75"))
                .build();

        ProductoDto productoDtoSalida = ProductoDto.builder()
                .id(1L)
                .nombre("Laptop Ultrabook")
                .descripcion("Portátil ligero y potente para profesionales")
                .precio(new BigDecimal("1250.75"))
                .build();

        // Mockeamos el comportamiento del servicio: cuando se llame a crearProducto,
        // debe devolver nuestro productoDtoSalida simulado.
        when(productoService.crearProducto(any(ProductoDto.class))).thenReturn(productoDtoSalida);

        // WHEN: Realizamos la solicitud HTTP POST simulada al controlador
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON) // Indicamos que el contenido es JSON
                .content(objectMapper.writeValueAsString(productoDtoEntrada))) // Convertimos el DTO a JSON para el cuerpo de la solicitud
                // THEN: Verificamos los resultados esperados
                .andExpect(status().isCreated()) // Esperamos un estado HTTP 201 (Created)
                .andExpect(jsonPath("$.data.type").value("producto")) // Verificamos el tipo 'producto' en la respuesta JSON (formato JsonApi)
                .andExpect(jsonPath("$.data.id").value("1")) // Verificamos el ID del producto
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop Ultrabook")) // Verificamos el nombre del producto
                .andExpect(jsonPath("$.data.attributes.descripcion").value("Portátil ligero y potente para profesionales")) // Verificamos la descripción
                .andExpect(jsonPath("$.data.attributes.precio").value(1250.75)); // Verificamos el precio (BigDecimal)
    }

    @Test
    @DisplayName("Debe obtener un producto por ID y devolver el estado 200 OK con el formato JsonApi")
    void obtenerProducto_shouldReturnOkStatusAndJsonApiFormat() throws Exception {
        // GIVEN
        Long productoId = 2L;
        ProductoDto productoDto = ProductoDto.builder()
                .id(productoId)
                .nombre("Monitor Curvo")
                .descripcion("Experiencia inmersiva para juegos y trabajo")
                .precio(new BigDecimal("399.99"))
                .build();

        // Mockeamos el servicio para que devuelva el producto cuando se pida por ID
        when(productoService.obtenerProductoPorId(productoId)).thenReturn(productoDto);

        // WHEN
        mockMvc.perform(get("/api/productos/{id}", productoId) // Hacemos un GET al endpoint con el ID
                .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk()) // Esperamos un estado HTTP 200 (OK)
                .andExpect(jsonPath("$.data.type").value("producto"))
                .andExpect(jsonPath("$.data.id").value("2"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Monitor Curvo"))
                .andExpect(jsonPath("$.data.attributes.descripcion").value("Experiencia inmersiva para juegos y trabajo"))
                .andExpect(jsonPath("$.data.attributes.precio").value(399.99));
    }

    @Test
    @DisplayName("Debe listar todos los productos y devolver el estado 200 OK con el formato JsonApi")
    void listarProductos_shouldReturnOkStatusAndJsonApiFormat() throws Exception {
        // GIVEN
        List<ProductoDto> productos = Arrays.asList(
            ProductoDto.builder()
                .id(1L)
                .nombre("Teclado Mecánico")
                .descripcion("Teclas responsivas para escritura y juegos")
                .precio(new BigDecimal("89.90"))
                .build(),
            ProductoDto.builder()
                .id(2L)
                .nombre("Webcam HD")
                .descripcion("Alta definición para videollamadas")
                .precio(new BigDecimal("55.00"))
                .build()
        );

        // Mockeamos el servicio para que devuelva la lista de productos
        when(productoService.listarProductos()).thenReturn(productos);

        // WHEN
        mockMvc.perform(get("/api/productos") // Hacemos un GET al endpoint sin ID
                .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk()) // Esperamos un estado HTTP 200 (OK)
                // Verificamos el primer elemento de la lista
                .andExpect(jsonPath("$[0].data.type").value("producto"))
                .andExpect(jsonPath("$[0].data.id").value("1"))
                .andExpect(jsonPath("$[0].data.attributes.nombre").value("Teclado Mecánico"))
                .andExpect(jsonPath("$[0].data.attributes.descripcion").value("Teclas responsivas para escritura y juegos"))
                .andExpect(jsonPath("$[0].data.attributes.precio").value(89.90))
                // Verificamos el segundo elemento de la lista
                .andExpect(jsonPath("$[1].data.type").value("producto"))
                .andExpect(jsonPath("$[1].data.id").value("2"))
                .andExpect(jsonPath("$[1].data.attributes.nombre").value("Webcam HD"))
                .andExpect(jsonPath("$[1].data.attributes.descripcion").value("Alta definición para videollamadas"))
                .andExpect(jsonPath("$[1].data.attributes.precio").value(55.00));
    }
}