package cl.zaror.alertas.config;

import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;
import cl.zaror.alertas.repository.AlertaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Carga 2 alertas ficticias la primera vez, para que la app no parta con la lista vacia en la demo.
// @Profile("!test") evita que se carguen al correr las pruebas.
@Configuration
@Profile("!test")
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarAlertas(AlertaRepository repo) {
        return args -> {
            if (repo.count() > 0) return; // ya hay datos, no se duplican

            long ahora = System.currentTimeMillis();
            repo.save(nueva("Vecina Demo", "Los Jazmines 425", ahora - 3_600_000,
                    "Persona sospechosa", "Auto sin patente dando vueltas por el pasaje", EstadoAlerta.ATENDIDA));
            repo.save(nueva("Vecino Demo", "Los Aromos 112", ahora - 600_000,
                    "Falla del sistema de alarma", "La sirena del poste no suena", EstadoAlerta.ACTIVA));
        };
    }

    private Alerta nueva(String vecino, String ubicacion, long fecha, String tipo, String desc, EstadoAlerta estado) {
        Alerta a = new Alerta();
        a.setVecinoNombre(vecino);
        a.setUbicacion(ubicacion);
        a.setFechaHora(fecha);
        a.setTipoEmergencia(tipo);
        a.setDescripcion(desc);
        a.setEstado(estado);
        return a;
    }
}
