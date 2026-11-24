package br.edu.unirv.garagem.dto;

import jakarta.validation.constraints.NotBlank;

public record VeiculoRequest(
        @NotBlank(message = "Placa é obrigatória") String placa,
        @NotBlank(message = "Modelo é obrigatório") String modelo) { }
