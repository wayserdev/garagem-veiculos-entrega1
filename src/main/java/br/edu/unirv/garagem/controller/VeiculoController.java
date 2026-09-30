package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Veiculo;
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

@Controller
public class VeiculoController {

    private final IVeiculoRepository veiculoRepository;
    private final IReservaRepository reservaRepository;

    public VeiculoController(
            IVeiculoRepository veiculoRepository,
            IReservaRepository reservaRepository) {

        this.veiculoRepository = veiculoRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/veiculos")
    public String listar(Model model) {
        model.addAttribute("veiculos", veiculoRepository.obterTodos());
        return "veiculo/index";
    }

    @GetMapping("/veiculos/novo")
    public String novo(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        model.addAttribute("modoEdicao", false);
        return "veiculo/VeiculoForm";
    }

    @PostMapping("/veiculos/novo")
    public String cadastrar(
            @Valid @ModelAttribute("veiculo") Veiculo veiculo,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validarPlaca(veiculo, bindingResult, null);

        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicao", false);
            return "veiculo/VeiculoForm";
        }

        veiculoRepository.adicionar(veiculo);
        redirectAttributes.addFlashAttribute("sucesso", "Veículo cadastrado com sucesso.");
        return "redirect:/veiculos";
    }

    @GetMapping("/veiculos/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        return veiculoRepository.obterPorId(id)
                .map(veiculo -> {
                    model.addAttribute("veiculo", veiculo);
                    model.addAttribute("modoEdicao", true);
                    return "veiculo/VeiculoForm";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Veículo não encontrado.");
                    return "redirect:/veiculos";
                });
    }

    @PostMapping("/veiculos/{id}/editar")
    public String atualizar(
            @PathVariable int id,
            @Valid @ModelAttribute("veiculo") Veiculo veiculo,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        veiculo.setId(id);
        validarPlaca(veiculo, bindingResult, id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicao", true);
            return "veiculo/VeiculoForm";
        }

        if (veiculoRepository.obterPorId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Veículo não encontrado.");
            return "redirect:/veiculos";
        }

        veiculoRepository.atualizar(veiculo);
        redirectAttributes.addFlashAttribute("sucesso", "Veículo atualizado com sucesso.");
        return "redirect:/veiculos";
    }

    @PostMapping("/veiculos/{id}/excluir")
    public String excluir(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (reservaRepository.existeReservaParaVeiculo(id)) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não é possível excluir este veículo porque ele possui reserva vinculada."
            );
            return "redirect:/veiculos";
        }

        try {
            veiculoRepository.remover(id);
            redirectAttributes.addFlashAttribute("sucesso", "Veículo excluído com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/veiculos";
    }

    private void validarPlaca(Veiculo veiculo, BindingResult bindingResult, Integer idIgnorado) {
        if (veiculo.getPlaca() == null || veiculo.getPlaca().isBlank()) {
            return;
        }

        String placa = veiculo.getPlaca().replaceAll("[^A-Za-z0-9]", "");

        if (placa.length() != 7) {
            bindingResult.rejectValue("placa", "placa.invalida", "A placa deve conter 7 caracteres.");
            return;
        }

        if (veiculoRepository.existePlaca(veiculo.getPlaca(), idIgnorado)) {
            bindingResult.rejectValue("placa", "placa.duplicada", "Já existe um veículo com esta placa.");
        }
    }
}
