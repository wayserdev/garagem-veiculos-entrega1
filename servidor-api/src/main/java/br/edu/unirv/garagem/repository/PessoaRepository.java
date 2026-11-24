package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
