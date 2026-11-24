package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.dto.PessoaRequest;
import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.service.PessoaService;
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
@RequestMapping("/api/pessoas")
public class PessoaController {
    private final PessoaService service;
    public PessoaController(PessoaService service) { this.service = service; }

    @GetMapping
    public List<Pessoa> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Pessoa buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Pessoa> cadastrar(@Valid @RequestBody PessoaRequest request) {
        Pessoa pessoa = service.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/pessoas/" + pessoa.getId())).body(pessoa);
    }
}
