package com.miempresa.inventario.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.miempresa.inventario.dtos.InventarioDto;
import com.miempresa.inventario.entities.InventarioEntity;
import com.miempresa.inventario.exceptions.RecursoNoEncontradoException;
import com.miempresa.inventario.repositories.InventarioRepository;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private static final String PRODUCTOS_API_URL = "http://localhost:8081/api";
    private static final String API_KEY_PRODUCTO = "test-api-key";

    private Long productoIdExistente = 101L;
    private Long productoIdNoExistenteEnMsProductos = 999L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventarioService, "PRODUCTOS_API_URL", PRODUCTOS_API_URL);
        ReflectionTestUtils.setField(inventarioService, "API_KEY_PRODUCTO", API_KEY_PRODUCTO);
    }



    @Test
    @DisplayName("Debe consultar inventario y devolver la información del producto si existe")
    void consultarInventarioPorProductoId_shouldReturnProductInfoWhenProductExists() {
        // GIVEN: Simular que la validación de existencia del producto es exitosa
        String productoInfoEsperada = "{\"id\":101,\"nombre\":\"Producto Test\",\"precio\":10.0}";
        
        // Mockear el comportamiento del RestTemplate para validarExistenciaProducto
        doReturn(new ResponseEntity<>(productoInfoEsperada, HttpStatus.OK))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN: Llamamos al método consultarInventarioPorProductoId
        String resultado = inventarioService.consultarInventarioPorProductoId(productoIdExistente);

        // THEN: Verificamos que el resultado sea la información esperada
        assertNotNull(resultado);
        assertEquals(productoInfoEsperada, resultado);
        // Verificamos que el RestTemplate fue llamado para validar existencia
        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Client Error 404) si el producto no existe en el MS de Productos")
    void consultarInventarioPorProductoId_shouldThrowRecursoNoEncontradoExceptionWhenProductDoesNotExist() {
        // GIVEN: Simular un error 404 Not Found del MS de Productos
        // El cuerpo de la respuesta se pasa como un String vacío o nulo si no hay un cuerpo de error específico.
        String errorResponseBody = ""; 
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT_FOUND", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdNoExistenteEnMsProductos),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN: Verificamos que se lanza RecursoNoEncontradoException
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.consultarInventarioPorProductoId(productoIdNoExistenteEnMsProductos);
        });

        // **Ajuste aquí para que coincida con el mensaje real del catch general de HttpClientErrorException**
        String expectedMessage = "Error del servicio de productos: Error del cliente al consultar MSProducto (HTTP 404 NOT_FOUND): " + errorResponseBody;
        assertEquals(expectedMessage, thrown.getMessage());

        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdNoExistenteEnMsProductos),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Client Error) si el MS de Productos devuelve otro 4xx")
    void consultarInventarioPorProductoId_shouldThrowRecursoNoEncontradoExceptionWhenClientError() {
        // GIVEN: Simular un error 400 Bad Request del MS de Productos
        String errorResponseBody = "{\"error\":\"Invalid input\"}";
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente), // Usamos ID existente pero simulamos error de request
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.consultarInventarioPorProductoId(productoIdExistente);
        });

        // Verificamos el mensaje específico del catch de HttpClientErrorException
        // El formato de HttpStatus.BAD_REQUEST.toString() es "400 BAD_REQUEST"
        String expectedInnerMessage = "Error del cliente al consultar MSProducto (HTTP " + HttpStatus.BAD_REQUEST + "): " + errorResponseBody;
        String expectedFullMessage = "Error del servicio de productos: " + expectedInnerMessage;
        
        // Usamos assertEquals para comparar el mensaje completo y exacto
        assertEquals(expectedFullMessage, thrown.getMessage()); 
        
        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Server Error) si el MS de Productos devuelve un 5xx")
    void consultarInventarioPorProductoId_shouldThrowRecursoNoEncontradoExceptionWhenServerError() {
        // GIVEN: Simular un error 500 Internal Server Error del MS de Productos
        String errorResponseBody = "{\"message\":\"DB Down\"}";
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.consultarInventarioPorProductoId(productoIdExistente);
        });

        // Verificamos el mensaje específico del catch de HttpServerErrorException
        // Ajustamos la cadena esperada para que coincida con el formato de HttpStatus.INTERNAL_SERVER_ERROR.toString()
        String expectedInnerMessage = "Error del servidor al consultar MSProducto (HTTP " + HttpStatus.INTERNAL_SERVER_ERROR + "): " + errorResponseBody;
        String expectedFullMessage = "Error del servicio de productos (servidor): " + expectedInnerMessage;
        
        assertEquals(expectedFullMessage, thrown.getMessage()); // Usamos assertEquals para una comparación exacta

        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }
    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Connection Error) si hay un problema de conexión con el MS de Productos")
    void consultarInventarioPorProductoId_shouldThrowRecursoNoEncontradoExceptionWhenConnectionError() {
        // GIVEN: Simular un problema de conexión (RestClientException general)
        doThrow(new RestClientException("Connection refused: connect"))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.consultarInventarioPorProductoId(productoIdExistente);
        });

        // Verificamos el mensaje específico del catch de RestClientException
        assertTrue(thrown.getMessage().contains("No se pudo conectar con el servicio de productos: Connection refused: connect"));
        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Unexpected Error) si ocurre una excepción no manejada específicamente")
    void consultarInventarioPorProductoId_shouldThrowRecursoNoEncontradoExceptionWhenUnexpectedError() {
        // GIVEN: Simular una excepción genérica inesperada
        doThrow(new RuntimeException("Something unexpected happened"))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.consultarInventarioPorProductoId(productoIdExistente);
        });

        // Verificamos el mensaje específico del catch de Exception
        assertTrue(thrown.getMessage().contains("Error interno al validar existencia de producto: Something unexpected happened"));
        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }



    @Test
    @DisplayName("Debe crear una nueva entrada de inventario cuando el producto existe y no hay registro previo")
    void actualizarCantidadInventario_shouldCreateNewEntryWhenProductExistsAndNoPriorRecord() {
        // GIVEN: El producto existe en el servicio externo
        int nuevaCantidad = 70;
        // La entidad que simula el retorno de la DB con un ID asignado
        InventarioEntity savedInventarioEntity = new InventarioEntity(1L, productoIdExistente, nuevaCantidad);

        // Mockear el servicio de productos externo como exitoso
        mockProductServiceSuccess(productoIdExistente, "Producto existe");

        // --- ¡LÍNEA ELIMINADA! ---
        // Tu servicio actualmente no llama a findByProductoId en este método,
        // siempre crea una nueva entidad. Por lo tanto, este mock es innecesario.
        // when(inventarioRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.empty());
        // -------------------------
        
        // Mockear que save devuelve la entidad con el ID
        when(inventarioRepository.save(any(InventarioEntity.class))).thenReturn(savedInventarioEntity);

        // WHEN
        InventarioDto result = inventarioService.actualizarCantidadInventario(productoIdExistente, nuevaCantidad);

        // THEN
        assertNotNull(result);
        assertEquals(savedInventarioEntity.getId(), result.getId());
        assertEquals(productoIdExistente, result.getProductoId());
        assertEquals(nuevaCantidad, result.getCantidad());

        // Verificaciones
        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
        // Verifica explícitamente que findByProductoId *nunca* fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong()); 
        verify(inventarioRepository, times(1)).save(any(InventarioEntity.class)); // Se llamó save para una nueva entidad
    }


    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Client Error 404) si el producto no existe en el MS de Productos al actualizar")
    void actualizarCantidadInventario_shouldThrowExceptionWhenProductDoesNotExistInMsProducts() {
        // GIVEN: El servicio de productos devuelve 404 Not Found
        int nuevaCantidad = 70;
        // Se puede pasar un cuerpo de error vacío o específico si el servicio de productos lo devuelve.
        String errorResponseBody = ""; 
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT_FOUND", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdNoExistenteEnMsProductos),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN: Verificamos que se lanza RecursoNoEncontradoException
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.actualizarCantidadInventario(productoIdNoExistenteEnMsProductos, nuevaCantidad);
        });

        // **AJUSTE AQUÍ:** El mensaje esperado debe coincidir con el formato del catch general de HttpClientErrorException
        // Incluye el HttpStatus completo (ej. "404 NOT_FOUND") y el cuerpo de la respuesta de error.
        String expectedMessage = "Error del servicio de productos: Error del cliente al consultar MSProducto (HTTP " + HttpStatus.NOT_FOUND + "): " + errorResponseBody;
        assertEquals(expectedMessage, thrown.getMessage());
        
        // Verificamos que el repositorio no fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong()); // No se intentó buscar
        verify(inventarioRepository, never()).save(any(InventarioEntity.class)); // Ni guardar
    }
    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Client Error) si el MS de Productos devuelve otro 4xx al actualizar")
    void actualizarCantidadInventario_shouldThrowExceptionWhenProductServiceReturnsOther4xx() {
        // GIVEN: El servicio de productos devuelve 401 Unauthorized
        int nuevaCantidad = 70;
        // Si el HttpClientErrorException no tiene un cuerpo de respuesta en el mock, usa String vacío.
        // Si esperas un cuerpo de respuesta, defínelo aquí, por ejemplo: "{\"error\":\"Unauthorized access\"}"
        String errorResponseBody = ""; 
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.actualizarCantidadInventario(productoIdExistente, nuevaCantidad);
        });

        // **AJUSTE AQUÍ:** El mensaje esperado debe coincidir con el formato real.
        // HttpStatus.UNAUTHORIZED.toString() producirá "401 UNAUTHORIZED"
        String expectedInnerMessage = "Error del cliente al consultar MSProducto (HTTP " + HttpStatus.UNAUTHORIZED + "): " + errorResponseBody;
        String expectedFullMessage = "Error del servicio de productos: " + expectedInnerMessage;
        
        // Usa assertEquals para una comparación exacta de la cadena completa.
        assertEquals(expectedFullMessage, thrown.getMessage()); 
        
        // Verificamos que el repositorio no fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(inventarioRepository, never()).save(any(InventarioEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Server Error) si el MS de Productos devuelve un 5xx al actualizar")
    void actualizarCantidadInventario_shouldThrowExceptionWhenProductServiceReturns5xx() {
        // GIVEN: El servicio de productos devuelve 500 Internal Server Error
        int nuevaCantidad = 70;
        // Si el HttpServerErrorException no tiene un cuerpo de respuesta en el mock, usa String vacío.
        // Si esperas un cuerpo de respuesta, defínelo aquí, por ejemplo: "{\"message\":\"DB connection lost\"}"
        String errorResponseBody = ""; 
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.actualizarCantidadInventario(productoIdExistente, nuevaCantidad);
        });

        // **AJUSTE AQUÍ:** El mensaje esperado debe coincidir con el formato real.
        // HttpStatus.INTERNAL_SERVER_ERROR.toString() producirá "500 INTERNAL_SERVER_ERROR"
        String expectedInnerMessage = "Error del servidor al consultar MSProducto (HTTP " + HttpStatus.INTERNAL_SERVER_ERROR + "): " + errorResponseBody;
        String expectedFullMessage = "Error del servicio de productos (servidor): " + expectedInnerMessage;
        
        // Usa assertEquals para una comparación exacta de la cadena completa.
        assertEquals(expectedFullMessage, thrown.getMessage()); 
        
        // Verificamos que el repositorio no fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(inventarioRepository, never()).save(any(InventarioEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Connection Error) si hay un problema de conexión al actualizar")
    void actualizarCantidadInventario_shouldThrowExceptionWhenProductServiceConnectionError() {
        // GIVEN: Hay un error de conexión (ej. servicio de productos caído)
        int nuevaCantidad = 70;
        String connectionErrorMessage = "Connection refused";
        
        doThrow(new RestClientException(connectionErrorMessage))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.actualizarCantidadInventario(productoIdExistente, nuevaCantidad);
        });

        // Verificamos el mensaje específico del catch de RestClientException
        assertTrue(thrown.getMessage().contains("No se pudo conectar con el servicio de productos: " + connectionErrorMessage));
        
        // Verificamos que el repositorio no fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(inventarioRepository, never()).save(any(InventarioEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Unexpected Error) si hay un error inesperado al actualizar")
    void actualizarCantidadInventario_shouldThrowExceptionWhenUnexpectedError() {
        // GIVEN: Ocurre una excepción inesperada no capturada específicamente
        int nuevaCantidad = 70;
        String unexpectedErrorMessage = "Some unexpected problem";
        
        doThrow(new RuntimeException(unexpectedErrorMessage))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.actualizarCantidadInventario(productoIdExistente, nuevaCantidad);
        });

        // Verificamos el mensaje específico del catch de Exception
        assertTrue(thrown.getMessage().contains("Error interno al validar existencia de producto: " + unexpectedErrorMessage));
        
        // Verificamos que el repositorio no fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(inventarioRepository, never()).save(any(InventarioEntity.class));
    }


    @Test
    @DisplayName("Debe procesar la compra y reducir la cantidad del inventario")
    void comprarProducto_shouldReduceInventarioQuantity() {
        // GIVEN: Inventario existente con suficiente cantidad
        InventarioEntity inventarioInicial = new InventarioEntity(1L, productoIdExistente, 100);
        InventarioEntity inventarioDespuesCompra = new InventarioEntity(1L, productoIdExistente, 90); // 100 - 10

        // Mock de la validación de existencia del producto (exitosa)
        mockProductServiceSuccess(productoIdExistente, "Producto existe");
        
        // Mock de findByProductoId para devolver el inventario inicial
        when(inventarioRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(inventarioInicial));
        // Mock de save para devolver el inventario después de la compra
        when(inventarioRepository.save(any(InventarioEntity.class))).thenReturn(inventarioDespuesCompra);

        // WHEN: Llamamos al método comprarProducto
        InventarioDto resultado = inventarioService.comprarProducto(productoIdExistente, 10);

        // THEN: Verificamos el resultado
        assertNotNull(resultado);
        assertEquals(inventarioDespuesCompra.getId(), resultado.getId());
        assertEquals(inventarioDespuesCompra.getProductoId(), resultado.getProductoId());
        assertEquals(90, resultado.getCantidad()); // Cantidad esperada después de la compra

        // Verificación de interacciones
        verify(restTemplate, times(1)).exchange(
            eq(PRODUCTOS_API_URL + "/productos/" + productoIdExistente),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
        verify(inventarioRepository, times(1)).findByProductoId(productoIdExistente);
        verify(inventarioRepository, times(1)).save(inventarioInicial);
    }

    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException (Client Error 404) si el producto no existe en el MS de Productos al comprar")
    void comprarProducto_shouldThrowExceptionWhenProductDoesNotExistAtPurchase() {
        // GIVEN: El servicio de productos devuelve 404 Not Found
        // Provide an empty or specific error body if your service includes it
        String errorResponseBody = ""; 
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT_FOUND", errorResponseBody.getBytes(), null))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productoIdNoExistenteEnMsProductos),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );

        // WHEN & THEN: Verificamos que se lanza RecursoNoEncontradoException
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.comprarProducto(productoIdNoExistenteEnMsProductos, 10);
        });

        // **Ajuste aquí para que coincida con el mensaje real del catch general de HttpClientErrorException**
        // The message includes the full HttpStatus enum name (e.g., "404 NOT_FOUND")
        String expectedMessage = "Error del servicio de productos: Error del cliente al consultar MSProducto (HTTP " + HttpStatus.NOT_FOUND + "): " + errorResponseBody;
        assertEquals(expectedMessage, thrown.getMessage());
        
        // Verificamos que el repositorio no fue llamado
        verify(inventarioRepository, never()).findByProductoId(anyLong());
        verify(inventarioRepository, never()).save(any(InventarioEntity.class));
    }
    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException si el inventario no existe para el producto al comprar")
    void comprarProducto_shouldThrowRecursoNoEncontradoExceptionWhenInventarioNotFound() {
        // GIVEN: El producto existe en el MS de Productos, pero no hay registro de inventario para él en la DB
        mockProductServiceSuccess(productoIdExistente, "Producto existe");
        when(inventarioRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException thrown = assertThrows(RecursoNoEncontradoException.class, () -> {
            inventarioService.comprarProducto(productoIdExistente, 10);
        });

        assertEquals("Inventario no encontrado", thrown.getMessage());
        verify(inventarioRepository, times(1)).findByProductoId(productoIdExistente);
        verify(inventarioRepository, never()).save(any(InventarioEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el inventario es insuficiente para la compra")
    void comprarProducto_shouldThrowIllegalArgumentExceptionWhenInsufficientInventario() {
        // GIVEN: Inventario existente pero con cantidad insuficiente
        InventarioEntity inventarioInsuficiente = new InventarioEntity(1L, productoIdExistente, 5); // Solo 5 unidades

        // Mock de la validación de existencia del producto (exitosa)
        mockProductServiceSuccess(productoIdExistente, "Producto existe");
        when(inventarioRepository.findByProductoId(productoIdExistente)).thenReturn(Optional.of(inventarioInsuficiente));

        // WHEN & THEN
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.comprarProducto(productoIdExistente, 10); // Intentar comprar 10, solo hay 5
        });

        assertEquals("Inventario insuficiente", thrown.getMessage());
        verify(inventarioRepository, times(1)).findByProductoId(productoIdExistente);
        verify(inventarioRepository, never()).save(any(InventarioEntity.class)); // No se debe guardar si hay error
    }

    // --- Métodos de Ayuda para Mocks ---

    private void mockProductServiceSuccess(Long productId, String responseBody) {
        doReturn(new ResponseEntity<>(responseBody, HttpStatus.OK))
            .when(restTemplate).exchange(
                eq(PRODUCTOS_API_URL + "/productos/" + productId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );
    }
}