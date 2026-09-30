package br.edu.unirv.garagem.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reserva {

    private int id;

    @NotNull(message = "Selecione um veículo.")
    private Integer veiculoId;

    @NotNull(message = "Selecione uma pessoa.")
    private Integer pessoaId;

    @NotNull(message = "A data inicial é obrigatória.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @NotNull(message = "A data final é obrigatória.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;

    public Reserva() {
    }

    public Reserva(int id, Integer veiculoId, Integer pessoaId, LocalDate dataInicio, LocalDate dataFim) {
        this.id = id;
        this.veiculoId = veiculoId;
        this.pessoaId = pessoaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(Integer veiculoId) {
        this.veiculoId = veiculoId;
    }

    public Integer getPessoaId() {
        return pessoaId;
    }

    public void setPessoaId(Integer pessoaId) {
        this.pessoaId = pessoaId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getDataInicioFormatada() {
        return dataInicio == null ? "" : dataInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getDataFimFormatada() {
        return dataFim == null ? "" : dataFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}

