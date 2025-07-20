package com.miempresa.inventario.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.miempresa.inventario.dtos.InventarioDto;
import com.miempresa.inventario.entities.InventarioEntity;
import com.miempresa.inventario.exceptions.RecursoNoEncontradoException;
import com.miempresa.inventario.repositories.InventarioRepository;
import com.miempresa.inventario.services.InventarioService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

	private static final Logger log = LoggerFactory.getLogger(InventarioServiceImpl.class);
	
	private final InventarioRepository inventarioRepository;
	private final RestTemplate restTemplate;

	@Value("${productos.api.url}")
	private String PRODUCTOS_API_URL;

	@Value("${productos.service.api.key}")
	private String API_KEY_PRODUCTO;

	@Override
	public String consultarInventarioPorProductoId(Long productoId) {
	    log.info("Consultando inventario para productoId: {}", productoId);
	    String resultado = validarExistenciaProducto(productoId);
	    log.info("Resultado de la validación de existencia del producto: {}", resultado);
	    return resultado;
	}

	@Transactional
	@Override
	public InventarioDto actualizarCantidadInventario(Long productoId, int nuevaCantidad) {
	    log.info("Actualizando cantidad de inventario. productoId: {}, nuevaCantidad: {}", productoId, nuevaCantidad);

	    validarExistenciaProducto(productoId);

	    InventarioEntity entity = new InventarioEntity();
	    entity.setProductoId(productoId);
	    entity.setCantidad(nuevaCantidad);

	    InventarioEntity guardado = inventarioRepository.save(entity);
	    log.info("Inventario actualizado para productoId: {} con cantidad: {}", guardado.getProductoId(), guardado.getCantidad());

	    return toDto(guardado);
	}

	@Transactional
	@Override
	public InventarioDto comprarProducto(Long productoId, int cantidad) {
	    log.info("Procesando compra de productoId: {}, cantidad: {}", productoId, cantidad);

	    validarExistenciaProducto(productoId);

	    InventarioEntity inventario = inventarioRepository.findByProductoId(productoId)
	            .orElseThrow(() -> {
	                log.warn("Inventario no encontrado para productoId: {}", productoId);
	                return new RecursoNoEncontradoException("Inventario no encontrado");
	            });

	    if (inventario.getCantidad() < cantidad) {
	        log.warn("Inventario insuficiente para productoId: {}. Disponible: {}, Solicitado: {}",
	                productoId, inventario.getCantidad(), cantidad);
	        throw new IllegalArgumentException("Inventario insuficiente");
	    }

	    inventario.setCantidad(inventario.getCantidad() - cantidad);
	    inventarioRepository.save(inventario);
	    log.info("Compra realizada. Nuevo inventario para productoId {}: {}", productoId, inventario.getCantidad());

	    return toDto(inventario);
	}

	private InventarioDto toDto(InventarioEntity entity) {
	    return InventarioDto.builder()
	    		.id(entity.getId())
	            .productoId(entity.getProductoId())
	            .cantidad(entity.getCantidad())
	            .build();
	}

	protected String validarExistenciaProducto(Long productoId) {
        String url = PRODUCTOS_API_URL + "/productos/" + productoId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", API_KEY_PRODUCTO); 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        log.info("Consultando existencia de producto en: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Producto con ID {} encontrado exitosamente. Respuesta: {}", productoId, response.getBody());
                return response.getBody();
            } else {
                // Si llegamos aquí con un código no 2xx, ya se debería haber lanzado una excepción Http...Exception.
                // Esto es un respaldo, pero indica un flujo inesperado.
                log.warn("MSProducto respondió con estado inesperado (no 2xx): {}", response.getStatusCode());
                throw new RecursoNoEncontradoException("Error inesperado del servicio de productos al validar ID: " + productoId);
            }


        } catch (HttpClientErrorException e) {
            String errorMessage = "Error del cliente al consultar MSProducto (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
            log.error(errorMessage, e);
            // *** También lanzamos una excepción para otros errores 4xx ***
            throw new RecursoNoEncontradoException("Error del servicio de productos: " + errorMessage);

        } catch (HttpServerErrorException e) {
            String errorMessage = "Error del servidor al consultar MSProducto (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
            log.error(errorMessage, e);
            // *** Lanzamos una excepción para errores 5xx ***
            throw new RecursoNoEncontradoException("Error del servicio de productos (servidor): " + errorMessage);

        } catch (RestClientException e) {
            log.error("Error de conexión o comunicación con MSProducto: {}", e.getMessage(), e);
            // *** Lanzamos una excepción para problemas de conexión ***
            throw new RecursoNoEncontradoException("No se pudo conectar con el servicio de productos: " + e.getMessage());

        } catch (Exception e) {
            log.error("Ocurrió un error inesperado al validar la existencia del producto: {}", e.getMessage(), e);
            // *** Lanzamos una excepción genérica para otros errores ***
            throw new RecursoNoEncontradoException("Error interno al validar existencia de producto: " + e.getMessage());
        }
    }
	 

}