package com.cordillera.ventas.Service;

import com.cordillera.ventas.Interface.ProductoClient;
import com.cordillera.ventas.Interface.SucursalClient; // <--- Nuevo Cliente
import com.cordillera.ventas.Dto.ProductoResponseDto;
import com.cordillera.ventas.Dto.SucursalResponseDto; // <--- Nuevo DTO
import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Model.VentaModel;
import com.cordillera.ventas.Repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private SucursalClient sucursalClient; // <--- Inyectamos SucursalClient

    @Transactional
    public VentaResponseDto crearVenta(VentaRequestDto dto) {

        // 1. VALIDAR SUCURSAL (Microservicio externo puerto 8083)
        SucursalResponseDto sucursal;
        try {
            sucursal = sucursalClient.obtenerSucursalPorId(dto.getSucursalId());
        } catch (Exception e) {
            throw new RuntimeException("Error: La sucursal " + dto.getSucursalId() + " no existe o el servicio de sucursales está caído.");
        }

        // 2. VALIDAR PRODUCTO (Microservicio externo puerto 8081)
        ProductoResponseDto producto;
        try {
            producto = productoClient.obtenerProductoPorId(dto.getProductoId());
        } catch (Exception e) {
            throw new RuntimeException("Error: El producto no existe o el catálogo de productos está caído.");
        }

        // 3. Lógica de Negocio: Calcular monto total con el precio oficial
        Double montoCalculado = producto.getPrecio() * dto.getCantidad();

        // 4. Mapear y guardar la Venta
        VentaModel entity = new VentaModel();

        // ¡OJO AQUÍ!: En tu VentaModel, el campo ahora debe ser 'Long sucursalId'
        entity.setSucursalId(dto.getSucursalId());
        entity.setProductoId(dto.getProductoId());

        entity.setOrigen(dto.getOrigen());
        entity.setCantidad(dto.getCantidad());
        entity.setMontoTotal(montoCalculado);
        entity.setFechaVenta(LocalDateTime.now());

        VentaModel guardado = ventaRepository.save(entity);

        // Devolvemos el DTO usando los nombres que nos dieron los otros microservicios
        return entityToDto(guardado, producto.getNombre(), producto.getSku(), sucursal.getNombre());
    }

    public List<VentaResponseDto> listarVentas() {
        return ventaRepository.findAll()
                .stream()
                .map(venta -> {
                    // En un listado real, podrías llamar a los otros servicios
                    // para traer los nombres, por ahora devolvemos el ID o "Cargando..."
                    return entityToDto(venta, "Producto ID: " + venta.getProductoId(), "N/A", "Sucursal ID: " + venta.getSucursalId());
                })
                .collect(Collectors.toList());
    }

    // Mapeo refinado: recibe los nombres externos para el JSON final
    private VentaResponseDto entityToDto(VentaModel entity, String nombreProd, String skuProd, String nombreSuc) {
        VentaResponseDto dto = new VentaResponseDto();
        dto.setId(entity.getId());
        dto.setNombreSucursal(nombreSuc); // <--- Nombre que viene del microservicio
        dto.setNombreProducto(nombreProd); // <--- Nombre que viene del microservicio
        dto.setSkuProducto(skuProd);
        dto.setOrigen(entity.getOrigen());
        dto.setCantidad(entity.getCantidad());
        dto.setMontoTotal(entity.getMontoTotal());
        dto.setFechaFormateada(entity.getFechaVenta().toString());
        return dto;
    }
    public List<VentaResponseDto> listarPorOrigen(String origen) {
        return ventaRepository.findByOrigen(origen)
                .stream()
                .map(venta -> {
                    // Aquí podrías llamar a Feign si quisieras los nombres reales,
                    // o por ahora devolver los IDs para que el reporte sea rápido.
                    return entityToDto(venta, "Producto ID: " + venta.getProductoId(), "N/A", "Sucursal ID: " + venta.getSucursalId());
                })
                .collect(Collectors.toList());
    }
}