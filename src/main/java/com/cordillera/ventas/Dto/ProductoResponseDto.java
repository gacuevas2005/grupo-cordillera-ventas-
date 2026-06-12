package com.cordillera.ventas.Dto;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoResponseDto {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private Double precio;

    private Long categoriaId;
}