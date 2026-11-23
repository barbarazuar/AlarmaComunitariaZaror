package cl.zaror.alertas.repository;

import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// @DataJpaTest usa una H2 en memoria que se borra al terminar.
// Aqui no hay mocks: se prueba que las consultas del repositorio funcionen de verdad.
@DataJpaTest
@ActiveProfiles("test")
class AlertaRepositoryTest {

    @Autowired
    AlertaRepository repo;

    private Alerta alerta(long fecha, EstadoAlerta estado) {
        Alerta a = new Alerta();
        a.setVecinoNombre("Ana");
        a.setUbicacion("Los Jazmines 425");
        a.setFechaHora(fecha);
        a.setTipoEmergencia("Incendio");
        a.setEstado(estado);
        return a;
    }

    @Test
    void listaOrdenadaDeMasNuevaAMasAntigua() {
        repo.save(alerta(100L, EstadoAlerta.ACTIVA));
        repo.save(alerta(300L, EstadoAlerta.ACTIVA));
        repo.save(alerta(200L, EstadoAlerta.ACTIVA));

        List<Alerta> res = repo.findAllByOrderByFechaHoraDesc();

        assertEquals(List.of(300L, 200L, 100L), res.stream().map(Alerta::getFechaHora).toList());
    }

    @Test
    void filtraPorEstado() {
        repo.save(alerta(100L, EstadoAlerta.ACTIVA));
        repo.save(alerta(200L, EstadoAlerta.ATENDIDA));

        assertEquals(1, repo.findByEstadoOrderByFechaHoraDesc(EstadoAlerta.ATENDIDA).size());
    }
}
