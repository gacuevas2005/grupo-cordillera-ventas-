package com.cordillera.ventas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 🚀 El cliente Feign viaja de vuelta al Gateway (8086)
@FeignClient(name = "kpi-service", url = "http://localhost:8086/api/kpi")
public interface KpiClient {

    @PutMapping("/acumular") // Ruta final: http://localhost:8086/api/kpi/acumular
    void acumularProgresoVenta(
            @RequestParam("sucursalId") Long sucursalId,
            @RequestParam("cantidad") Integer cantidad
    );
}