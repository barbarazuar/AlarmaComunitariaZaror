package cl.zaror.usuarios.repository;

import cl.zaror.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository ya trae save, findById, findAll, deleteById, etc.
// Los metodos de abajo Spring los arma solo a partir del nombre.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
