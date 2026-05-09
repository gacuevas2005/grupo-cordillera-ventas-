package com.cordillera.ventas.Service;

import com.cordillera.ventas.client.ProductoClient;
import com.cordillera.ventas.client.SucursalClient;
import com.cordillera.ventas.client.StockClient;
import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Dto.StockResponseDto; // Asegúrate de importar el DTO de Stock
import com.cordillera.ventas.Model.VentaModel;
import com.cordillera.ventas.Repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoClient productoClient;

    @Autowired
    private SucursalClient sucursalClient;

    @Autowired
    private StockClient stockClient;

    /**
     * Crea una venta validando Producto, Sucursal y descontando Stock de forma proactiva.
     */
    @Transactional
    public VentaResponseDto crearVenta(VentaRequestDto dto) {

        // 1. Validar existencia del Producto y obtener sus datos (Nombre, SKU)
        var producto = productoClient.obtenerProductoPorId(dto.getProductoId());
        if (producto == null) {
            throw new RuntimeException("Error: El producto " + dto.getProductoId() + " no existe.");
        }

        // 2. Validar existencia de la Sucursal
        var sucursal = sucursalClient.obtenerSucursalPorId(dto.getSucursalId());
        if (sucursal == null) {
            throw new RuntimeException("Error: La sucursal " + dto.getSucursalId() + " no existe.");
        }

        // --- 3. VALIDACIÓN PROACTIVA DE STOCK (Fail-Fast) ---
        // Consultamos TODO el stock de ese producto
        List<StockResponseDto> stocks = stockClient.obtenerPorProducto(dto.getProductoId());

        // Filtramos para encontrar el inventario específico en la sucursal solicitada
        StockResponseDto stockEnSucursal = stocks.stream()
                .filter(s -> s.getSucursalId().equals(dto.getSucursalId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay inventario registrado para este producto en la sucursal seleccionada."));

        // Validamos que haya cantidad suficiente
        if (stockEnSucursal.getCantidadDisponible() < dto.getCantidad()) {
            throw new RuntimeException("Stock insuficiente: Disponibles " +
                    stockEnSucursal.getCantidadDisponible() + ", solicitados " + dto.getCantidad());
        }
        // ----------------------------------------------------

        // 4. Mapear y Guardar la Venta en la base de datos de Ventas (Solo llegamos aquí si hay stock validado)
        VentaModel venta = new VentaModel();
        venta.setProductoId(dto.getProductoId());
        venta.setSucursalId(dto.getSucursalId());
        venta.setCantidad(dto.getCantidad());
        venta.setOrigen(dto.getOrigen());
        venta.setMontoTotal(dto.getMontoTotal());
        venta.setFechaVenta(LocalDateTime.now());

        VentaModel ventaGuardada = ventaRepository.save(venta);

        // 5. Integración con Stock: Consumir las unidades
        // Si el microservicio de Stock falla en este punto (ej: Circuit Breaker abierto),
        // se lanzará una RuntimeException desde el Fallback y @Transactional cancelará la venta.
        stockClient.consumirStock(
                ventaGuardada.getProductoId(),
                ventaGuardada.getSucursalId(),
                ventaGuardada.getCantidad()
        );

        // 6. Devolver respuesta con datos enriquecidos (Composición)
        VentaResponseDto response = mapToResponseDto(ventaGuardada);
        response.setNombreProducto(producto.getNombre()); // Viene del microservicio Productos
        response.setSkuProducto(producto.getSku());       // Viene del microservicio Productos
        response.setNombreSucursal(sucursal.getNombre()); // Viene del microservicio Sucursales

        return response;
    }

    /**
     * Lista todas las ventas registradas.
     */
    public List<VentaResponseDto> listarVentas() {
        return ventaRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Filtra ventas por origen (WEB, PRESENCIAL, etc.)
     */
    public List<VentaResponseDto> listarPorOrigen(String origen) {
        return ventaRepository.findByOrigen(origen)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Mapeador interno de Entidad a DTO para asegurar consistencia.
     */
    private VentaResponseDto mapToResponseDto(VentaModel entity) {
        VentaResponseDto response = new VentaResponseDto();
        response.setId(entity.getId());
        response.setProductoId(entity.getProductoId());
        response.setSucursalId(entity.getSucursalId());
        response.setCantidad(entity.getCantidad());
        response.setMontoTotal(entity.getMontoTotal());
        response.setOrigen(entity.getOrigen());
        response.setFechaVenta(entity.getFechaVenta());

        // Formateo manual de fecha para el campo String (opcional)
        if (entity.getFechaVenta() != null) {
            response.setFechaFormateada(entity.getFechaVenta().toString());
        }

        return response;
    }
}