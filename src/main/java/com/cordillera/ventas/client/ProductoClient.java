package com.cordillera.ventas.client;

import com.cordillera.ventas.Dto.ProductoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// El nombre debe coincidir con el del microservicio de productos
// La URL es donde está corriendo ese microservicio (puerto 8081)
@FeignClient(name = "producto-service", url = "http://localhost:8082/api/productos")
public interface ProductoClient {

    @GetMapping("/{id}")
    ProductoResponseDto obtenerProductoPorId(@PathVariable("id") Long id);
}