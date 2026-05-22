# VIN Share API - Ford FIAP 2026

API RESTful para analise e predicao de VIN Share na rede de concessionarias Ford.

## Descricao do Projeto

Este projeto foi desenvolvido como parte do programa **Ford & FIAP 2026**, com o objetivo de ajudar a Ford a aumentar a retencao de clientes na rede oficial de manutencao, atraves de:

- **Analise de VIN Share**: Calculo do percentual de veiculos Ford que utilizam a rede oficial para manutencoes
- **Identificacao de Clientes em Risco**: Predicao de clientes com alta probabilidade de deixar a rede
- **Dashboard Gerencial**: Visualizacao de metricas por concessionaria, regiao e indicadores gerais

## Arquitetura

O projeto segue os principios de **Arquitetura Orientada a Servicos (SOA)** com separacao clara em camadas:

```
src/main/java/br/com/ford/vinshare/
├── controller/          # Camada de apresentacao (APIs REST)
├── domain/             # Camada de negocio (entidades, repositorios, DTOs)
│   ├── concessionaria/
│   ├── cliente/
│   ├── veiculo/
│   ├── servico/
│   └── vinshare/
└── infra/              # Infraestrutura (tratamento de excecoes, configuracoes)
```

### Padroes e Boas Praticas Aplicados

- **RESTful API**: Uso correto dos metodos HTTP (GET, POST, PUT, DELETE)
- **DTO Pattern**: Records Java para transferencia de dados
- **Repository Pattern**: Spring Data JPA para acesso a dados
- **Exception Handling**: Tratamento centralizado de erros com `@RestControllerAdvice`
- **Soft Delete**: Exclusao logica de registros (ativo = false)
- **Validacao de Entrada**: Bean Validation com anotacoes jakarta.validation
- **Paginacao**: Suporte a paginacao em listagens

## Tecnologias

| Tecnologia | Descricao |
|------------|-----------|
| Java 25 | Linguagem de programacao |
| Spring Boot 4.0.5 | Framework principal |
| Spring Data JPA | Persistencia de dados |
| Flyway | Controle de migracoes de banco de dados |
| MySQL | Banco de dados relacional |
| Lombok | Reducao de boilerplate |
| Springdoc OpenAPI | Documentacao automatica da API (Swagger) |

## Configuracao do Ambiente

### 1. Pre-requisitos

- Java 25+
- Maven 3.8+
- MySQL 8.0+

### 2. Banco de Dados

Criar o banco de dados MySQL:

```sql
CREATE DATABASE vinshare CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configuracao

O arquivo `src/main/resources/application.properties` ja esta configurado com:

```properties
spring.application.name=vinshare
server.port=8085

spring.datasource.url=jdbc:mysql://localhost:3306/vinshare
spring.datasource.username=root
spring.datasource.password=fiap
```

### 4. Execucao

```bash
# Compilar o projeto
./mvnw clean package

# Executar
./mvnw spring-boot:run
```

A API estara disponivel em: `http://localhost:8085`

## Documentacao da API (Swagger)

Apos iniciar a aplicacao, acesse:

- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8085/api-docs

## Endpoints Disponiveis

### Health Check

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/health-check` | Verifica se a API esta funcionando |

### Concessionarias

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/concessionarias` | Cadastrar nova concessionaria |
| GET | `/concessionarias` | Listar todas as concessionarias (paginado) |
| GET | `/concessionarias/{id}` | Buscar concessionaria por ID |
| PUT | `/concessionarias` | Atualizar concessionaria |
| DELETE | `/concessionarias/{id}` | Excluir concessionaria (soft delete) |

### Veiculos

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/veiculos` | Cadastrar novo veiculo |
| GET | `/veiculos` | Listar todos os veiculos (paginado) |
| GET | `/veiculos/{id}` | Buscar veiculo por ID |
| PUT | `/veiculos` | Atualizar veiculo |
| DELETE | `/veiculos/{id}` | Excluir veiculo |

### Clientes

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/clientes` | Cadastrar novo cliente |
| GET | `/clientes` | Listar todos os clientes (paginado) |
| GET | `/clientes/{id}` | Buscar cliente por ID |
| PUT | `/clientes` | Atualizar cliente |
| DELETE | `/clientes/{id}` | Excluir cliente (soft delete) |

### Servicos

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/servicos` | Cadastrar novo servico |
| GET | `/servicos` | Listar todos os servicos (paginado) |
| GET | `/servicos/{id}` | Buscar servico por ID |
| PUT | `/servicos` | Atualizar servico |
| DELETE | `/servicos/{id}` | Excluir servico |

### VIN Share (Analise)

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/vinshare/dashboard` | Dashboard completo com metricas |
| GET | `/vinshare/concessionaria/{id}` | VIN Share de uma concessionaria |
| GET | `/vinshare/concessionarias` | VIN Share de todas as concessionarias |
| GET | `/vinshare/clientes-risco` | Listar clientes em risco |
| GET | `/vinshare/clientes-risco?concessionariaId={id}` | Clientes em risco por concessionaria |

