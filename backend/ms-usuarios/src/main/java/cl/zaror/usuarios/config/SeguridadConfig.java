package cl.zaror.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Solo usamos el modulo crypto de Spring Security para hashear claves con BCrypt.
// BCrypt agrega "sal" aleatoria, asi dos claves iguales quedan con hash distinto.
@Configuration
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
