package cl.zaror.usuarios.controller;

import cl.zaror.usuarios.dto.ActualizarUsuarioRequest;
import cl.zaror.usuarios.dto.LoginRequest;
import cl.zaror.usuarios.dto.RegistroRequest;
import cl.zaror.usuarios.dto.UsuarioResponse;
import cl.zaror.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints REST de usuarios. Base: http://localhost:8081/api/usuarios
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // @Valid activa las validaciones del DTO; si fallan responde 400
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
