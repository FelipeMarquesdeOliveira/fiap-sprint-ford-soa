# Catálogo de erros da API

Todas as respostas de erro seguem o padrão **Problem Details** ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457))
com `Content-Type: application/problem+json`. O campo `type` de cada resposta aponta para a seção correspondente deste documento.

```json
{
  "type": "https://github.com/FelipeMarquesdeOliveira/fiap-sprint-ford-soa/blob/main/docs/ERROS.md#dados_invalidos",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos. Veja a lista em 'erros'.",
  "instance": "/concessionarias",
  "codigo": "DADOS_INVALIDOS",
  "timestamp": "2026-09-25T04:39:13.182843Z",
  "erros": [
    { "campo": "cnpj", "mensagem": "CNPJ inválido" },
    { "campo": "estado", "mensagem": "Estado deve ser a sigla da UF com 2 letras maiúsculas (ex.: SP)" }
  ]
}
```

| Campo | Descrição |
|---|---|
| `type` | URI que identifica o tipo do problema (link para este catálogo) |
| `title` | Resumo legível do tipo de erro |
| `status` | Status HTTP (repetido no corpo) |
| `detail` | Explicação específica desta ocorrência |
| `instance` | Caminho da requisição que gerou o erro |
| `codigo` | Código estável para tratamento programático no cliente |
| `timestamp` | Instante do erro (UTC, ISO-8601) |
| `erros` | Apenas em `DADOS_INVALIDOS`: lista de `campo` / `mensagem` |

Garantias: stack traces, nomes de classes, SQL e mensagens internas **nunca** são enviados ao cliente
(`server.error.include-stacktrace=never` e tratamento genérico para `500`). Respostas `401` trazem o header
`WWW-Authenticate: Bearer realm="vinshare-api"` (RFC 6750), com `error="invalid_token"` quando há um token inválido.

## Códigos

