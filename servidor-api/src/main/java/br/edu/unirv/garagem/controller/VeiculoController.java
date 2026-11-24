package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.dto.VeiculoRequest;
import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.service.VeiculoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {
    private final VeiculoService service;
    public VeiculoController(VeiculoService service) { this.service = service; }

    @GetMapping
    public List<Veiculo> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Veiculo buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Veiculo> cadastrar(@Valid @RequestBody VeiculoRequest request) {
        Veiculo veiculo = service.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/veiculos/" + veiculo.getId())).body(veiculo);
    }
}
