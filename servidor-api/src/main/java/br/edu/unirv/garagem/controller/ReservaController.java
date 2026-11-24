package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.dto.ReservaRequest;
import br.edu.unirv.garagem.model.Reserva;
import br.edu.unirv.garagem.service.ReservaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) { this.service = service; }

    @GetMapping
    public List<Reserva> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Reserva buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Reserva> criar(@Valid @RequestBody ReservaRequest request) {
        Reserva reserva = service.criar(request);
        return ResponseEntity.created(URI.create("/api/reservas/" + reserva.getId())).body(reserva);
    }

    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id) { return service.cancelar(id); }
}
