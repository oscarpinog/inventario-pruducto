package com.miempresa.productos.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.miempresa.productos.dtos.ProductoDto;
import com.miempresa.productos.entities.ProductoEntity;
import com.miempresa.productos.exceptions.RecursoNoEncontradoException;
import com.miempresa.productos.repositories.ProductoRepository;
import com.miempresa.productos.services.impl.ProductoServiceImpl;

@ExtendWith(MockitoExtension.class) 
class ProductoServiceImplTest {

    @Mock // Crea un mock del ProductoRepository
    private ProductoRepository productoRepository;

    @InjectMocks // Inyecta los mocks necesarios en ProductoServiceImpl
    private ProductoServiceImpl productoService;

    // Productos de ejemplo para usar en las pruebas
    private ProductoDto productoDto;
    private ProductoEntity productoEntity; // Ahora sin @Builder, usaremos constructor

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setUp() {
        // Inicializamos los objetos de prueba
        // ProductoDto sí usa @Builder
        productoDto = ProductoDto.builder()
                .id(1L)
                .nombre("Monitor Gaming")
                .descripcion("Monitor de alta tasa de refresco para juegos")
                .precio(new BigDecimal("450.99"))
                .build();

        // ProductoEntity NO usa @Builder, así que usamos su constructor AllArgsConstructor
        productoEntity = new ProductoEntity(
                1L,
                "Monitor Gaming",
                "Monitor de alta tasa de refresco para juegos",
                new BigDecimal("450.99")
        );
    }


    @Test
    @DisplayName("Debe crear un producto correctamente y devolver el DTO")
    void crearProducto_shouldReturnCreatedProductDto() {
        // GIVEN: Cuando el repositorio.save() es llamado con cualquier ProductoEntity,
        // debe devolver el productoEntity simulado (con ID asignado).
        // Si el ProductoDto de entrada tiene un ID, BeanUtils.copyProperties lo copiará al entity,
        // pero en el servicio el ID real lo asigna la base de datos (simulada por save()).
        // Por eso el entity de salida debe tener el ID asignado.
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(productoEntity);

        // WHEN: Llamamos al método que queremos probar
        // Pasamos un ProductoDto sin ID para simular una creación real (el ID se asigna al guardar)
        ProductoDto productoDtoSinId = ProductoDto.builder()
                .nombre("Monitor Gaming")
                .descripcion("Monitor de alta tasa de refresco para juegos")
                .precio(new BigDecimal("450.99"))
                .build();

        ProductoDto resultado = productoService.crearProducto(productoDtoSinId);

        // THEN: Verificamos los resultados
        assertNotNull(resultado); // Aseguramos que no sea nulo
        assertEquals(productoEntity.getId(), resultado.getId()); // Comparamos el ID asignado por el repositorio
        assertEquals(productoDtoSinId.getNombre(), resultado.getNombre()); // Comparamos el nombre
        assertEquals(productoDtoSinId.getDescripcion(), resultado.getDescripcion()); // Comparamos la descripción
        assertTrue(productoDtoSinId.getPrecio().compareTo(resultado.getPrecio()) == 0); // Comparamos BigDecimal
        // Verificamos que el método save() del repositorio fue llamado exactamente una vez
        verify(productoRepository, times(1)).save(any(ProductoEntity.class));
    }



    @Test
    @DisplayName("Debe obtener un producto por ID existente y devolver el DTO")
    void obtenerProductoPorId_shouldReturnProductDtoWhenFound() {
        // GIVEN: Cuando el repositorio.findById() es llamado con el ID específico,
        // debe devolver un Optional que contiene el productoEntity simulado.
        when(productoRepository.findById(anyLong())).thenReturn(Optional.of(productoEntity));

        // WHEN
        ProductoDto resultado = productoService.obtenerProductoPorId(productoDto.getId());

        // THEN
        assertNotNull(resultado);
        assertEquals(productoEntity.getId(), resultado.getId());
        assertEquals(productoEntity.getNombre(), resultado.getNombre());
        assertEquals(productoEntity.getDescripcion(), resultado.getDescripcion());
        assertTrue(productoEntity.getPrecio().compareTo(resultado.getPrecio()) == 0);
        // Verificamos que findById() fue llamado exactamente una vez con el ID correcto
        verify(productoRepository, times(1)).findById(productoDto.getId());
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException si el producto no es encontrado")
    void obtenerProductoPorId_shouldThrowExceptionWhenNotFound() {
        // GIVEN: Cuando el repositorio.findById() es llamado,
        // debe devolver un Optional vacío (producto no encontrado).
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN & THEN: Verificamos que se lanza la excepción esperada
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            productoService.obtenerProductoPorId(99L); // ID que no existe
        });

        // Verificamos el mensaje de la excepción
        assertEquals("Producto no encontrado con id 99", thrown.getMessage());
        // Verificamos que findById() fue llamado una vez
        verify(productoRepository, times(1)).findById(99L);
    }



    @Test
    @DisplayName("Debe listar todos los productos y devolver una lista de DTOs")
    void listarProductos_shouldReturnListOfProductDtos() {
        // GIVEN: Creamos una lista de entidades de producto simuladas
        // ProductoEntity NO usa @Builder, así que usamos su constructor AllArgsConstructor
        ProductoEntity producto1 = new ProductoEntity(
            1L, "Teclado", "Teclado RGB mecánico", new BigDecimal("100.00")
        );
        ProductoEntity producto2 = new ProductoEntity(
            2L, "Mouse", "Mouse inalámbrico ergonómico", new BigDecimal("50.00")
        );
        List<ProductoEntity> entities = Arrays.asList(producto1, producto2);

        // Cuando el repositorio.findAll() es llamado, debe devolver nuestra lista simulada.
        when(productoRepository.findAll()).thenReturn(entities);

        // WHEN
        List<ProductoDto> resultados = productoService.listarProductos();

        // THEN
        assertNotNull(resultados); // La lista no debe ser nula
        assertEquals(2, resultados.size()); // Debe contener 2 elementos

        // Verificamos el primer producto en la lista
        assertEquals(1L, resultados.get(0).getId());
        assertEquals("Teclado", resultados.get(0).getNombre());
        assertEquals("Teclado RGB mecánico", resultados.get(0).getDescripcion());
        assertTrue(new BigDecimal("100.00").compareTo(resultados.get(0).getPrecio()) == 0);

        // Verificamos el segundo producto en la lista
        assertEquals(2L, resultados.get(1).getId());
        assertEquals("Mouse", resultados.get(1).getNombre());
        assertEquals("Mouse inalámbrico ergonómico", resultados.get(1).getDescripcion());
        assertTrue(new BigDecimal("50.00").compareTo(resultados.get(1).getPrecio()) == 0);

        // Verificamos que findAll() del repositorio fue llamado exactamente una vez
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe devolver una lista vacía si no hay productos")
    void listarProductos_shouldReturnEmptyListIfNoProducts() {
        // GIVEN: Cuando findAll() es llamado, debe devolver una lista vacía.
        when(productoRepository.findAll()).thenReturn(Arrays.asList());

        // WHEN
        List<ProductoDto> resultados = productoService.listarProductos();

        // THEN
        assertNotNull(resultados); // La lista no debe ser nula
        assertTrue(resultados.isEmpty()); // La lista debe estar vacía
        // Verificamos que findAll() fue llamado una vez
        verify(productoRepository, times(1)).findAll();
    }
}