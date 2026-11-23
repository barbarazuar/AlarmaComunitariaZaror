package cl.zaror.usuarios.controller;

import cl.zaror.usuarios.dto.UsuarioResponse;
import cl.zaror.usuarios.exception.CorreoDuplicadoException;
import cl.zaror.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest levanta solo la capa web (controlador + manejador de errores).
// El servicio se reemplaza por un mock, asi se prueba la respuesta HTTP sin BD.
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UsuarioService service;

    @Test
    void registro_valido_responde201SinClave() throws Exception {
        when(service.registrar(any()))
                .thenReturn(new UsuarioResponse(1L, "Ana", "ana@zaror.cl", "Los Jazmines 425"));

        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","correo":"ana@zaror.cl","clave":"secreta1","direccion":"Los Jazmines 425"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    void registro_conCorreoInvalido_responde400ConDetalle() throws Exception {
        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","correo":"no-es-correo","clave":"secreta1","direccion":"Los Jazmines 425"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").value("El correo no es valido"));

        verifyNoInteractions(service); // la validacion corta antes de llegar al servicio
    }

    @Test
    void registro_correoRepetido_responde409() throws Exception {
        when(service.registrar(any())).thenThrow(new CorreoDuplicadoException("ana@zaror.cl"));

        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","correo":"ana@zaror.cl","clave":"secreta1","direccion":"Los Jazmines 425"}
                                """))
                .andExpect(status().isConflict());
    }
}
