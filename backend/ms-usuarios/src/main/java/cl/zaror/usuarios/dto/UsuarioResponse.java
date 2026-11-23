package cl.zaror.usuarios.dto;

import cl.zaror.usuarios.model.Usuario;

// Lo que devuelve la API. No incluye la clave, ni siquiera el hash.
public record UsuarioResponse(Long id, String nombre, String correo, String direccion) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getDireccion());
    }
}
