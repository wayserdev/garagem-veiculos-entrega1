package br.edu.unirv.garagem.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
        @NotNull(message = "Pessoa é obrigatória") Long pessoaId,
        @NotNull(message = "Veículo é obrigatório") Long veiculoId) { }
