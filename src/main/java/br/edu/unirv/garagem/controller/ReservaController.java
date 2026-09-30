package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.model.Reserva;
import br.edu.unirv.garagem.model.Veiculo;
import br.edu.unirv.garagem.repository.IPessoaRepository;
import br.edu.unirv.garagem.repository.IReservaRepository;
import br.edu.unirv.garagem.repository.IVeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ReservaController {

    private final IReservaRepository reservaRepository;
    private final IPessoaRepository pessoaRepository;
    private final IVeiculoRepository veiculoRepository;

    public ReservaController(
            IReservaRepository reservaRepository,
            IPessoaRepository pessoaRepository,
            IVeiculoRepository veiculoRepository) {

        this.reservaRepository = reservaRepository;
        this.pessoaRepository = pessoaRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        prepararTela(model, new Reserva());
        return "reserva/index";
    }

    @GetMapping("/reservas")
    public String listar(Model model) {
        prepararTela(model, new Reserva());
        return "reserva/index";
    }

    @PostMapping("/reservas/novo")
    public String cadastrar(
            @Valid @ModelAttribute("reserva") Reserva reserva,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validarReserva(reserva, bindingResult, null);

        if (bindingResult.hasErrors()) {
            prepararTela(model, reserva);
            return "reserva/index";
        }

        if (existeConflito(reserva, null)) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Este veículo já possui uma reserva que se sobrepõe ao período informado."
            );
            return "redirect:/";
        }

        reservaRepository.adicionar(reserva);
        redirectAttributes.addFlashAttribute("sucesso", "Reserva criada com sucesso.");
        return "redirect:/";
    }

    @GetMapping("/reservas/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        return reservaRepository.obterPorId(id)
                .map(reserva -> {
                    prepararTela(model, reserva);
                    model.addAttribute("modoEdicao", true);
                    return "reserva/index";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Reserva não encontrada.");
                    return "redirect:/";
                });
    }

    @PostMapping("/reservas/{id}/editar")
    public String atualizar(
            @PathVariable int id,
            @Valid @ModelAttribute("reserva") Reserva reserva,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        reserva.setId(id);
        validarReserva(reserva, bindingResult, id);

        if (bindingResult.hasErrors()) {
            prepararTela(model, reserva);
            model.addAttribute("modoEdicao", true);
            return "reserva/index";
        }

        if (existeConflito(reserva, id)) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Este veículo já possui uma reserva que se sobrepõe ao período informado."
            );
            return "redirect:/reservas/" + id + "/editar";
        }

        if (reservaRepository.obterPorId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Reserva não encontrada.");
            return "redirect:/";
        }

        reservaRepository.atualizar(reserva);
        redirectAttributes.addFlashAttribute("sucesso", "Reserva atualizada com sucesso.");
        return "redirect:/";
    }

    @PostMapping("/reservas/{id}/excluir")
    public String excluir(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            reservaRepository.remover(id);
            redirectAttributes.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/";
    }

    private void validarReserva(Reserva reserva, BindingResult bindingResult, Integer idIgnorado) {
        if (reserva.getDataInicio() != null
                && reserva.getDataFim() != null
                && reserva.getDataFim().isBefore(reserva.getDataInicio())) {

            bindingResult.rejectValue(
                    "dataFim",
                    "data.invalida",
                    "A data final não pode ser anterior à data inicial."
            );
        }

        if (reserva.getPessoaId() != null
                && pessoaRepository.obterPorId(reserva.getPessoaId()).isEmpty()) {

            bindingResult.rejectValue("pessoaId", "pessoa.invalida", "A pessoa selecionada não existe.");
        }

        if (reserva.getVeiculoId() != null
                && veiculoRepository.obterPorId(reserva.getVeiculoId()).isEmpty()) {

            bindingResult.rejectValue("veiculoId", "veiculo.invalido", "O veículo selecionado não existe.");
        }
    }


    private boolean existeConflito(Reserva reserva, Integer idIgnorado) {
        return reserva.getVeiculoId() != null
                && reserva.getDataInicio() != null
                && reserva.getDataFim() != null
                && !reserva.getDataFim().isBefore(reserva.getDataInicio())
                && reservaRepository.existeConflito(
                        reserva.getVeiculoId(),
                        reserva.getDataInicio(),
                        reserva.getDataFim(),
                        idIgnorado
                );
    }

    private void prepararTela(Model model, Reserva reserva) {
        List<Pessoa> pessoas = pessoaRepository.obterTodas();
        List<Veiculo> veiculos = veiculoRepository.obterTodos();
        List<Reserva> reservas = reservaRepository.obterTodas();

        Map<Integer, Pessoa> pessoasPorId = pessoas.stream()
                .collect(Collectors.toMap(
                        Pessoa::getId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<Integer, Veiculo> veiculosPorId = veiculos.stream()
                .collect(Collectors.toMap(
                        Veiculo::getId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<Integer, Boolean> statusHoje = new LinkedHashMap<>();
        for (Veiculo veiculo : veiculos) {
            statusHoje.put(
                    veiculo.getId(),
                    reservaRepository.veiculoReservadoNaData(veiculo.getId(), LocalDate.now())
            );
        }

        model.addAttribute("reserva", reserva);
        model.addAttribute("reservas", reservas);
        model.addAttribute("pessoas", pessoas);
        model.addAttribute("veiculos", veiculos);
        model.addAttribute("pessoasPorId", pessoasPorId);
        model.addAttribute("veiculosPorId", veiculosPorId);
        model.addAttribute("statusHoje", statusHoje);
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("hojeFormatado", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("modoEdicao", reserva.getId() > 0);
    }
}
