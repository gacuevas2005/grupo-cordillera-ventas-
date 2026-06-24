package com.cordillera.ventas.Repository;

import com.cordillera.ventas.Model.VentaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<VentaModel, Long> {

    // Filtrar por canal de origen (WEB, FISICO)
    List<VentaModel> findByOrigen(String origen);

    // 🏢 NUEVO: Spring Data JPA creará automáticamente el "WHERE sucursal_id = ?"
    List<VentaModel> findBySucursalId(Long sucursalId);
}
