package com.cordillera.ventas.Service;

import com.cordillera.ventas.Interface.ProductoClient;
import com.cordillera.ventas.Interface.SucursalClient;
import com.cordillera.ventas.Interface.StockClient;
import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
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
     * Crea una venta validando Producto, Sucursal y descontando Stock.
     * Si falla el stock, la venta no se guarda (Rollback).
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

        // 3. Mapear y Guardar la Venta en la base de datos de Ventas
        VentaModel venta = new VentaModel();
        venta.setProductoId(dto.getProductoId());
        venta.setSucursalId(dto.getSucursalId());
        venta.setCantidad(dto.getCantidad());
        venta.setOrigen(dto.getOrigen());
        venta.setMontoTotal(dto.getMontoTotal());
        venta.setFechaVenta(LocalDateTime.now());

        VentaModel ventaGuardada = ventaRepository.save(venta);

        // 4. Integración con Stock: Descontar las unidades vendidas
        // Si el microservicio de Stock lanza error (ej: no hay suficientes),
        // @Transactional cancelará el guardado de la venta automáticamente.
        stockClient.consumirStock(
                ventaGuardada.getProductoId(),
                ventaGuardada.getSucursalId(),
                ventaGuardada.getCantidad()
        );

        // 5. Devolver respuesta con datos enriquecidos
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
     * Mapeador de Entidad a DTO para asegurar consistencia.
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