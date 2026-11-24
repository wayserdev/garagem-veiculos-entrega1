package br.edu.unirv.garagem.service;

import br.edu.unirv.garagem.dto.VeiculoRequest;
import br.edu.unirv.garagem.exception.ConflitoException;
import br.edu.unirv.garagem.exception.RecursoNaoEncontradoException;
import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.repository.VeiculoRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VeiculoService {
    private final VeiculoRepository repository;

    public VeiculoService(VeiculoRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Veiculo> listar() { return repository.findAll(Sort.by("id")); }

    @Transactional(readOnly = true)
    public Veiculo buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Veiculo não encontrado(a)"));
    }

    @Transactional
    public Veiculo cadastrar(VeiculoRequest request) {
        String placa = request.placa().trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (!placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new IllegalArgumentException("Placa inválida: use o formato ABC1234 ou ABC1D23");
        }
        if (repository.existsByPlaca(placa)) {
            throw new ConflitoException("Esta placa já está cadastrada");
        }
        return repository.save(new Veiculo(placa, request.modelo().trim()));
    }
}
