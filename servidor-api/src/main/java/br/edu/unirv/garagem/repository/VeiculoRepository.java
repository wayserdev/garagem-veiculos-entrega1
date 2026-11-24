package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Veiculo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    boolean existsByPlaca(String placa);

    // Bloqueio no banco evita reservar o mesmo veículo simultaneamente.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Veiculo v where v.id = :id")
    Optional<Veiculo> buscarParaAlteracao(@Param("id") Long id);
}
