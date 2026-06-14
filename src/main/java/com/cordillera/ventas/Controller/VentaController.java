package com.cordillera.ventas.Controller;

import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Service.VentaService;

// Importaciones de Swagger / OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Gestión de Ventas", description = "Endpoints para el registro, consulta y consolidación de transacciones comerciales de Grupo Cordillera")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Operation(
            summary = "Registrar nueva venta",
            description = "Valida la existencia del producto, verifica el stock en la sucursal, automatiza el cálculo de precios y registra la transacción descontando el inventario de forma asíncrona."
    )
    @ApiResponse(responseCode = "201", description = "Venta registrada y stock descontado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación (Ej: stock insuficiente, producto no existe o sucursal inválida)")
    @PostMapping
    public ResponseEntity<VentaResponseDto> crear(
            @Parameter(description = "Objeto JSON con los datos de la venta a registrar (productoId, sucursalId, cantidad, origen)")
            @Valid @RequestBody VentaRequestDto dto) {
        return new ResponseEntity<>(ventaService.crearVenta(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar todas las ventas",
            description = "Obtiene el registro histórico completo de todas las ventas consolidadas. Retorna datos enriquecidos conectándose internamente con los microservicios de Productos y Sucursales."
    )
    @ApiResponse(responseCode = "200", description = "Lista histórica de ventas obtenida con éxito")
    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> listar() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @Operation(
            summary = "Filtrar ventas por canal de origen",
            description = "Permite obtener una lista de ventas filtrando específicamente por el canal donde se realizó la transacción (Ej: WEB, POS, FISICO)."
    )
    @ApiResponse(responseCode = "200", description = "Ventas filtradas obtenidas con éxito")
    @GetMapping("/origen/{origen}")
    public ResponseEntity<List<VentaResponseDto>> listarPorOrigen(
            @Parameter(description = "Canal de venta a consultar", example = "POS") @PathVariable String origen) {
        return ResponseEntity.ok(ventaService.listarPorOrigen(origen));
    }

    @Operation(summary = "Manejador de errores", hidden = true) // hidden=true para que no ensucie la interfaz de Swagger
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> manejarErroresValidacion(IllegalArgumentException ex) {
        java.util.Map<String, String> respuestaDeError = new java.util.HashMap<>();

        // Usamos la clave "message" para mantener la consistencia con el consumo en React
        respuestaDeError.put("message", ex.getMessage());

        // Retorna un 400 Bad Request interceptando de forma limpia las excepciones del VentaService
        return new ResponseEntity<>(respuestaDeError, HttpStatus.BAD_REQUEST);
    }
}