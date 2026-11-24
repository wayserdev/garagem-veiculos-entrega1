# Cliente Console — Projeto independente

O cliente usa apenas a API REST via HTTP. **Não** importa classes do servidor nem acessa o banco de dados.

A partir da raiz do repositório, com a API rodando, execute:

```bash
java cliente-console/src/main/java/GaragemCliente.java
```

Requer Java 17+. A variável `GARAGEM_API_URL` permite usar outro endereço da API (padrão `http://localhost:8080`).

Também é possível executar com Maven a partir de `cliente-console`: `mvn -q compile exec:java`.
