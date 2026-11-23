package cl.zaror.usuarios.service;

import cl.zaror.usuarios.dto.ActualizarUsuarioRequest;
import cl.zaror.usuarios.dto.LoginRequest;
import cl.zaror.usuarios.dto.RegistroRequest;
import cl.zaror.usuarios.dto.UsuarioResponse;
import cl.zaror.usuarios.exception.CorreoDuplicadoException;
import cl.zaror.usuarios.exception.CredencialesInvalidasException;
import cl.zaror.usuarios.exception.RecursoNoEncontradoException;
import cl.zaror.usuarios.model.Usuario;
import cl.zaror.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Aqui va la logica de negocio. El controlador solo recibe y responde.
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    // Inyeccion por constructor: Spring entrega el repositorio y el encoder
    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public UsuarioResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        if (repo.existsByCorreoIgnoreCase(correo)) {
            throw new CorreoDuplicadoException(correo);
        }
        Usuario nuevo = new Usuario(req.nombre().trim(), correo, encoder.encode(req.clave()), req.direccion().trim());
        return UsuarioResponse.desde(repo.save(nuevo));
    }

    public UsuarioResponse login(LoginRequest req) {
        Usuario u = repo.findByCorreoIgnoreCase(req.correo().trim())
                .orElseThrow(CredencialesInvalidasException::new);
        // matches compara la clave escrita con el hash guardado
        if (!encoder.matches(req.clave(), u.getClaveHash())) {
            throw new CredencialesInvalidasException();
        }
        return UsuarioResponse.desde(u);
    }

    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.desde(obtener(id));
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req) {
        Usuario u = obtener(id);
        u.setNombre(req.nombre().trim());
        u.setDireccion(req.direccion().trim());
        return UsuarioResponse.desde(repo.save(u));
    }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el usuario " + id);
        }
        repo.deleteById(id);
    }

    private Usuario obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id));
    }
}
