package com.cordillera.ventas.Dto;



import lombok.Data;

@Data
public class ProductoResponseDto {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private Double precio;

    private String nombreCategoria;
}