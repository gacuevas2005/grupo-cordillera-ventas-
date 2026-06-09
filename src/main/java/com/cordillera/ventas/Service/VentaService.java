package com.cordillera.ventas.Service;

import com.cordillera.ventas.client.ProductoClient;
import com.cordillera.ventas.client.SucursalClient;
import com.cordillera.ventas.client.StockClient;
import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Dto.StockResponseDto;
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

    @Transactional
    public VentaResponseDto crearVenta(VentaRequestDto dto) {
        // 1. Validaciones de existencia
        var producto = productoClient.obtenerProductoPorId(dto.getProductoId());
        if (producto == null) throw new IllegalArgumentException("El producto seleccionado no existe en la base de datos.");

        var sucursal = sucursalClient.obtenerSucursalPorId(dto.getSucursalId());
        if (sucursal == null) throw new IllegalArgumentException("La sucursal seleccionada no existe.");

        // 🌟 VALIDACIÓN DE PRECIO
        Double valorEsperado = producto.getPrecio() * dto.getCantidad();
        if (dto.getMontoTotal() < valorEsperado) {
            throw new IllegalArgumentException("Monto insuficiente. El valor a pagar por " + dto.getCantidad() + "x " + producto.getNombre() + " es de $" + valorEsperado);
        }

        // 2. Validación de Stock
        List<StockResponseDto> stocks = stockClient.obtenerPorProducto(dto.getProductoId());
        StockResponseDto stockEnSucursal = stocks.stream()
                .filter(s -> s.getSucursalId().equals(dto.getSucursalId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay registros de inventario para este producto en la sucursal seleccionada."));

        if (stockEnSucursal.getCantidadDisponible() < dto.getCantidad()) {
            throw new IllegalArgumentException("Stock insuficiente. Solo quedan " + stockEnSucursal.getCantidadDisponible() + " unidades disponibles.");
        }

        // 3. Persistencia (El resto del código hacia abajo queda exactamente igual...)
        VentaModel venta = new VentaModel();
        venta.setProductoId(dto.getProductoId());
        venta.setSucursalId(dto.getSucursalId());
        venta.setCantidad(dto.getCantidad());
        venta.setOrigen(dto.getOrigen());
        // Puedes usar el dto.getMontoTotal() o forzar directamente valorEsperado para ser más estricto
        venta.setMontoTotal(dto.getMontoTotal());
        venta.setFechaVenta(LocalDateTime.now());

        VentaModel ventaGuardada = ventaRepository.save(venta);

        // 4. Consumo de Stock
        stockClient.consumirStock(ventaGuardada.getProductoId(), ventaGuardada.getSucursalId(), ventaGuardada.getCantidad());

        // 5. Respuesta optimizada
        VentaResponseDto response = mapearBase(ventaGuardada);
        response.setNombreProducto(producto.getNombre());
        response.setSkuProducto(producto.getSku());
        response.setNombreSucursal(sucursal.getNombre());

        return response;
    }

    public List<VentaResponseDto> listarVentas() {
        return ventaRepository.findAll().stream()
                .map(this::mapToResponseDtoEnriched)
                .collect(Collectors.toList());
    }

    public List<VentaResponseDto> listarPorOrigen(String origen) {
        return ventaRepository.findByOrigen(origen).stream()
                .map(this::mapToResponseDtoEnriched)
                .collect(Collectors.toList());
    }

    private VentaResponseDto mapearBase(VentaModel entity) {
        VentaResponseDto response = new VentaResponseDto();
        response.setId(entity.getId());
        response.setProductoId(entity.getProductoId());
        response.setSucursalId(entity.getSucursalId());
        response.setCantidad(entity.getCantidad());
        response.setMontoTotal(entity.getMontoTotal());
        response.setOrigen(entity.getOrigen());
        response.setFechaVenta(entity.getFechaVenta());
        response.setFechaFormateada(entity.getFechaVenta().toString());
        return response;
    }

    private VentaResponseDto mapToResponseDtoEnriched(VentaModel entity) {
        VentaResponseDto response = mapearBase(entity);
        try {
            var prod = productoClient.obtenerProductoPorId(entity.getProductoId());
            if (prod != null) {
                response.setNombreProducto(prod.getNombre());
                response.setSkuProducto(prod.getSku());
            }
            var suc = sucursalClient.obtenerSucursalPorId(entity.getSucursalId());
            if (suc != null) {
                response.setNombreSucursal(suc.getNombre());
            }
        } catch (Exception e) {
            response.setNombreProducto("Cargando...");
            response.setNombreSucursal("Sucursal " + entity.getSucursalId());
        }
        return response;
    }
}