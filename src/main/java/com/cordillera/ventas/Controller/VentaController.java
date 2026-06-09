package com.cordillera.ventas.Controller;

import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Registrar nueva venta", description = "Valida la existencia del producto, verifica el stock en la sucursal, comprueba que el monto total sea válido y, si todo es correcto, registra la transacción descontando el inventario automáticamente.")
    @PostMapping
    public ResponseEntity<VentaResponseDto> crear(
            @Valid @RequestBody VentaRequestDto dto) {
        return new ResponseEntity<>(ventaService.crearVenta(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todas las ventas", description = "Obtiene el registro histórico completo de todas las ventas consolidadas. Retorna datos enriquecidos conectándose internamente con los microservicios de Productos y Sucursales.")
    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> listar() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @Operation(summary = "Filtrar ventas por canal de origen", description = "Permite obtener una lista de ventas filtrando específicamente por el canal donde se realizó la transacción (Ej: WEB, FISICO).")
    @GetMapping("/origen/{origen}")
    public ResponseEntity<List<VentaResponseDto>> listarPorOrigen(
            @Parameter(description = "Canal de venta a consultar", example = "WEB") @PathVariable String origen) {
        return ResponseEntity.ok(ventaService.listarPorOrigen(origen));
    }

    @Operation(summary = "Manejador de errores", hidden = true) // hidden=true para que no ensucie el Swagger
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> manejarErroresValidacion(IllegalArgumentException ex) {
        java.util.Map<String, String> respuestaDeError = new java.util.HashMap<>();

        // Usamos la clave "message" porque así lo programaste en tu ventaService.js de React
        respuestaDeError.put("message", ex.getMessage());

        // Retorna un 400 Bad Request sin reventar la consola
        return new ResponseEntity<>(respuestaDeError, HttpStatus.BAD_REQUEST);
    }
}