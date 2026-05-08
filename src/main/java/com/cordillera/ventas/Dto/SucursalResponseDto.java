package com.cordillera.ventas.Dto;

import lombok.Data;

@Data
public class SucursalResponseDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private Boolean activa;
}