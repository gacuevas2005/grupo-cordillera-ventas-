package com.cordillera.ventas.Dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor // Necesario para que @Builder funcione correctamente
@NoArgsConstructor
public class StockResponseDto {
    private Long id;

    private Long productoId;
    private Long sucursalId;

    private Integer cantidadDisponible;
    private Integer cantidadReservada;

    private LocalDateTime ultimaActualizacion;
}