package cl.zaror.alertas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de alertas (puerto 8082)
@SpringBootApplication
public class AlertasApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertasApplication.class, args);
    }
}
