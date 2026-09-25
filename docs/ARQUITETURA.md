# Arquitetura da solução - VIN Share API

Documento de arquitetura da Sprint 3 (Arquitetura Orientada a Serviços e Web Services).
Os diagramas abaixo usam Mermaid (renderizados pelo GitHub). As versões em imagem ficam em [`docs/arquitetura/`](arquitetura/).

## Sumário

1. [Contexto](#1-contexto)
2. [Diagrama de componentes](#2-diagrama-de-componentes)
3. [Organização e responsabilidades](#3-organização-e-responsabilidades)
4. [Fluxo de comunicação de uma requisição](#4-fluxo-de-comunicação-de-uma-requisição)
5. [Fluxo de autenticação](#5-fluxo-de-autenticação)
6. [JWT: estrutura, validação, expiração e revogação](#6-jwt-estrutura-validação-expiração-e-revogação)
7. [Autorização: perfis e escopo](#7-autorização-perfis-e-escopo)
8. [Modelo de dados](#8-modelo-de-dados)
9. [Decisões de arquitetura](#9-decisões-de-arquitetura)

---

## 1. Contexto

**Desafio 02 - Impulsionando o VIN Share na América do Sul.** O VIN Share é o percentual de veículos Ford que
continuam fazendo manutenção na rede oficial. A VIN Share API é o serviço central da solução: registra a rede de
concessionárias, os veículos (VIN), os clientes e os serviços de pós-venda, e calcula os indicadores de retenção
(VIN Share por concessionária e clientes com risco de evasão).

Consumidores previstos:

| Consumidor | Uso | Perfil |
|---|---|---|
| App mobile **Ford Guardian** (React Native/Expo, sprint de Mobile) | localizar concessionárias (endpoints públicos) e consultar dados do veículo | público / autenticado |
| Dashboard de pós-venda da Ford | indicadores de VIN Share da rede e clientes em risco | `ANALISTA`, `ADMIN` |
| Sistemas/operadores das concessionárias | registrar clientes e serviços, acompanhar o próprio VIN Share | `CONCESSIONARIA` |

## 2. Diagrama de componentes

![Diagrama de componentes](arquitetura/componentes.png)

<details>
<summary>Versão em Mermaid (editável)</summary>

```mermaid
flowchart TB
    subgraph consumidores["Consumidores da API"]
        direction LR
        app["App Ford Guardian<br/>React Native / Expo"]
        dash["Dashboard de pós-venda<br/>analistas Ford"]
        swagger["Swagger UI / Postman"]
    end

    subgraph api["VIN Share API - Spring Boot 4 / Java 25"]
        direction TB
        subgraph seguranca["1. Segurança - infra.security"]
            direction LR
            filtro["SecurityFilter<br/>autentica o JWT"]
            cadeia["SecurityFilterChain<br/>matriz de perfis e CORS"]
            token["TokenService<br/>JWT HS256 - java-jwt"]
        end
        controllers["2. Controllers REST - controller<br/>/auth /usuarios /concessionarias /veiculos<br/>/clientes /servicos /vinshare"]
        services["3. Services - service<br/>regras de negócio, transações e escopo por concessionária"]
        repos["4. Repositories - Spring Data JPA"]
        subgraph transversal["Transversais"]
            direction LR
            erros["GlobalExceptionHandler<br/>Problem Details - RFC 9457"]
            openapi["springdoc-openapi<br/>/api-docs e /swagger-ui.html"]
        end
    end

    db[("MySQL 8<br/>schema versionado com Flyway")]

    consumidores == "HTTPS + JSON<br/>Authorization: Bearer JWT" ==> filtro
    filtro --> cadeia
    filtro -. "valida assinatura, exp, iss, aud" .-> token
    cadeia ==> controllers ==> services ==> repos ==> db
    services -. "emite tokens no login e refresh" .-> token
    cadeia -. "401 / 403" .-> erros
    controllers -. "400 / 404 / 409 / 422" .-> erros
    openapi -. "documenta" .-> controllers
```

</details>

## 3. Organização e responsabilidades

A aplicação segue uma arquitetura em camadas, com dependência sempre no sentido
`controller -> service -> repository`. Nenhuma camada conhece a camada acima dela.

| Camada | Pacote | Responsabilidade | O que **não** faz |
|---|---|---|---|
| Segurança | `infra.security` | Autenticar o JWT (`SecurityFilter`), aplicar a matriz de perfis (`SecurityConfig`), emitir/validar tokens (`TokenService`), CORS | Regras de negócio |
| Apresentação (REST) | `controller` | Mapear recursos e verbos HTTP, validar a entrada (`@Valid`), definir status codes e headers (`Location`), documentar a API | Acessar o banco ou decidir regras |
| Serviço | `service` | Regras de negócio, escopo por concessionária, transações (`@Transactional`), cálculo do VIN Share | Conhecer HTTP (não usa `ResponseEntity`) |
| Domínio | `domain.*` | Entidades JPA com comportamento (`excluir()`, `atualizarInformacoes()`), DTOs imutáveis (`record`), enums e exceções de domínio | Depender de frameworks web |
| Persistência | `domain.*.*Repository` + `db/migration` | Acesso a dados com Spring Data JPA; versionamento do schema com Flyway | Regras de negócio |
| Tratamento de erros | `infra.exception` | Traduzir exceções para respostas Problem Details padronizadas | - |
| Documentação | `infra.openapi` | Contrato OpenAPI 3, esquema de segurança Bearer e respostas de erro padrão | - |

```
src/main/java/br/com/ford/vinshare
├── controller/          # REST: recursos, verbos, status codes
├── service/             # regras de negócio e escopo por concessionária
├── domain/
│   ├── autenticacao/    # refresh token, DTOs de login
│   ├── usuario/         # Usuario (UserDetails), Perfil
│   ├── concessionaria/  # entidade, repository, DTOs
│   ├── veiculo/
│   ├── cliente/
│   ├── servico/
│   ├── vinshare/        # DTOs dos indicadores
│   └── exception/       # exceções de domínio (404, 409, 422, 403)
└── infra/
    ├── security/        # SecurityConfig, SecurityFilter, TokenService
    ├── exception/       # GlobalExceptionHandler, catálogo de erros
    └── openapi/         # configuração do Swagger
```

## 4. Fluxo de comunicação de uma requisição

Exemplo: um usuário da concessionária de São Paulo tenta ler um cliente da concessionária do Rio de Janeiro.

![Fluxo de uma requisição](arquitetura/fluxo-requisicao.png)

```mermaid
sequenceDiagram
    autonumber
    actor C as Consumidor
    participant F as SecurityFilter
    participant UR as UsuarioRepository
    participant S as SecurityFilterChain
    participant CT as ClienteController
    participant SV as ClienteService
    participant R as ClienteRepository
    participant E as GlobalExceptionHandler

    C->>F: GET /clientes/4 com Authorization Bearer JWT
    F->>F: valida assinatura HS256, exp, nbf, iss, aud e tipo
    F->>UR: busca o usuário do claim sub
    UR-->>F: usuário ativo e versão do token igual ao claim ver
    F->>S: autenticado com ROLE_CONCESSIONARIA
    S->>S: matriz de perfis - GET /clientes/** exige autenticação
    S->>CT: encaminha a requisição
    CT->>SV: detalhar(4, usuário)
    SV->>R: findByIdAndAtivoTrue(4)
    R-->>SV: cliente da concessionária 2
    SV-->>E: AcessoNegadoException - cliente de outra concessionária
    E-->>C: 403 application/problem+json com codigo ACESSO_NEGADO
```

Pontos de decisão ao longo do fluxo:

| Onde | Verificação | Resposta em caso de falha |
|---|---|---|
| `SecurityFilter` | JWT válido, usuário ativo, versão do token atual | `401` `TOKEN_INVALIDO`, `TOKEN_EXPIRADO` ou `TOKEN_REVOGADO` |
| `SecurityFilterChain` | rota pública ou autenticada; perfil exigido pela rota | `401` `NAO_AUTENTICADO` / `403` `ACESSO_NEGADO` |
| Controller (`@Valid`) | formato e obrigatoriedade dos campos | `400` `DADOS_INVALIDOS` |
| Service | existência, duplicidade, escopo por concessionária, regras de negócio | `404`, `409`, `403`, `422` |

## 5. Fluxo de autenticação

![Fluxo de autenticação](arquitetura/fluxo-autenticacao.png)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário
    participant A as /auth
    participant AM as AuthenticationManager
    participant T as TokenService
    participant DB as MySQL
    participant R as Recurso protegido

    Note over U,R: 1. Login
    U->>A: POST /auth/login com email e senha
    A->>AM: authenticate(email, senha)
    AM->>DB: busca o usuário e compara o hash BCrypt
    AM-->>A: usuário autenticado ou 401 CREDENCIAIS_INVALIDAS
    A->>T: gerarAccessToken(usuário)
    T-->>A: JWT HS256 válido por 15 minutos
    A->>DB: grava o SHA-256 do refresh token (7 dias)
    A-->>U: 200 com accessToken, refreshToken e expiresIn 900

    Note over U,R: 2. Uso do access token
    U->>R: GET /clientes com Authorization Bearer accessToken
    R-->>U: 200, ou 401 TOKEN_EXPIRADO depois de 15 minutos

    Note over U,R: 3. Renovação com rotação
    U->>A: POST /auth/refresh com refreshToken
    A->>DB: localiza o hash e revoga o token usado
    A-->>U: 200 com um novo par de tokens
    Note over U,A: Refresh token já usado de novo:<br/>401 REFRESH_TOKEN_REUTILIZADO<br/>e todas as sessões do usuário são encerradas

    Note over U,R: 4. Logout
    U->>A: POST /auth/logout com Bearer accessToken
    A->>DB: revoga os refresh tokens e incrementa versao_token
    A-->>U: 204 - tokens anteriores passam a receber 401 TOKEN_REVOGADO
```

## 6. JWT: estrutura, validação, expiração e revogação

### Estrutura do access token

```json
// header
{ "alg": "HS256", "typ": "JWT" }
// payload
{
  "iss": "vinshare-api",
  "aud": "vinshare-clients",
  "sub": "3",
  "jti": "2f0b7c3e-5d0a-4c1e-9b8e-0c6f1d7a9e21",
  "iat": 1790311152,
  "nbf": 1790311152,
  "exp": 1790312052,
  "tipo": "access",
  "email": "concessionaria.sp@vinshare.test",
  "nome": "Gestor Ford Paulista",
  "perfil": "CONCESSIONARIA",
  "concessionariaId": 1,
  "ver": 0
}
```

| Claim | Uso na API |
|---|---|
| `sub` | Identifica o usuário; o `SecurityFilter` carrega o cadastro a partir dele |
| `iss` / `aud` | Garantem que o token foi emitido por esta API e para os clientes dela |
| `exp` / `nbf` / `iat` | Janela de validade (15 minutos, configurável em `api.security.jwt.access-token-expiration`) |
| `jti` | Identificador único de cada token (rastreabilidade) |
| `tipo` | Só tokens do tipo `access` são aceitos como credencial |
| `perfil`, `nome`, `email` | Informativos para o cliente (ex.: montar menus no app) |
| `concessionariaId` | Escopo do usuário `CONCESSIONARIA`: os services usam esse valor, e nunca o corpo da requisição, para definir a concessionária de clientes e serviços |
| `ver` | Versão do token: se for diferente da versão atual do usuário, o token foi revogado |

### Validações feitas a cada requisição

1. Header no formato `Authorization: Bearer <token>`.
2. Algoritmo fixo HS256 (tokens com `alg: none` ou outro algoritmo são rejeitados).
3. Assinatura com o segredo `JWT_SECRET` (mínimo de 32 bytes, verificado na inicialização).
4. `iss`, `aud` e `tipo` esperados; presença de `perfil` e `ver`.
5. `exp` e `nbf` (tolerância de 1 segundo).
6. Usuário do `sub` existe, está ativo e tem a mesma versão de token (`ver`).

As permissões são sempre derivadas do cadastro do usuário no servidor: um claim `perfil` adulterado não concede acesso
(coberto pelo teste `SegurancaJwtTest.perfilNaoPodeSerForjado`).

### Expiração e renovação

| Token | Formato | Validade | Armazenamento no servidor |
|---|---|---|---|
| Access token | JWT assinado (HS256) | 15 minutos | nenhum (stateless) |
| Refresh token | valor aleatório de 256 bits (Base64URL) | 7 dias | somente o hash SHA-256 (tabela `refresh_tokens`) |

- **Rotação:** cada `POST /auth/refresh` revoga o refresh token usado e entrega um novo par.
- **Detecção de reuso:** apresentar um refresh token já rotacionado indica vazamento; todas as sessões do usuário são revogadas (`REFRESH_TOKEN_REUTILIZADO`).
- **Revogação imediata:** logout, troca de perfil e desativação incrementam `versao_token`, invalidando access tokens ainda dentro da validade.

## 7. Autorização: perfis e escopo

| Perfil | Quem é | Pode |
|---|---|---|
| `ADMIN` | Equipe Ford | Tudo, inclusive gestão de usuários e da rede de concessionárias |
| `ANALISTA` | Analista de pós-venda Ford | Ler toda a rede e os indicadores de VIN Share; não altera dados |
| `CONCESSIONARIA` | Gestor de uma concessionária | Cadastrar veículos; gerir clientes e serviços **da própria concessionária**; ver o próprio VIN Share |

A autorização é aplicada em três camadas (defesa em profundidade):

1. **`SecurityFilterChain`** (antes do controller): rotas públicas e perfil exigido por rota. A requisição é barrada sem nem ler o corpo.
2. **`@PreAuthorize`** nos controllers: repete a regra junto do endpoint (documentação viva e segunda barreira).
3. **Services**: escopo por concessionária. Dados de outra concessionária retornam `403`.

Matriz completa de permissões: [README - Endpoints](../README.md#endpoints).

## 8. Modelo de dados

```mermaid
erDiagram
    CONCESSIONARIAS ||--o{ CLIENTES : "vende para"
    CONCESSIONARIAS ||--o{ SERVICOS : "executa"
    CONCESSIONARIAS |o--o{ USUARIOS : "é operada por"
    VEICULOS |o--o| CLIENTES : "pertence a"
    VEICULOS ||--o{ SERVICOS : "recebe"
    CLIENTES ||--o{ SERVICOS : "contrata"
    USUARIOS ||--o{ REFRESH_TOKENS : "possui"

    CONCESSIONARIAS {
        bigint id PK
        varchar cnpj UK
        varchar nome
        varchar regiao
        varchar cidade
        varchar estado
        boolean ativa
    }
    VEICULOS {
        bigint id PK
        varchar vin UK
        varchar modelo
        varchar versao
        int ano_fabricacao
        int ano_modelo
        decimal valor_compra
    }
    CLIENTES {
        bigint id PK
        varchar cpf UK
        varchar nome
        varchar perfil_cliente
        date data_compra
        bigint veiculo_id FK
        bigint concessionaria_id FK
        boolean ativa
    }
    SERVICOS {
        bigint id PK
        bigint cliente_id FK
        bigint veiculo_id FK
        bigint concessionaria_id FK
        varchar tipo_servico
        date data_servico
        decimal valor_servico
        varchar status_servico
    }
    USUARIOS {
        bigint id PK
        varchar email UK
        varchar senha "hash BCrypt"
        varchar perfil
        bigint concessionaria_id FK
        boolean ativo
        int versao_token
    }
    REFRESH_TOKENS {
        bigint id PK
        varchar token_hash UK "SHA-256"
        bigint usuario_id FK
        timestamp expira_em
        boolean revogado
    }
```

Migrações Flyway:

| Versão | Local | Conteúdo |
|---|---|---|
| V1 | `db/migration` | tabelas da Sprint 1 (concessionárias, veículos, clientes, serviços). Corrigido o índice que referenciava a coluna inexistente `ativo` |
| V2 | `db/migration` | `usuarios` (perfil, vínculo com concessionária, versão do token) |
| V3 | `db/migration` | `refresh_tokens` |
| V4 | `db/seed` | carga de demonstração (4 concessionárias, 10 veículos, 10 clientes, 15 serviços, 4 usuários) |

## 9. Decisões de arquitetura

| Decisão | Motivo |
|---|---|
| API stateless com JWT (sem sessão HTTP) | Escala horizontalmente e atende app mobile, web e integrações com o mesmo mecanismo |
| Access token curto (15 min) + refresh token opaco com rotação | Limita o impacto de um token vazado sem obrigar o usuário a logar a todo momento |
| Versão do token no cadastro do usuário (`ver`) | Permite revogar tokens JWT (logout, desativação, troca de perfil) mantendo o access token stateless |
| Matriz de perfis na `SecurityFilterChain` | Barra acessos antes do processamento do corpo: um perfil sem permissão sempre recebe `403`, nunca um `400` de validação |
| Escopo por concessionária no service, a partir do token | Evita que um usuário acesse ou crie dados de outra concessionária alterando o corpo da requisição (IDOR / OWASP API1) |
| Problem Details (RFC 9457) + catálogo de códigos | Um único formato de erro para toda a API, com `codigo` estável para o cliente tratar |
| `400` para validação, `422` para regra de negócio | Separa requisição malformada de requisição válida que viola uma regra do domínio |
| Exclusão lógica de concessionárias, clientes e usuários | Preserva o histórico de serviços usado no cálculo do VIN Share |
| H2 em memória para testes e perfil `dev` | Testes rápidos e isolados; o professor pode rodar a API sem instalar MySQL |
