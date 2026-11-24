package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> { }
