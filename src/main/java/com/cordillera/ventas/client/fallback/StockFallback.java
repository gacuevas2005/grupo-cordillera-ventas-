package com.cordillera.ventas.client.fallback;

import com.cordillera.ventas.client.StockClient;
import com.cordillera.ventas.Dto.StockResponseDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class StockFallback implements StockClient {

    @Override
    public List<StockResponseDto> obtenerPorProducto(Long productoId) {
        // Si el micro de stock cae, devolvemos una lista vacía.
        // La lógica de Ventas interpretará esto como "No hay stock".
        return Collections.emptyList();
    }

    @Override
    public StockResponseDto consumirStock(Long productoId, Long sucursalId, Integer cantidad) {
        // Lanzamos el error para abortar la transacción de venta
        throw new RuntimeException("Transacción cancelada: El servicio de Stock no responde.");
    }
}