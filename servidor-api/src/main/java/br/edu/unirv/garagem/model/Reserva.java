package br.edu.unirv.garagem.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    private LocalDateTime criadaEm;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    protected Reserva() { }

    public Reserva(Pessoa pessoa, Veiculo veiculo) {
        this.pessoa = pessoa;
        this.veiculo = veiculo;
        this.criadaEm = LocalDateTime.now();
        this.status = StatusReserva.ATIVA;
    }

    public Long getId() { return id; }
    public Pessoa getPessoa() { return pessoa; }
    public Veiculo getVeiculo() { return veiculo; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public StatusReserva getStatus() { return status; }
    public void cancelar() { this.status = StatusReserva.CANCELADA; }
}
