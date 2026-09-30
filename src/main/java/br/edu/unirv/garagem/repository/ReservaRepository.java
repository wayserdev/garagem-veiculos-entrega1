package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Reserva;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservaRepository implements IReservaRepository {

    private final ObjectMapper objectMapper;
    private final Path arquivo;

    public ReservaRepository(
            ObjectMapper objectMapper,
            @Value("${app.data.reservas:data/reservas.json}") String caminhoArquivo) {

        this.objectMapper = objectMapper;
        this.arquivo = Path.of(caminhoArquivo);
        inicializarArquivo();
    }

    private synchronized void inicializarArquivo() {
        try {
            Path pasta = arquivo.getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            if (Files.notExists(arquivo)) {
                Files.writeString(arquivo, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível inicializar o arquivo de reservas.", e);
        }
    }

    private synchronized List<Reserva> lerTodas() {
        try {
            String conteudo = Files.readString(arquivo);

            if (conteudo == null || conteudo.isBlank()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    conteudo,
                    new TypeReference<List<Reserva>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler os dados de reservas.", e);
        }
    }

    private synchronized void salvarTodas(List<Reserva> reservas) {
        try {
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporario.toFile(), reservas);

            try {
                Files.move(
                        temporario,
                        arquivo,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveNaoSuportado) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar os dados de reservas.", e);
        }
    }

    @Override
    public synchronized List<Reserva> obterTodas() {
        return lerTodas().stream()
                .sorted(Comparator.comparing(Reserva::getDataInicio)
                        .thenComparingInt(Reserva::getId))
                .toList();
    }

    @Override
    public synchronized Optional<Reserva> obterPorId(int id) {
        return lerTodas().stream()
                .filter(reserva -> reserva.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized void adicionar(Reserva reserva) {
        List<Reserva> reservas = lerTodas();

        int proximoId = reservas.stream()
                .mapToInt(Reserva::getId)
                .max()
                .orElse(0) + 1;

        reserva.setId(proximoId);
        reservas.add(reserva);
        salvarTodas(reservas);
    }

    @Override
    public synchronized void atualizar(Reserva reserva) {
        List<Reserva> reservas = lerTodas();

        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId() == reserva.getId()) {
                reservas.set(i, reserva);
                salvarTodas(reservas);
                return;
            }
        }

        throw new IllegalArgumentException("Reserva não encontrada.");
    }

    @Override
    public synchronized void remover(int id) {
        List<Reserva> reservas = lerTodas();
        boolean removida = reservas.removeIf(reserva -> reserva.getId() == id);

        if (!removida) {
            throw new IllegalArgumentException("Reserva não encontrada.");
        }

        salvarTodas(reservas);
    }

    @Override
    public synchronized boolean existeConflito(
            int veiculoId,
            LocalDate inicio,
            LocalDate fim,
            Integer idIgnorado) {

        if (inicio == null || fim == null) {
            return false;
        }

        return lerTodas().stream().anyMatch(existente ->
                existente.getVeiculoId() != null
                        && existente.getVeiculoId() == veiculoId
                        && (idIgnorado == null || existente.getId() != idIgnorado)
                        && !inicio.isAfter(existente.getDataFim())
                        && !fim.isBefore(existente.getDataInicio())
        );
    }

    @Override
    public synchronized boolean veiculoReservadoNaData(int veiculoId, LocalDate data) {
        return lerTodas().stream().anyMatch(reserva ->
                reserva.getVeiculoId() != null
                        && reserva.getVeiculoId() == veiculoId
                        && !data.isBefore(reserva.getDataInicio())
                        && !data.isAfter(reserva.getDataFim())
        );
    }

    @Override
    public synchronized boolean existeReservaParaVeiculo(int veiculoId) {
        return lerTodas().stream()
                .anyMatch(reserva -> reserva.getVeiculoId() != null && reserva.getVeiculoId() == veiculoId);
    }

    @Override
    public synchronized boolean existeReservaParaPessoa(int pessoaId) {
        return lerTodas().stream()
                .anyMatch(reserva -> reserva.getPessoaId() != null && reserva.getPessoaId() == pessoaId);
    }
}
