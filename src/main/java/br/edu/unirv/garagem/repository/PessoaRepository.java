package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Pessoa;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class PessoaRepository implements IPessoaRepository {

    private final ObjectMapper objectMapper;
    private final Path arquivo;

    public PessoaRepository(
            ObjectMapper objectMapper,
            @Value("${app.data.pessoas:data/pessoas.json}") String caminhoArquivo) {

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
            throw new IllegalStateException("Não foi possível inicializar o arquivo de pessoas.", e);
        }
    }

    private synchronized List<Pessoa> lerTodas() {
        try {
            String conteudo = Files.readString(arquivo);

            if (conteudo == null || conteudo.isBlank()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    conteudo,
                    new TypeReference<List<Pessoa>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler os dados de pessoas.", e);
        }
    }

    private synchronized void salvarTodas(List<Pessoa> pessoas) {
        try {
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporario.toFile(), pessoas);

            try {
                Files.move(
                        temporario,
                        arquivo,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveNaoSuportado) {
                Files.move(
                        temporario,
                        arquivo,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar os dados de pessoas.", e);
        }
    }

    @Override
    public synchronized List<Pessoa> obterTodas() {
        return lerTodas()
                .stream()
                .sorted(Comparator.comparingInt(Pessoa::getId))
                .toList();
    }

    @Override
    public synchronized Optional<Pessoa> obterPorId(int id) {
        return lerTodas()
                .stream()
                .filter(pessoa -> pessoa.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized void adicionar(Pessoa pessoa) {
        List<Pessoa> pessoas = lerTodas();

        int proximoId = pessoas.stream()
                .mapToInt(Pessoa::getId)
                .max()
                .orElse(0) + 1;

        pessoa.setId(proximoId);
        pessoas.add(pessoa);

        salvarTodas(pessoas);
    }

    @Override
    public synchronized void atualizar(Pessoa pessoa) {
        List<Pessoa> pessoas = lerTodas();

        for (int i = 0; i < pessoas.size(); i++) {
            if (pessoas.get(i).getId() == pessoa.getId()) {
                pessoas.set(i, pessoa);
                salvarTodas(pessoas);
                return;
            }
        }

        throw new IllegalArgumentException("Pessoa não encontrada.");
    }

    @Override
    public synchronized void remover(int id) {
        List<Pessoa> pessoas = lerTodas();
        boolean removida = pessoas.removeIf(pessoa -> pessoa.getId() == id);

        if (!removida) {
            throw new IllegalArgumentException("Pessoa não encontrada.");
        }

        salvarTodas(pessoas);
    }

    @Override
    public synchronized boolean existeCpf(String cpf, Integer idIgnorado) {
        String cpfNormalizado = normalizarCpf(cpf);

        return lerTodas().stream().anyMatch(pessoa ->
                normalizarCpf(pessoa.getCpf()).equals(cpfNormalizado)
                        && (idIgnorado == null || pessoa.getId() != idIgnorado)
        );
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }
}
