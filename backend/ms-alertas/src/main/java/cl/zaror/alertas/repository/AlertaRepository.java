package cl.zaror.alertas.repository;

import cl.zaror.alertas.model.Alerta;
import cl.zaror.alertas.model.EstadoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Spring arma las consultas a partir del nombre del metodo
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findAllByOrderByFechaHoraDesc();

    List<Alerta> findByEstadoOrderByFechaHoraDesc(EstadoAlerta estado);

    List<Alerta> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}
