package com.cordillera.ventas.Interface;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "stock-service", url = "http://localhost:8085/api/stock")
public interface StockClient {
    @PutMapping("/producto/{productoId}/sucursal/{sucursalId}/consumir")
    void consumirStock(@PathVariable Long productoId,
                       @PathVariable Long sucursalId,
                       @RequestParam Integer cantidad);
}