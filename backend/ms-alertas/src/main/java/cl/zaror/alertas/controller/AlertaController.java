package cl.zaror.alertas.controller;

import cl.zaror.alertas.dto.AlertaRequest;
import cl.zaror.alertas.dto.AlertaResponse;
import cl.zaror.alertas.dto.EstadoRequest;
import cl.zaror.alertas.model.EstadoAlerta;
import cl.zaror.alertas.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints REST de alertas (CRUD completo). Base: http://localhost:8082/api/alertas
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    // GET /api/alertas, /api/alertas?estado=ACTIVA o /api/alertas?usuarioId=1
    @GetMapping
    public List<AlertaResponse> listar(@RequestParam(required = false) EstadoAlerta estado,
                                       @RequestParam(required = false) Long usuarioId) {
        return service.listar(estado, usuarioId);
    }

    @GetMapping("/{id}")
    public AlertaResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertaResponse crear(@Valid @RequestBody AlertaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public AlertaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlertaRequest req) {
        return service.actualizar(id, req);
    }

    // PATCH cambia solo una parte (el estado), PUT reemplaza la alerta completa
    @PatchMapping("/{id}/estado")
    public AlertaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
