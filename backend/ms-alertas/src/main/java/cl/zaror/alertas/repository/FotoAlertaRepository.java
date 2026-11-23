package cl.zaror.alertas.repository;

import cl.zaror.alertas.model.FotoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

// La llave de FotoAlerta es el id de la alerta, asi que findById/existsById/deleteById bastan.
public interface FotoAlertaRepository extends JpaRepository<FotoAlerta, Long> {
}