## Exemplos de Requisicoes

### Cadastrar Concessionaria

```bash
curl -X POST http://localhost:8085/concessionarias \
  -H "Content-Type: application/json" \
  -d '{
    "cnpj": "12.345.678/0001-90",
    "nome": "Ford SP Centro",
    "regiao": "Sudeste",
    "cidade": "Sao Paulo",
    "estado": "SP"
  }'
```

### Cadastrar Veiculo

```bash
curl -X POST http://localhost:8085/veiculos \
  -H "Content-Type: application/json" \
  -d '{
    "vin": "1HGBH41JXMN109186",
    "modelo": "Ford Ranger",
    "versao": "XLS 2.2 Diesel",
    "anoFabricacao": 2024,
    "anoModelo": 2024,
    "cor": "Preto",
    "combustivel": "Diesel",
    "valorCompra": 189900.00,
    "tipoVeiculo": "Caminhonete"
  }'
```

### Cadastrar Cliente

```bash
curl -X POST http://localhost:8085/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "123.456.789-00",
    "nome": "Joao Silva",
    "email": "joao.silva@email.com",
    "telefone": "(11) 99999-9999",
    "idade": 35,
    "sexo": "M",
    "regiao": "Sudeste",
    "dataCompra": "2024-01-15",
    "veiculoId": 1,
    "concessionariaId": 1,
    "perfilCliente": "FIEL"
  }'
```

### Cadastrar Servico

```bash
curl -X POST http://localhost:8085/servicos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "veiculoId": 1,
    "concessionariaId": 1,
    "tipoServico": "Revisao Periodica",
    "dataServico": "2024-03-15",
    "valorServico": 850.00,
    "garantiaAtiva": true,
    "statusServico": "CONCLUIDO"
  }'
```

### Consultar Dashboard

```bash
curl http://localhost:8085/vinshare/dashboard
```

### Consultar Clientes em Risco

```bash
curl http://localhost:8085/vinshare/clientes-risco
```

## Estrutura do Banco de Dados

### Tabelas

```sql
concessionarias
├── id (PK)
├── cnpj (UNIQUE)
├── nome
├── regiao
├── cidade
├── estado
└── ativa

veiculos
├── id (PK)
├── vin (UNIQUE)
├── modelo
├── versao
├── ano_fabricacao
├── ano_modelo
├── cor
├── combustivel
├── valor_compra
└── tipo_veiculo

clientes
├── id (PK)
├── cpf (UNIQUE)
├── nome
├── email
├── telefone
├── idade
├── sexo
├── regiao
├── data_compra
├── veiculo_id (FK)
├── concessionaria_id (FK)
├── perfil_cliente
└── ativa

servicos
├── id (PK)
├── cliente_id (FK)
├── veiculo_id (FK)
├── concessionaria_id (FK)
├── tipo_servico
├── data_servico
├── valor_servico
├── garantia_ativa
└── status_servico
```

## Perfis de Cliente

O sistema identifica 4 perfis de clientes baseados no comportamento de manutencao:

| Perfil | Descricao |
|--------|-----------|
| **FIEL** | Cliente fiel que retorna consistentemente para manutencao na rede oficial |
| **ABANDONO** | Cliente que faz apenas a primeira revisao e sai da rede |
| **ESQUECIDO** | Cliente que perde o timing da manutencao |
| **ECONOMICO** | Cliente sensivel a preco e promocoes |

## Estrutura de Arquivos

```
fiap-sprint-ford-soa/
├── pom.xml                                    # Configuracao Maven
├── src/
│   └── main/
│       ├── java/br/com/ford/vinshare/
│       │   ├── VinShareApplication.java      # Classe principal
│       │   ├── controller/                    # Controllers REST
│       │   │   ├── ClienteController.java
│       │   │   ├── ConcessionariaController.java
│       │   │   ├── HealthCheckController.java
│       │   │   ├── ServicoController.java
│       │   │   ├── VeiculoController.java
│       │   │   └── VinShareController.java
│       │   ├── domain/                       # Dominio da aplicacao
│       │   │   ├── cliente/
│       │   │   ├── concessionaria/
│       │   │   ├── servico/
│       │   │   ├── veiculo/
│       │   │   └── vinshare/
│       │   └── infra/                        # Infraestrutura
│       │       ├── GlobalExceptionHandler.java
│       │       └── OpenApiConfig.java
│       └── resources/
│           ├── application.properties
│           └── db/migration/
│               └── V1__create-tables.sql
└── README.md
```

## Equipe

Projeto desenvolvido para **Ford FIAP 2026** - Desafio 02: Impulsionando o VIN Share na America do Sul com Solucoes Inteligentes.

| Nome | RM |
|------|-----|
| Felipe Marques de Oliveira | 556319 |
| Gabriel Barros Cisoto | 556309 |

## Licenca

Para uso interno da Ford e FIAP.