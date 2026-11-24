import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

/** Projeto cliente independente: comunica-se SOMENTE via HTTP com a API Spring Boot. */
public class GaragemCliente {
    private static final String API = System.getenv().getOrDefault("GARAGEM_API_URL", "http://localhost:8080");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public static void main(String[] args) {
        System.out.println("\n=== GARAGEM DE VEÍCULOS | CLIENTE CONSOLE ===");
        System.out.println("Servidor: " + API);
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            try {
                enviar("GET", "/api/status", null);
            } catch (Exception e) {
                System.out.println("Servidor indisponível. Inicie a API antes deste cliente.");
                System.out.println("Detalhe: " + e.getMessage());
                return;
            }
            while (true) {
                System.out.println("\n1 - Listar pessoas");
                System.out.println("2 - Cadastrar pessoa");
                System.out.println("3 - Listar veículos");
                System.out.println("4 - Cadastrar veículo");
                System.out.println("5 - Listar reservas");
                System.out.println("6 - Criar reserva");
                System.out.println("7 - Cancelar reserva");
                System.out.println("0 - Sair");
                System.out.print("Escolha: ");
                String escolha = scanner.nextLine().trim();
                try {
                    switch (escolha) {
                        case "1" -> enviar("GET", "/api/pessoas", null);
                        case "2" -> cadastrarPessoa(scanner);
                        case "3" -> enviar("GET", "/api/veiculos", null);
                        case "4" -> cadastrarVeiculo(scanner);
                        case "5" -> enviar("GET", "/api/reservas", null);
                        case "6" -> criarReserva(scanner);
                        case "7" -> cancelarReserva(scanner);
                        case "0" -> { System.out.println("Até logo!"); return; }
                        default -> System.out.println("Opção inválida.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Entrada inválida: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Falha ao comunicar com a API: " + e.getMessage());
                }
            }
        }
    }

    private static void cadastrarPessoa(Scanner sc) throws Exception {
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("E-mail: "); String email = sc.nextLine();
        enviar("POST", "/api/pessoas", "{\"nome\":" + json(nome) + ",\"email\":" + json(email) + "}");
    }

    private static void cadastrarVeiculo(Scanner sc) throws Exception {
        System.out.print("Placa (ABC1234 ou ABC1D23): "); String placa = sc.nextLine();
        System.out.print("Modelo: "); String modelo = sc.nextLine();
        enviar("POST", "/api/veiculos", "{\"placa\":" + json(placa) + ",\"modelo\":" + json(modelo) + "}");
    }

    private static void criarReserva(Scanner sc) throws Exception {
        System.out.println("Consulte os IDs de pessoas (opção 1) e veículos (opção 3) antes.");
        System.out.print("ID da pessoa: "); long pessoaId = lerId(sc.nextLine());
        System.out.print("ID do veículo: "); long veiculoId = lerId(sc.nextLine());
        enviar("POST", "/api/reservas", "{\"pessoaId\":" + pessoaId + ",\"veiculoId\":" + veiculoId + "}");
    }

    private static void cancelarReserva(Scanner sc) throws Exception {
        System.out.print("ID da reserva: "); long id = lerId(sc.nextLine());
        enviar("PUT", "/api/reservas/" + id + "/cancelar", null);
    }

    private static long lerId(String texto) {
        try {
            long id = Long.parseLong(texto.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("digite um ID numérico maior que zero");
        }
    }

    private static void enviar(String metodo, String rota, String corpo) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API + rota))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json");
        if (corpo != null) {
            builder.header("Content-Type", "application/json; charset=UTF-8");
            builder.method(metodo, HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        } else {
            builder.method(metodo, HttpRequest.BodyPublishers.noBody());
        }
        HttpResponse<String> resposta = HTTP.send(builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("\nHTTP " + resposta.statusCode() + " | " + metodo + " " + rota);
        System.out.println(formatarJson(resposta.body()));
    }

    /** Escapa corretamente os caracteres JSON (aspas, barra, quebras e controle). */
    private static String json(String texto) {
        StringBuilder resultado = new StringBuilder("\"");
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> resultado.append("\\\"");
                case '\\' -> resultado.append("\\\\");
                case '\n' -> resultado.append("\\n");
                case '\r' -> resultado.append("\\r");
                case '\t' -> resultado.append("\\t");
                default -> {
                    if (c < 0x20) resultado.append(String.format("\\u%04x", (int)c));
                    else resultado.append(c);
                }
            }
        }
        return resultado.append('"').toString();
    }

    /** Formata a resposta para leitura sem bibliotecas de terceiros. */
    private static String formatarJson(String bruto) {
        if (bruto == null || bruto.isBlank()) return "(sem conteúdo)";
        StringBuilder saida = new StringBuilder(); int nivel = 0; boolean aspas = false, escape = false;
        for (char c : bruto.toCharArray()) {
            if (escape) { saida.append(c); escape = false; continue; }
            if (aspas && c == '\\') { saida.append(c); escape = true; continue; }
            if (c == '"') { aspas = !aspas; saida.append(c); continue; }
            if (!aspas) {
                switch (c) {
                    case '{', '[' -> { saida.append(c).append('\n'); nivel++; identar(saida, nivel); }
                    case '}', ']' -> { saida.append('\n'); nivel = Math.max(0, nivel - 1); identar(saida, nivel); saida.append(c); }
                    case ',' -> { saida.append(c).append('\n'); identar(saida, nivel); }
                    case ':' -> saida.append(": ");
                    default -> { if (!Character.isWhitespace(c)) saida.append(c); }
                }
            } else saida.append(c);
        }
        return saida.toString();
    }

    private static void identar(StringBuilder s, int nivel) {
        s.append("  ".repeat(Math.min(nivel, 20)));
    }
}