| Código | Status | Quando ocorre |
|---|---|---|
| [`DADOS_INVALIDOS`](#dados_invalidos) | 400 | Falha de validação dos campos do corpo |
| [`REQUISICAO_INVALIDA`](#requisicao_invalida) | 400 | JSON malformado, tipo incorreto, enum inexistente, parâmetro inválido |
| [`NAO_AUTENTICADO`](#nao_autenticado) | 401 | Endpoint protegido sem token |
| [`CREDENCIAIS_INVALIDAS`](#credenciais_invalidas) | 401 | Login com e-mail/senha inválidos ou usuário inativo |
| [`TOKEN_INVALIDO`](#token_invalido) | 401 | JWT malformado, assinatura inválida, emissor/audiência incorretos |
| [`TOKEN_EXPIRADO`](#token_expirado) | 401 | JWT fora da validade (`exp`) |
| [`TOKEN_REVOGADO`](#token_revogado) | 401 | JWT emitido antes de logout, troca de perfil ou desativação |
| [`REFRESH_TOKEN_INVALIDO`](#refresh_token_invalido) | 401 | Refresh token desconhecido |
| [`REFRESH_TOKEN_EXPIRADO`](#refresh_token_expirado) | 401 | Refresh token com mais de 7 dias |
| [`REFRESH_TOKEN_REUTILIZADO`](#refresh_token_reutilizado) | 401 | Refresh token já rotacionado foi reapresentado |
| [`ACESSO_NEGADO`](#acesso_negado) | 403 | Perfil sem permissão ou dado de outra concessionária |
| [`RECURSO_NAO_ENCONTRADO`](#recurso_nao_encontrado) | 404 | Recurso ou rota inexistente |
| [`METODO_NAO_PERMITIDO`](#metodo_nao_permitido) | 405 | Verbo HTTP não suportado pelo recurso |
| [`FORMATO_NAO_ACEITAVEL`](#formato_nao_aceitavel) | 406 | Header `Accept` incompatível |
| [`CONFLITO`](#conflito) | 409 | Duplicidade (CNPJ, VIN, CPF, e-mail) ou recurso em uso |
| [`TIPO_DE_MIDIA_NAO_SUPORTADO`](#tipo_de_midia_nao_suportado) | 415 | `Content-Type` diferente de `application/json` |
| [`REGRA_DE_NEGOCIO`](#regra_de_negocio) | 422 | Requisição válida que viola uma regra do domínio |
| [`ERRO_INTERNO`](#erro_interno) | 500 | Falha inesperada (registrada no log do servidor) |

### DADOS_INVALIDOS
**400.** Um ou mais campos não passaram na validação (Bean Validation). Exemplos: CNPJ/CPF com dígito verificador
inválido, VIN fora do padrão ISO 3779, senha fraca, campos obrigatórios ausentes. A lista `erros` indica cada campo.

### REQUISICAO_INVALIDA
**400.** A requisição não pôde ser lida: corpo ausente, JSON malformado, valor com tipo incorreto (ex.: `"statusServico": "XPTO"`)
ou parâmetro de rota/query inválido (ex.: `GET /veiculos/abc`). Quando possível, o `detail` indica o campo.

### NAO_AUTENTICADO
**401.** O endpoint exige autenticação e a requisição não enviou o header `Authorization: Bearer <token>`.

### CREDENCIAIS_INVALIDAS
**401.** Login recusado. A mesma mensagem é usada para usuário inexistente, senha incorreta ou conta inativa,
para não revelar quais e-mails estão cadastrados.

### TOKEN_INVALIDO
**401.** O access token não é confiável: formato inválido, assinatura que não confere, `alg: none`, emissor (`iss`),
audiência (`aud`) ou tipo incorretos, ou header `Authorization` fora do padrão `Bearer`.

### TOKEN_EXPIRADO
**401.** O access token passou do `exp` (15 minutos). Obtenha um novo em `POST /auth/refresh`.

### TOKEN_REVOGADO
**401.** O token é autêntico, mas foi invalidado: o usuário fez logout, teve o perfil alterado ou foi desativado
(a versão `ver` do token não confere com a do cadastro).

### REFRESH_TOKEN_INVALIDO
**401.** O refresh token enviado não existe (ou o usuário está inativo). Faça login novamente.

### REFRESH_TOKEN_EXPIRADO
**401.** O refresh token passou da validade (7 dias). Faça login novamente.

### REFRESH_TOKEN_REUTILIZADO
**401.** Um refresh token já rotacionado foi usado de novo, o que indica possível vazamento. Por segurança, todas
as sessões do usuário são encerradas.

### ACESSO_NEGADO
**403.** O usuário está autenticado, mas não tem permissão: o perfil não pode executar a operação (ex.: `ANALISTA`
tentando criar uma concessionária) ou o recurso pertence a outra concessionária (perfil `CONCESSIONARIA`).

### RECURSO_NAO_ENCONTRADO
**404.** O recurso não existe ou foi excluído (exclusão lógica), ou a rota não existe.

### METODO_NAO_PERMITIDO
**405.** O recurso existe, mas não aceita o verbo usado. O header `Allow` informa os métodos suportados.

### FORMATO_NAO_ACEITAVEL
**406.** O cliente pediu (header `Accept`) um formato que a API não produz. Use `application/json`.

### CONFLITO
**409.** A operação conflita com o estado atual: CNPJ, VIN, CPF ou e-mail já cadastrados, veículo já vinculado
a outro cliente, ou exclusão de veículo que possui cliente/serviços vinculados.

### TIPO_DE_MIDIA_NAO_SUPORTADO
**415.** O corpo foi enviado com um `Content-Type` não suportado. Use `application/json`.

### REGRA_DE_NEGOCIO
**422.** A requisição está bem formada, mas viola uma regra do domínio. Exemplos: veículo que não pertence ao cliente,
serviço `CONCLUIDO` com data futura, alteração de serviço já finalizado, usuário `CONCESSIONARIA` sem concessionária,
administrador tentando remover o próprio perfil `ADMIN`.

### ERRO_INTERNO
**500.** Erro inesperado. O detalhe fica apenas no log do servidor; o cliente recebe uma mensagem genérica.
