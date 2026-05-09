package com.cordillera.ventas.Interface;


import com.cordillera.ventas.Dto.SucursalResponseDto; // Debes crear este DTO sencillo
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "sucursal-service", url = "http://localhost:8084/api/sucursales")
public interface SucursalClient {

    @GetMapping("/{id}")
    SucursalResponseDto obtenerSucursalPorId(@PathVariable("id") Long id);
}
