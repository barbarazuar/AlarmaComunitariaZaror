package cl.zaror.alertas.service;

import cl.zaror.alertas.dto.AlertaRequest;
import cl.zaror.alertas.dto.AlertaResponse;
import cl.zaror.alertas.exception.FotoInvalidaException;
import cl.zaror.alertas.exception.RecursoNoEncontradoException;
import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;
import cl.zaror.alertas.model.FotoAlerta;
import cl.zaror.alertas.repository.AlertaRepository;
import cl.zaror.alertas.repository.FotoAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.LongSupplier;

// Logica de negocio de las alertas. El controlador solo recibe y responde.
@Service
public class AlertaService {

    // Tamano maximo de una foto: la app la reduce antes de subirla
    static final int MAX_BYTES_FOTO = 2 * 1024 * 1024;

    private final AlertaRepository repo;
    private final FotoAlertaRepository fotos;
    // Reloj inyectable: en produccion es la hora real, en los tests se fija para poder comparar
    private final LongSupplier reloj;

    // Hay dos constructores, asi que se le indica a Spring cual usar
    @Autowired
    public AlertaService(AlertaRepository repo, FotoAlertaRepository fotos) {
        this(repo, fotos, System::currentTimeMillis);
    }

    AlertaService(AlertaRepository repo, FotoAlertaRepository fotos, LongSupplier reloj) {
        this.repo = repo;
        this.fotos = fotos;
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
        return alertas.stream().map(this::respuesta).toList();
    }

    public AlertaResponse buscarPorId(Long id) {
        return respuesta(obtener(id));
    }

    public AlertaResponse crear(AlertaRequest req) {
        Alerta a = new Alerta();
        copiarDatos(req, a);
        a.setFechaHora(req.fechaHora() != null ? req.fechaHora() : reloj.getAsLong());
        a.setEstado(EstadoAlerta.ACTIVA); // toda alerta nueva parte activa
        return respuesta(repo.save(a));
    }

    public AlertaResponse actualizar(Long id, AlertaRequest req) {
        Alerta a = obtener(id);
        copiarDatos(req, a);
        if (req.fechaHora() != null) {
            a.setFechaHora(req.fechaHora());
        }
        return respuesta(repo.save(a));
    }

    public AlertaResponse cambiarEstado(Long id, EstadoAlerta estado) {
        Alerta a = obtener(id);
        a.setEstado(estado);
        return respuesta(repo.save(a));
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe la alerta " + id);
        }
        repo.deleteById(id);
        fotos.deleteById(id); // la foto se va con su alerta
    }

    // Guarda (o reemplaza) la foto de una alerta
    public void guardarFoto(Long alertaId, byte[] datos, String contentType) {
        obtener(alertaId); // 404 si la alerta no existe
        if (datos == null || datos.length == 0) {
            throw new FotoInvalidaException("La foto esta vacia");
        }
        if (datos.length > MAX_BYTES_FOTO) {
            throw new FotoInvalidaException("La foto no puede pasar de 2 MB");
        }
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new FotoInvalidaException("El archivo debe ser una imagen (image/jpeg o image/png)");
        }
        fotos.save(new FotoAlerta(alertaId, contentType, datos));
    }

    public FotoAlerta obtenerFoto(Long alertaId) {
        return fotos.findById(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La alerta " + alertaId + " no tiene foto"));
    }

    private AlertaResponse respuesta(Alerta a) {
        return AlertaResponse.desde(a, a.getId() != null && fotos.existsById(a.getId()));
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
