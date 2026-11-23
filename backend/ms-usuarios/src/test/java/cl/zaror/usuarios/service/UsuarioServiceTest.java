package cl.zaror.usuarios.service;

import cl.zaror.usuarios.dto.LoginRequest;
import cl.zaror.usuarios.dto.RegistroRequest;
import cl.zaror.usuarios.dto.UsuarioResponse;
import cl.zaror.usuarios.exception.CorreoDuplicadoException;
import cl.zaror.usuarios.exception.CredencialesInvalidasException;
import cl.zaror.usuarios.exception.RecursoNoEncontradoException;
import cl.zaror.usuarios.model.Usuario;
import cl.zaror.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Prueba unitaria del servicio: se mockean el repositorio (la BD) y el encoder,
// asi solo se prueba la logica de negocio, sin base de datos real.
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repo;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UsuarioService service;

    @Test
    void registrar_guardaCorreoEnMinusculaYClaveHasheada() {
        when(repo.existsByCorreoIgnoreCase("ana@zaror.cl")).thenReturn(false);
        when(encoder.encode("secreta1")).thenReturn("HASH");
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L); // simula que la BD asigno el id
            return u;
        });

        UsuarioResponse res = service.registrar(
                new RegistroRequest(" Ana Perez ", "ANA@zaror.cl", "secreta1", "Los Jazmines 425"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertEquals("ana@zaror.cl", captor.getValue().getCorreo());
        assertEquals("HASH", captor.getValue().getClaveHash()); // nunca la clave en texto plano
        assertEquals(1L, res.id());
        assertEquals("Ana Perez", res.nombre());
    }

    @Test
    void registrar_conCorreoRepetido_lanzaConflicto() {
        when(repo.existsByCorreoIgnoreCase("ana@zaror.cl")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> service.registrar(
                new RegistroRequest("Ana", "ana@zaror.cl", "secreta1", "Los Jazmines 425")));
        verify(repo, never()).save(any());
    }

    @Test
    void login_conClaveCorrecta_devuelveUsuario() {
        Usuario u = new Usuario("Ana", "ana@zaror.cl", "HASH", "Los Jazmines 425");
        u.setId(5L);
        when(repo.findByCorreoIgnoreCase("ana@zaror.cl")).thenReturn(Optional.of(u));
        when(encoder.matches("secreta1", "HASH")).thenReturn(true);

        UsuarioResponse res = service.login(new LoginRequest("ana@zaror.cl", "secreta1"));

        assertEquals(5L, res.id());
    }

    @Test
    void login_conClaveIncorrecta_lanzaCredencialesInvalidas() {
        Usuario u = new Usuario("Ana", "ana@zaror.cl", "HASH", "Los Jazmines 425");
        when(repo.findByCorreoIgnoreCase("ana@zaror.cl")).thenReturn(Optional.of(u));
        when(encoder.matches("mala", "HASH")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("ana@zaror.cl", "mala")));
    }

    @Test
    void login_conCorreoInexistente_lanzaCredencialesInvalidas() {
        when(repo.findByCorreoIgnoreCase("nadie@zaror.cl")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> service.login(new LoginRequest("nadie@zaror.cl", "x")));
    }

    @Test
    void eliminar_idInexistente_lanza404() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
        verify(repo, never()).deleteById(any());
    }
}
