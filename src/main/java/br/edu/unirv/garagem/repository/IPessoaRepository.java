package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;

import java.util.List;
import java.util.Optional;

public interface IPessoaRepository {

    List<Pessoa> obterTodas();

    Optional<Pessoa> obterPorId(int id);

    void adicionar(Pessoa pessoa);

    void atualizar(Pessoa pessoa);

    void remover(int id);

    boolean existeCpf(String cpf, Integer idIgnorado);
}
