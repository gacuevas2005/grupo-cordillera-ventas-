package com.cordillera.ventas.Repository;


import com.cordillera.ventas.Model.VentaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<VentaModel, Long> {
    // Aquí podrías crear métodos personalizados después
    List<VentaModel> findByOrigen(String origen);
}
