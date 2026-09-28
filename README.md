# Garagem de Veículos — Entrega 1

Atividade prática da disciplina **Arquitetura de Software — ESW430 — UniRV**.

## Entrega 1 — Módulo Pessoas

Sistema web desenvolvido em **Java 17 + Spring Boot + Thymeleaf**, aplicando:

- MVC
- Repository Pattern
- Injeção de Dependência
- Princípios SOLID
- Persistência em arquivo JSON

## Funcionalidades

- Listagem de pessoas
- Cadastro de pessoa
- Edição de pessoa
- Exclusão com confirmação
- Persistência em `data/pessoas.json`
- Validação de nome
- Validação de e-mail
- Validação de CPF com 11 números
- Bloqueio de CPF duplicado
- Busca rápida na listagem
- Interface responsiva
- Dados mantidos após reiniciar a aplicação

## Estrutura principal

```text
src/main/java/br/edu/unirv/garagem/
├── controller/
│   └── PessoaController.java
├── model/
│   └── Pessoa.java
├── repository/
│   ├── IPessoaRepository.java
│   └── PessoaRepository.java
└── GaragemApplication.java
```

Fluxo da aplicação:

```text
View → Controller → Interface → Repository → JSON
```

O `PessoaController` depende apenas de `IPessoaRepository`, recebida por injeção de dependência pelo construtor. O acesso ao arquivo JSON fica restrito ao `PessoaRepository`.

## Requisitos

- Java 17 ou superior
- Maven 3.9 ou superior

## Como executar

Na pasta do projeto, execute:

```bash
mvn clean package
java -jar target/garagem-veiculos-1.0.0.jar
```

<<<<<<< HEAD
Ou, após gerar o pacote:

```bash
mvn clean package
java -jar target/garagem-veiculos-1.0.0.jar
```

=======
>>>>>>> 37f8bec (Adiciona prints e finaliza Entrega 1)
Depois acesse:

```text
http://localhost:8080
```

Também é possível executar com:

```bash
mvn spring-boot:run
```

## Persistência

Os dados são salvos em:

```text
data/pessoas.json
```

O repositório é a única classe responsável por ler e gravar esse arquivo.

## Integrante

- **Wayser Henrique de Faria Lopes**

## Ferramentas de IA utilizadas

- **ChatGPT** — apoio na estruturação, implementação, revisão e documentação do projeto.

## Prints da Entrega 1


Adicionar aqui os prints da listagem e do formulário de Pessoas, conforme solicitado na atividade.

Exemplo, caso os arquivos sejam adicionados na pasta `docs/`:

```md
![Listagem de Pessoas](docs/listagem-pessoas.png)

![Formulário de Pessoa](docs/formulario-pessoa.png)
```
### Listagem de Pessoas

![Listagem de Pessoas](docs/listagem-pessoas.png)

### Formulário de Pessoa

![Formulário de Pessoa](docs/formulario-pessoa.png)
>>>>>>> 37f8bec (Adiciona prints e finaliza Entrega 1)
