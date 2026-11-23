package cl.zaror.alertas.service;

import cl.zaror.alertas.dto.AlertaRequest;
import cl.zaror.alertas.dto.AlertaResponse;
import cl.zaror.alertas.exception.RecursoNoEncontradoException;
import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;
import cl.zaror.alertas.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.LongSupplier;

// Logica de negocio de las alertas. El controlador solo recibe y responde.
@Service
public class AlertaService {

    private final AlertaRepository repo;
    // Reloj inyectable: en produccion es la hora real, en los tests se fija para poder comparar
    private final LongSupplier reloj;

    // Hay dos constructores, asi que se le indica a Spring cual usar
    @Autowired
    public AlertaService(AlertaRepository repo) {
        this(repo, System::currentTimeMillis);
    }

    AlertaService(AlertaRepository repo, LongSupplier reloj) {
        this.repo = repo;
        this.reloj = reloj;
    }

    // Si viene estado filtra por estado; si viene usuarioId filtra por usuario; si no, todas
    public List<AlertaResponse> listar(EstadoAlerta estado, Long usuarioId) {
        List<Alerta> alertas;
        if (estado != null) {
            alertas = repo.findByEstadoOrderByFechaHoraDesc(estado);
        } else if (usuarioId != null) {
            alertas = repo.findByUsuarioIdOrderByFechaHoraDesc(usuarioId);
        } else {
            alertas = repo.findAllByOrderByFechaHoraDesc();
        }
        return alertas.stream().map(AlertaResponse::desde).toList();
    }

    public AlertaResponse buscarPorId(Long id) {
        return AlertaResponse.desde(obtener(id));
    }

    public AlertaResponse crear(AlertaRequest req) {
        Alerta a = new Alerta();
        copiarDatos(req, a);
        a.setFechaHora(req.fechaHora() != null ? req.fechaHora() : reloj.getAsLong());
        a.setEstado(EstadoAlerta.ACTIVA); // toda alerta nueva parte activa
        return AlertaResponse.desde(repo.save(a));
    }

    public AlertaResponse actualizar(Long id, AlertaRequest req) {
        Alerta a = obtener(id);
        copiarDatos(req, a);
        if (req.fechaHora() != null) {
            a.setFechaHora(req.fechaHora());
        }
        return AlertaResponse.desde(repo.save(a));
    }

    public AlertaResponse cambiarEstado(Long id, EstadoAlerta estado) {
        Alerta a = obtener(id);
        a.setEstado(estado);
        return AlertaResponse.desde(repo.save(a));
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe la alerta " + id);
        }
        repo.deleteById(id);
    }

    private Alerta obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alerta " + id));
    }

    private void copiarDatos(AlertaRequest req, Alerta a) {
        a.setVecinoNombre(req.vecinoNombre().trim());
        a.setUsuarioId(req.usuarioId());
        a.setUbicacion(req.ubicacion().trim());
        a.setTipoEmergencia(req.tipoEmergencia().trim());
        a.setDescripcion(req.descripcion() == null ? "" : req.descripcion().trim());
    }
}
