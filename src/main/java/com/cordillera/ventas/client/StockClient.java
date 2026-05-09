package com.cordillera.ventas.client;

import com.cordillera.ventas.client.fallback.StockFallback;
import com.cordillera.ventas.Dto.StockResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "stock-service",
        url = "http://localhost:8085/api/stock",
        fallback = StockFallback.class
)
public interface StockClient {

    // Cambiamos al endpoint que SÍ EXISTE en el micro de Stock (tu método 3)
    @GetMapping("/producto/{productoId}")
    List<StockResponseDto> obtenerPorProducto(@PathVariable("productoId") Long productoId);

    // Ajustamos para que reciba el DTO que envía el micro de Stock (tu método 2)
    @PutMapping("/producto/{productoId}/sucursal/{sucursalId}/consumir")
    StockResponseDto consumirStock(
            @PathVariable("productoId") Long productoId,
            @PathVariable("sucursalId") Long sucursalId,
            @RequestParam("cantidad") Integer cantidad);
}