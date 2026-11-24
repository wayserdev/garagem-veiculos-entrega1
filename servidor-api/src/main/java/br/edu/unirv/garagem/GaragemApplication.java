package br.edu.unirv.garagem;

import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.repository.PessoaRepository;
import br.edu.unirv.garagem.repository.VeiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GaragemApplication {
    public static void main(String[] args) {
        SpringApplication.run(GaragemApplication.class, args);
    }

    @Bean
    CommandLineRunner dadosDeExemplo(PessoaRepository pessoas, VeiculoRepository veiculos) {
        return args -> {
            if (pessoas.count() == 0) {
                pessoas.save(new Pessoa("Cliente Exemplo", "cliente@exemplo.com"));
            }
            if (veiculos.count() == 0) {
                veiculos.save(new Veiculo("ABC1D23", "Honda Civic"));
            }
        };
    }
}
