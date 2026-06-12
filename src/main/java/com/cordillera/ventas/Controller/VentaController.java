package com.cordillera.ventas.Controller;

import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Gestión de Ventas", description = "Endpoints para el registro, consulta y consolidación de transacciones comerciales de Grupo Cordillera")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Operation(summary = "Registrar nueva venta")
    @PostMapping
    public ResponseEntity<VentaResponseDto> crear(
            @Valid @RequestBody VentaRequestDto dto) {
        return new ResponseEntity<>(ventaService.crearVenta(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todas las ventas con aislamiento multi-sucursal")
    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> listar(
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // 📝 LOGS EN CONSOLA DE VENTAS (Verificación de Contexto)
        System.out.println("==========================================================");
        System.out.println("[MS-VENTAS] -> Cabecera Original X-User-Role: " + rol);
        System.out.println("[MS-VENTAS] -> Cabecera Original X-Sucursal-Id: " + sucursalId);
        System.out.println("==========================================================");

        // 🛡️ 1. Sanitización inicial de variables de trabajo locales
        String rolFinal = (rol != null && !"null".equalsIgnoreCase(rol.trim())) ? rol.trim() : null;
        Long sucursalIdFinal = sucursalId;

        // 🕵️‍♂️ ESTRATEGIA DE EXTRACCIÓN INTERNA: Si los headers vienen vacíos, decodificamos el JWT
        if ((rolFinal == null || rolFinal.isEmpty() || sucursalIdFinal == null) && token != null && token.startsWith("Bearer ")) {
            try {
                String jwtPuro = token.substring(7).trim();
                String[] partesJwt = jwtPuro.split("\\.");

                if (partesJwt.length > 1) {
                    String payloadJson = new String(
                            java.util.Base64.getUrlDecoder().decode(partesJwt[1]),
                            java.nio.charset.StandardCharsets.UTF_8
                    );

                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> claims = mapper.readValue(payloadJson, Map.class);

                    if (rolFinal == null || rolFinal.isEmpty()) {
                        rolFinal = (String) claims.get("role");
                        if (rolFinal == null) rolFinal = (String) claims.get("rol");
                    }

                    if (sucursalIdFinal == null) {
                        Object idObj = claims.get("sucursalId");
                        if (idObj == null) idObj = claims.get("sucursal");
                        if (idObj != null) {
                            sucursalIdFinal = Long.valueOf(idObj.toString());
                        }
                    }
                    System.out.println("[🎯 JWT NATIVO PARSED] -> Éxito -> Rol: " + rolFinal + " | Sucursal ID: " + sucursalIdFinal);
                }
            } catch (Exception e) {
                System.out.println("[⚠️ ERROR PARSEO INTERNO] -> No se pudieron leer claims del Token: " + e.getMessage());
            }
        }

        // ⚙️ FALLBACK DINÁMICO DE CONTROL (Por si no hay sesión iniciada)
        if (rolFinal == null || rolFinal.isEmpty()) rolFinal = "GERENTE";
        if (sucursalIdFinal == null) sucursalIdFinal = 7L;

        List<VentaResponseDto> ventasCrudas;

        // 🔒 2. REGLA DE NEGOCIO MULTI-SUCURSAL DINÁMICA
        if (!"ADMIN".equalsIgnoreCase(rolFinal)) {
            System.out.println("[🔒 FILTRO DINÁMICO ACTIVO] -> Retornando ventas de la sucursal del usuario: " + sucursalIdFinal);
            ventasCrudas = ventaService.listarVentasPorSucursal(sucursalIdFinal);
        } else {
            // 🌐 ACCESO GLOBAL CORPORATIVO (ADMIN)
            System.out.println("[🌐 ACCESO GLOBAL] -> Retornando historial completo del Holding.");
            ventasCrudas = ventaService.listarVentas();
        }

        // 🎯 RETORNO VELOZ: Se eliminó el bucle RestTemplate lento para evitar el Timeout (1s) del BFF.
        // El Frontend en React inyectará los nombres usando el catálogo que descarga en un único viaje.
        return ResponseEntity.ok(ventasCrudas);
    }

    @GetMapping("/origen/{origen}")
    public ResponseEntity<List<VentaResponseDto>> listarPorOrigen(@PathVariable String origen) {
        return ResponseEntity.ok(ventaService.listarPorOrigen(origen));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarErroresValidacion(IllegalArgumentException ex) {
        Map<String, String> respuestaDeError = new HashMap<>();
        respuestaDeError.put("message", ex.getMessage());
        return new ResponseEntity<>(respuestaDeError, HttpStatus.BAD_REQUEST);
    }
}