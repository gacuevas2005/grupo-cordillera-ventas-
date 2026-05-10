package com.cordillera.ventas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cordillera.ventas.Dto.*;
import com.cordillera.ventas.Model.VentaModel;
import com.cordillera.ventas.Repository.VentaRepository;
import com.cordillera.ventas.Service.VentaService;
import com.cordillera.ventas.client.ProductoClient;
import com.cordillera.ventas.client.StockClient;
import com.cordillera.ventas.client.SucursalClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoClient productoClient;

    @Mock
    private SucursalClient sucursalClient;

    @Mock
    private StockClient stockClient;

    @InjectMocks
    private VentaService ventaService;

    private VentaRequestDto requestDto;
    private VentaModel ventaGuardada;
    private ProductoResponseDto mockProducto;
    private SucursalResponseDto mockSucursal;

    @BeforeEach
    void setUp() {
        // 1. Configuramos el Request que enviaría el Frontend
        requestDto = new VentaRequestDto();
        requestDto.setProductoId(1L);
        requestDto.setSucursalId(1L);
        requestDto.setCantidad(2);
        requestDto.setMontoTotal(50000.0);
        requestDto.setOrigen("WEB");

        // 2. Configuramos el Modelo que devolvería la base de datos
        ventaGuardada = new VentaModel();
        ventaGuardada.setId(100L);
        ventaGuardada.setProductoId(1L);
        ventaGuardada.setSucursalId(1L);
        ventaGuardada.setCantidad(2);
        ventaGuardada.setMontoTotal(50000.0);
        ventaGuardada.setOrigen("WEB");
        ventaGuardada.setFechaVenta(LocalDateTime.now());

        // 3. Objetos de respuesta de otros microservicios
        mockProducto = new ProductoResponseDto();
        mockProducto.setId(1L);
        mockProducto.setNombre("Notebook Corporativo i7");
        mockProducto.setSku("NB-PRO-001");

        mockSucursal = new SucursalResponseDto();
        mockSucursal.setId(1L);
        mockSucursal.setNombre("Santiago Centro");
    }

    @Test
    void cuandoCrearVenta_entoncesRetornaVentaExitosaYConsumeStock() {
        // GIVEN
        when(ventaRepository.save(any(VentaModel.class))).thenReturn(ventaGuardada);
        when(productoClient.obtenerProductoPorId(1L)).thenReturn(mockProducto);
        when(sucursalClient.obtenerSucursalPorId(1L)).thenReturn(mockSucursal);

        StockResponseDto stockPrevio = new StockResponseDto();
        stockPrevio.setSucursalId(1L);
        stockPrevio.setCantidadDisponible(10);
        when(stockClient.obtenerPorProducto(1L)).thenReturn(List.of(stockPrevio));

        // Aquí aplicamos la corrección:
        StockResponseDto stockPostConsumo = new StockResponseDto();
        when(stockClient.consumirStock(anyLong(), anyLong(), anyInt())).thenReturn(stockPostConsumo);

        // WHEN
        VentaResponseDto resultado = ventaService.crearVenta(requestDto);

        // THEN
        assertNotNull(resultado);
        verify(stockClient, times(1)).consumirStock(1L, 1L, 2);
    }
    @Test
    void cuandoStockInsuficiente_entoncesLanzaExcepcion() {
        // GIVEN: El stock solo tiene 1 unidad, pero pedimos 2
        StockResponseDto stockPoco = new StockResponseDto();
        stockPoco.setProductoId(1L);
        stockPoco.setSucursalId(1L);
        stockPoco.setCantidadDisponible(1);

        when(productoClient.obtenerProductoPorId(1L)).thenReturn(mockProducto);
        when(sucursalClient.obtenerSucursalPorId(1L)).thenReturn(mockSucursal);
        when(stockClient.obtenerPorProducto(1L)).thenReturn(List.of(stockPoco));

        // WHEN & THEN: Verificamos que lance la excepción y no guarde nada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ventaService.crearVenta(requestDto);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(ventaRepository, never()).save(any(VentaModel.class));
        verify(stockClient, never()).consumirStock(anyLong(), anyLong(), anyInt());
    }
}