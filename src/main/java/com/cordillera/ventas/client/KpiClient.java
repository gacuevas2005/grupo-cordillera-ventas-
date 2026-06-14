package com.cordillera.ventas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

// 🚀 El cliente Feign viaja de vuelta al Gateway (8086)
@FeignClient(name = "kpi-service", url = "http://localhost:8086/api/kpi")
public interface KpiClient {

    @PutMapping("/acumular")
    void acumularProgresoVenta(
            @RequestParam("sucursalId") Long sucursalId,
            // 👈 Ahora enviamos un Body JSON como lo exige el ms-kpi
            @RequestBody List<Map<String, Object>> productosVendidos
    );
}