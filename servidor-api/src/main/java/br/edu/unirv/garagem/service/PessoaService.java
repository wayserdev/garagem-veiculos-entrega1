package br.edu.unirv.garagem.service;

import br.edu.unirv.garagem.dto.PessoaRequest;
import br.edu.unirv.garagem.exception.ConflitoException;
import br.edu.unirv.garagem.exception.RecursoNaoEncontradoException;
import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.repository.PessoaRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {
    private final PessoaRepository repository;

    public PessoaService(PessoaRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Pessoa> listar() { return repository.findAll(Sort.by("id")); }

    @Transactional(readOnly = true)
    public Pessoa buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Pessoa não encontrado(a)"));
    }

    @Transactional
    public Pessoa cadastrar(PessoaRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Este e-mail já está cadastrado");
        }
        return repository.save(new Pessoa(request.nome().trim(), email));
    }
}
