package com.cordillera.ventas.Controller;


import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Service.VentaService;
import jakarta.validation.Valid; // Importante para las validaciones
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // 1. Crear Venta
    @PostMapping
    // El @Valid es vital ahora que cambiamos a productoId
    public ResponseEntity<VentaResponseDto> crear(@Valid @RequestBody VentaRequestDto dto) {
        VentaResponseDto nuevaVenta = ventaService.crearVenta(dto);
        return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
    }

    // 2. Listar todas las ventas
    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> listar() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    // 3. Filtrar por origen (POS/WEB)
    @GetMapping("/origen/{origen}")
    public ResponseEntity<List<VentaResponseDto>> listarPorOrigen(@PathVariable String origen) {
        return ResponseEntity.ok(ventaService.listarPorOrigen(origen));
    }
}