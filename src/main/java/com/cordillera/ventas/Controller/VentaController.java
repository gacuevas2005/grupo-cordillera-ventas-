package com.cordillera.ventas.Controller;

import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Service.VentaService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<VentaResponseDto> crear(@Valid @RequestBody VentaRequestDto dto) {
        return new ResponseEntity<>(ventaService.crearVenta(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> listar() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @GetMapping("/origen/{origen}")
    public ResponseEntity<List<VentaResponseDto>> listarPorOrigen(@PathVariable String origen) {
        // Ahora sí existe el método en el Service
        return ResponseEntity.ok(ventaService.listarPorOrigen(origen));
    }
}