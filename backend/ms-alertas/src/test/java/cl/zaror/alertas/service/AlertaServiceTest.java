package cl.zaror.alertas.service;

import cl.zaror.alertas.dto.AlertaRequest;
import cl.zaror.alertas.dto.AlertaResponse;
import cl.zaror.alertas.exception.RecursoNoEncontradoException;
import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;
import cl.zaror.alertas.repository.AlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Prueba unitaria del servicio de alertas. El repositorio es un mock (no hay BD)
// y el reloj se fija en 1000 para saber que fecha deberia quedar.
@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    AlertaRepository repo;

    AlertaService service;

    @BeforeEach
    void preparar() {
        service = new AlertaService(repo, () -> 1000L);
    }

    private AlertaRequest request(Long fecha) {
        return new AlertaRequest(" Ana ", 1L, "Los Jazmines 425", fecha, "Incendio", " Humo en el pasaje ");
    }

    @Test
    void crear_sinFecha_usaHoraDelServidorYQuedaActiva() {
        when(repo.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaResponse res = service.crear(request(null));

        assertEquals(1000L, res.fechaHora());
        assertEquals(EstadoAlerta.ACTIVA, res.estado());
        assertEquals("Ana", res.vecinoNombre());           // se limpian espacios
        assertEquals("Humo en el pasaje", res.descripcion());
    }

    @Test
    void crear_conFecha_respetaLaFechaDeLaApp() {
        when(repo.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaResponse res = service.crear(request(555L));

        assertEquals(555L, res.fechaHora());
    }

    @Test
    void cambiarEstado_marcaAtendida() {
        Alerta a = new Alerta();
        a.setId(3L);
        a.setEstado(EstadoAlerta.ACTIVA);
        when(repo.findById(3L)).thenReturn(Optional.of(a));
        when(repo.save(a)).thenReturn(a);

        AlertaResponse res = service.cambiarEstado(3L, EstadoAlerta.ATENDIDA);

        assertEquals(EstadoAlerta.ATENDIDA, res.estado());
    }

    @Test
    void actualizar_idInexistente_lanza404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.actualizar(99L, request(null)));
        verify(repo, never()).save(any());
    }

    @Test
    void listar_conEstado_usaConsultaFiltrada() {
        when(repo.findByEstadoOrderByFechaHoraDesc(EstadoAlerta.ACTIVA)).thenReturn(List.of(new Alerta()));

        List<AlertaResponse> res = service.listar(EstadoAlerta.ACTIVA, null);

        assertEquals(1, res.size());
        verify(repo, never()).findAllByOrderByFechaHoraDesc();
    }

    @Test
    void eliminar_existente_borra() {
        when(repo.existsById(3L)).thenReturn(true);

        service.eliminar(3L);

        verify(repo).deleteById(3L);
    }

    @Test void listar_sinFiltro_yPorUsuario() {
        when(repo.findAllByOrderByFechaHoraDesc()).thenReturn(List.of(new Alerta()));
        when(repo.findByUsuarioIdOrderByFechaHoraDesc(7L)).thenReturn(List.of(new Alerta()));
        assertEquals(1, service.listar(null, null).size());
        assertEquals(1, service.listar(null, 7L).size());
        verify(repo).findByUsuarioIdOrderByFechaHoraDesc(7L);
    }

    @Test void buscarPorId_existente_yAusente() {
        Alerta a = new Alerta(); a.setId(8L);
        when(repo.findById(8L)).thenReturn(Optional.of(a));
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertEquals(8L, service.buscarPorId(8L).id());
        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test void actualizar_reemplazaDatos_yConservaFechaSiNoViene() {
        Alerta a = new Alerta(); a.setId(8L); a.setFechaHora(123L); a.setEstado(EstadoAlerta.ATENDIDA);
        when(repo.findById(8L)).thenReturn(Optional.of(a));
        when(repo.save(a)).thenReturn(a);
        AlertaResponse sinFecha = service.actualizar(8L, new AlertaRequest(" Ana ", 7L, " Calle 123 ", null, " Incendio ", null));
        assertEquals(123L, sinFecha.fechaHora()); assertEquals("", sinFecha.descripcion());
        assertEquals("Calle 123", sinFecha.ubicacion()); assertEquals(7L, sinFecha.usuarioId());
        assertEquals(EstadoAlerta.ATENDIDA, sinFecha.estado());
        assertEquals(555L, service.actualizar(8L, request(555L)).fechaHora());
    }

    @Test void eliminar_ausente_noBorra() {
        when(repo.existsById(99L)).thenReturn(false);
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
        verify(repo, never()).deleteById(any());
    }
}
