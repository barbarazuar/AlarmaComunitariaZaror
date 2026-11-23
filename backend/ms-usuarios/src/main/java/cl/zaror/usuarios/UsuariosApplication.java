package cl.zaror.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de usuarios (puerto 8081)
@SpringBootApplication
public class UsuariosApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
