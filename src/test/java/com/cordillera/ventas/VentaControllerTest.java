package com.cordillera.ventas;

import com.cordillera.ventas.Controller.VentaController;
import com.cordillera.ventas.Dto.VentaRequestDto;
import com.cordillera.ventas.Dto.VentaResponseDto;
import com.cordillera.ventas.Service.VentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest levanta SOLO la capa web (Controladores), haciendo la prueba rapidísima
@WebMvcTest(VentaController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc; // Herramienta clave para simular peticiones HTTP (como un Postman interno)

    @MockBean
    private VentaService ventaService; // Falsificamos el servicio porque ya lo probamos en su propia clase

    @Autowired
    private ObjectMapper objectMapper; // Para convertir nuestros objetos a JSON

    private VentaRequestDto requestDto;
    private VentaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Preparamos los datos de entrada
        requestDto = new VentaRequestDto();
        requestDto.setProductoId(1L);
        requestDto.setSucursalId(1L);
        requestDto.setCantidad(2);
        requestDto.setOrigen("WEB");
        requestDto.setMontoTotal(50000.0);

        // Preparamos la respuesta esperada
        responseDto = new VentaResponseDto();
        responseDto.setId(100L);
        responseDto.setProductoId(1L);
        responseDto.setSucursalId(1L);
        responseDto.setCantidad(2);
        responseDto.setOrigen("WEB");
        responseDto.setMontoTotal(50000.0);
        responseDto.setNombreProducto("Notebook Corporativo i7");
        responseDto.setNombreSucursal("Santiago Centro");
    }

    @Test
    void cuandoCrearVenta_entoncesRetorna201YVenta() throws Exception {
        // GIVEN: Le decimos al servicio falso qué responder
        when(ventaService.crearVenta(any(VentaRequestDto.class))).thenReturn(responseDto);

        // WHEN & THEN: Simulamos un POST y verificamos
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))) // Convertimos el DTO a JSON
                .andExpect(status().isCreated()) // Esperamos un 201 Created
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nombreProducto").value("Notebook Corporativo i7"))
                .andExpect(jsonPath("$.origen").value("WEB"));
    }

    @Test
    void cuandoListarVentas_entoncesRetorna200YLista() throws Exception {
        // GIVEN
        when(ventaService.listarVentas()).thenReturn(List.of(responseDto));

        // WHEN & THEN: Simulamos un GET
        mockMvc.perform(get("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andExpect(jsonPath("$[0].nombreProducto").value("Notebook Corporativo i7"))
                .andExpect(jsonPath("$.size()").value(1)); // Verificamos que traiga 1 elemento
    }

    @Test
    void cuandoListarPorOrigen_entoncesRetorna200YListaFiltrada() throws Exception {
        // GIVEN
        when(ventaService.listarPorOrigen("WEB")).thenReturn(List.of(responseDto));

        // WHEN & THEN
        mockMvc.perform(get("/api/ventas/origen/WEB")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origen").value("WEB"))
                .andExpect(jsonPath("$.size()").value(1));
    }
}