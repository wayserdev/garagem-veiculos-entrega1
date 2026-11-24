package br.edu.unirv.garagem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GaragemApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void deveCadastrarReservarImpedirDuplicacaoECancelar() throws Exception {
        String pessoaJson = mockMvc.perform(post("/api/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Joao Teste\",\"email\":\"joao.teste@exemplo.com\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode pessoa = objectMapper.readTree(pessoaJson);
        mockMvc.perform(get("/api/pessoas/" + pessoa.get("id").asLong()))
                .andExpect(status().isOk());

        String veiculoJson = mockMvc.perform(post("/api/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"placa\":\"XYZ2E45\",\"modelo\":\"Carro Teste\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode veiculo = objectMapper.readTree(veiculoJson);
        mockMvc.perform(get("/api/veiculos/" + veiculo.get("id").asLong()))
                .andExpect(status().isOk());

        String dados = "{\"pessoaId\":" + pessoa.get("id").asLong()
                + ",\"veiculoId\":" + veiculo.get("id").asLong() + "}";
        String reservaJson = mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON).content(dados))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.veiculo.disponivel").value(false))
                .andReturn().getResponse().getContentAsString();
        long reservaId = objectMapper.readTree(reservaJson).get("id").asLong();
        mockMvc.perform(get("/api/reservas/" + reservaId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(dados)).andExpect(status().isConflict());

        mockMvc.perform(put("/api/reservas/" + reservaId + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.veiculo.disponivel").value(true));

        mockMvc.perform(get("/api/reservas")).andExpect(status().isOk());
        mockMvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                .content(dados)).andExpect(status().isCreated());
    }

    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/pessoas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\",\"email\":\"ruim\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/veiculos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"placa\":\"ABC\",\"modelo\":\"Carro\"}"))
                .andExpect(status().isBadRequest());
    }
}
