package br.edu.unirv.garagem.service;

import br.edu.unirv.garagem.dto.ReservaRequest;
import br.edu.unirv.garagem.exception.ConflitoException;
import br.edu.unirv.garagem.exception.RecursoNaoEncontradoException;
import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.model.Reserva;
import br.edu.unirv.garagem.model.StatusReserva;
import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.repository.PessoaRepository;
import br.edu.unirv.garagem.repository.ReservaRepository;
import br.edu.unirv.garagem.repository.VeiculoRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {
    private final ReservaRepository reservas;
    private final PessoaRepository pessoas;
    private final VeiculoRepository veiculos;

    // Injeção de dependências pelo construtor, sem "new Repository()".
    public ReservaService(ReservaRepository reservas, PessoaRepository pessoas, VeiculoRepository veiculos) {
        this.reservas = reservas;
        this.pessoas = pessoas;
        this.veiculos = veiculos;
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar() { return reservas.findAll(Sort.by("id")); }

    @Transactional(readOnly = true)
    public Reserva buscar(Long id) {
        return reservas.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Reserva não encontrada"));
    }

    @Transactional
    public Reserva criar(ReservaRequest request) {
        Pessoa pessoa = pessoas.findById(request.pessoaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
        Veiculo veiculo = veiculos.buscarParaAlteracao(request.veiculoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
        if (!veiculo.isDisponivel()) {
            throw new ConflitoException("Veículo indisponível para reserva");
        }
        veiculo.setDisponivel(false);
        veiculos.save(veiculo);
        return reservas.save(new Reserva(pessoa, veiculo));
    }

    @Transactional
    public Reserva cancelar(Long id) {
        Reserva reserva = reservas.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada"));
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new ConflitoException("Esta reserva já está cancelada");
        }
        // Bloqueio pessimista para preservar a disponibilidade em acessos simultâneos.
        Veiculo veiculo = veiculos.buscarParaAlteracao(reserva.getVeiculo().getId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
        reserva.cancelar();
        veiculo.setDisponivel(true);
        veiculos.save(veiculo);
        return reservas.save(reserva);
    }
}
