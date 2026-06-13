package com.cordillera.ventas.Model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas_consolidadas")
@Data
public class VentaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "sucursal_id")
    private Long sucursalId;

    private String origen;
    private Integer cantidad;

    @Column(name = "monto_total")
    private Double montoTotal;

    @Column(name = "fecha_venta")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaVenta;
}