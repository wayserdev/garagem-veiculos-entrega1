package br.edu.unirv.garagem.controller;

import br.edu.unirv.garagem.model.Pessoa;
import br.edu.unirv.garagem.repository.IPessoaRepository;
import br.edu.unirv.garagem.repository.IReservaRepository;
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
public class PessoaController {

    private final IPessoaRepository pessoaRepository;
    private final IReservaRepository reservaRepository;

    public PessoaController(
            IPessoaRepository pessoaRepository,
            IReservaRepository reservaRepository) {

        this.pessoaRepository = pessoaRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/pessoas")
    public String listar(Model model) {
        model.addAttribute("pessoas", pessoaRepository.obterTodas());
        return "pessoa/index";
    }

    @GetMapping("/pessoas/novo")
    public String novo(Model model) {
        model.addAttribute("pessoa", new Pessoa());
        model.addAttribute("modoEdicao", false);
        return "pessoa/PessoaForm";
    }

    @PostMapping("/pessoas/novo")
    public String cadastrar(
            @Valid @ModelAttribute("pessoa") Pessoa pessoa,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validarCpf(pessoa, bindingResult, null);

        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicao", false);
            return "pessoa/PessoaForm";
        }

        pessoaRepository.adicionar(pessoa);
        redirectAttributes.addFlashAttribute("sucesso", "Pessoa cadastrada com sucesso.");
        return "redirect:/pessoas";
    }

    @GetMapping("/pessoas/{id}/editar")
    public String editar(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        return pessoaRepository.obterPorId(id)
                .map(pessoa -> {
                    model.addAttribute("pessoa", pessoa);
                    model.addAttribute("modoEdicao", true);
                    return "pessoa/PessoaForm";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Pessoa não encontrada.");
                    return "redirect:/pessoas";
                });
    }

    @PostMapping("/pessoas/{id}/editar")
    public String atualizar(
            @PathVariable int id,
            @Valid @ModelAttribute("pessoa") Pessoa pessoa,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        pessoa.setId(id);
        validarCpf(pessoa, bindingResult, id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicao", true);
            return "pessoa/PessoaForm";
        }

        if (pessoaRepository.obterPorId(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Pessoa não encontrada.");
            return "redirect:/pessoas";
        }

        pessoaRepository.atualizar(pessoa);
        redirectAttributes.addFlashAttribute("sucesso", "Cadastro atualizado com sucesso.");
        return "redirect:/pessoas";
    }

    @PostMapping("/pessoas/{id}/excluir")
    public String excluir(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (reservaRepository.existeReservaParaPessoa(id)) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não é possível excluir esta pessoa porque ela possui reserva vinculada."
            );
            return "redirect:/pessoas";
        }

        try {
            pessoaRepository.remover(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pessoa excluída com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/pessoas";
    }

    private void validarCpf(Pessoa pessoa, BindingResult bindingResult, Integer idIgnorado) {
        if (pessoa.getCpf() == null || pessoa.getCpf().isBlank()) {
            return;
        }

        String somenteNumeros = pessoa.getCpf().replaceAll("\\D", "");

        if (somenteNumeros.length() != 11) {
            bindingResult.rejectValue("cpf", "cpf.invalido", "O CPF deve conter 11 números.");
            return;
        }

        if (pessoaRepository.existeCpf(pessoa.getCpf(), idIgnorado)) {
            bindingResult.rejectValue("cpf", "cpf.duplicado", "Já existe uma pessoa cadastrada com este CPF.");
        }
    }
}
