package cl.zaror.alertas.controller;

import cl.zaror.alertas.dto.AlertaResponse;
import cl.zaror.alertas.exception.RecursoNoEncontradoException;
import cl.zaror.alertas.model.EstadoAlerta;
import cl.zaror.alertas.service.AlertaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba de la capa web: se verifica que cada endpoint responda el codigo HTTP correcto.
@WebMvcTest(AlertaController.class)
@ActiveProfiles("test")
class AlertaControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AlertaService service;

    private AlertaResponse ejemplo(EstadoAlerta estado) {
        return new AlertaResponse(1L, "Ana", 1L, "Los Jazmines 425", 1000L, "Incendio", "Humo", estado);
    }

    @Test
    void listar_devuelveLasAlertas() throws Exception {
        when(service.listar(null, null)).thenReturn(List.of(ejemplo(EstadoAlerta.ACTIVA)));

        mvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoEmergencia").value("Incendio"));
    }

    @Test
    void crear_sinUbicacion_responde400() throws Exception {
        mvc.perform(post("/api/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vecinoNombre":"Ana","ubicacion":"","tipoEmergencia":"Incendio"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.ubicacion").exists());

        verifyNoInteractions(service);
    }

    @Test
    void crear_valida_responde201() throws Exception {
        when(service.crear(any())).thenReturn(ejemplo(EstadoAlerta.ACTIVA));

        mvc.perform(post("/api/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vecinoNombre":"Ana","ubicacion":"Los Jazmines 425","tipoEmergencia":"Incendio"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void cambiarEstado_responde200() throws Exception {
        when(service.cambiarEstado(eq(1L), eq(EstadoAlerta.ATENDIDA))).thenReturn(ejemplo(EstadoAlerta.ATENDIDA));

        mvc.perform(patch("/api/alertas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ATENDIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
    }

    @Test
    void eliminar_inexistente_responde404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe la alerta 9")).when(service).eliminar(9L);

        mvc.perform(delete("/api/alertas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la alerta 9"));
    }
}
