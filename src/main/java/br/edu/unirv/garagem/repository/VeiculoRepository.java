package br.edu.unirv.garagem.repository;

import br.edu.unirv.garagem.model.Veiculo;
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
public class VeiculoRepository implements IVeiculoRepository {

    private final ObjectMapper objectMapper;
    private final Path arquivo;

    public VeiculoRepository(
            ObjectMapper objectMapper,
            @Value("${app.data.veiculos:data/veiculos.json}") String caminhoArquivo) {

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
            throw new IllegalStateException("Não foi possível inicializar o arquivo de veículos.", e);
        }
    }

    private synchronized List<Veiculo> lerTodos() {
        try {
            String conteudo = Files.readString(arquivo);

            if (conteudo == null || conteudo.isBlank()) {
                return new ArrayList<>();
            }

            return objectMapper.readValue(
                    conteudo,
                    new TypeReference<List<Veiculo>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler os dados de veículos.", e);
        }
    }

    private synchronized void salvarTodos(List<Veiculo> veiculos) {
        try {
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporario.toFile(), veiculos);

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
            throw new IllegalStateException("Não foi possível salvar os dados de veículos.", e);
        }
    }

    @Override
    public synchronized List<Veiculo> obterTodos() {
        return lerTodos().stream()
                .sorted(Comparator.comparingInt(Veiculo::getId))
                .toList();
    }

    @Override
    public synchronized Optional<Veiculo> obterPorId(int id) {
        return lerTodos().stream()
                .filter(veiculo -> veiculo.getId() == id)
                .findFirst();
    }

    @Override
    public synchronized void adicionar(Veiculo veiculo) {
        List<Veiculo> veiculos = lerTodos();

        int proximoId = veiculos.stream()
                .mapToInt(Veiculo::getId)
                .max()
                .orElse(0) + 1;

        veiculo.setId(proximoId);
        normalizar(veiculo);
        veiculos.add(veiculo);
        salvarTodos(veiculos);
    }

    @Override
    public synchronized void atualizar(Veiculo veiculo) {
        List<Veiculo> veiculos = lerTodos();

        for (int i = 0; i < veiculos.size(); i++) {
            if (veiculos.get(i).getId() == veiculo.getId()) {
                normalizar(veiculo);
                veiculos.set(i, veiculo);
                salvarTodos(veiculos);
                return;
            }
        }

        throw new IllegalArgumentException("Veículo não encontrado.");
    }

    @Override
    public synchronized void remover(int id) {
        List<Veiculo> veiculos = lerTodos();
        boolean removido = veiculos.removeIf(veiculo -> veiculo.getId() == id);

        if (!removido) {
            throw new IllegalArgumentException("Veículo não encontrado.");
        }

        salvarTodos(veiculos);
    }

    @Override
    public synchronized boolean existePlaca(String placa, Integer idIgnorado) {
        String placaNormalizada = normalizarPlaca(placa);

        return lerTodos().stream().anyMatch(veiculo ->
                normalizarPlaca(veiculo.getPlaca()).equals(placaNormalizada)
                        && (idIgnorado == null || veiculo.getId() != idIgnorado)
        );
    }

    private void normalizar(Veiculo veiculo) {
        veiculo.setPlaca(normalizarPlaca(veiculo.getPlaca()));
        if (veiculo.getMarca() != null) {
            veiculo.setMarca(veiculo.getMarca().trim());
        }
        if (veiculo.getModelo() != null) {
            veiculo.setModelo(veiculo.getModelo().trim());
        }
        if (veiculo.getCor() != null) {
            veiculo.setCor(veiculo.getCor().trim());
        }
    }

    private String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
