package br.edu.unirv.garagem.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of("status", "online", "aplicacao", "Garagem de Veículos - Servidor N3");
    }
}
