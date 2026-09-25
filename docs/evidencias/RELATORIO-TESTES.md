# Relatório de testes automatizados

Gerado em 25/09/2026 02:00 a partir de `target/surefire-reports` e `target/site/jacoco` com `python3 scripts/relatorio_testes.py` (após `./mvnw verify`).

## Resumo da execução

| Total | Aprovados | Falhas | Ignorados | Tempo |
|---:|---:|---:|---:|---:|
| 162 | 162 | 0 | 0 | 2.3s |

### Cenários cobertos

| Categoria | Testes |
|---|---:|
| Sucesso (2xx) | 57 |
| Erro de requisição/negócio (4xx) | 42 |
| Acesso não autorizado (401/403) | 47 |
| Unitário (sem HTTP) | 23 |

> Um teste pode verificar mais de uma categoria (ex.: `GET 200 / 403 / 404`).

### Cobertura de código (JaCoCo)

| Métrica | Cobertura |
|---|---:|
| Linhas | 92.8% (896/965) |
| Branches | 70.6% (199/282) |
| Instruções | 92.5% (4774/5161) |
| Métodos | 93.2% (260/279) |

## Testes por classe

| Classe | Testes | Aprovados |
|---|---:|---:|
| `controller.AutenticacaoControllerTest` | 15 | 15 |
| `controller.AutorizacaoPorPerfilTest` | 36 | 36 |
| `controller.ClienteControllerTest` | 12 | 12 |
| `controller.ConcessionariaControllerTest` | 12 | 12 |
| `controller.SegurancaJwtTest` | 22 | 22 |
| `controller.ServicoControllerTest` | 10 | 10 |
| `controller.TratamentoDeErrosTest` | 8 | 8 |
| `controller.UsuarioControllerTest` | 8 | 8 |
| `controller.VeiculoControllerTest` | 9 | 9 |
| `controller.VinShareControllerTest` | 5 | 5 |
| `documentacao.OpenApiDocumentacaoTest` | 2 | 2 |
| `domain.RegrasDeDominioTest` | 6 | 6 |
| `infra.security.TokenServiceTest` | 12 | 12 |
| `service.VinShareServiceTest` | 5 | 5 |

### POST /auth/logout
`controller.AutenticacaoControllerTest`

**POST /auth/logout**

- [OK] 204: logout revoga o access token e o refresh token imediatamente
- [OK] 401: logout exige autenticação
**POST /auth/login**

- [OK] 401: usuário desativado não consegue autenticar
- [OK] 400: corpo com e-mail inválido e senha vazia lista os campos com erro
- [OK] 415: Content-Type diferente de JSON
- [OK] 401: senha incorreta
- [OK] 401: usuário inexistente recebe a mesma resposta (sem enumeração de usuários)
- [OK] 200: e-mail não diferencia maiúsculas/minúsculas e o token do perfil CONCESSIONARIA traz a concessionária
- [OK] 400: JSON malformado
- [OK] 200: credenciais válidas retornam access token, refresh token e dados do usuário
**POST /auth/refresh**

- [OK] 401: refresh token desconhecido
- [OK] 401: refresh token expirado
- [OK] 200: refresh token válido gera um novo par e o anterior é revogado (rotação)
- [OK] 401: reutilizar um refresh token já usado encerra todas as sessões do usuário
- [OK] 400: refresh token ausente


### Autorização por perfil (ADMIN, ANALISTA, CONCESSIONARIA)
`controller.AutorizacaoPorPerfilTest`

**Autorização por perfil (ADMIN, ANALISTA, CONCESSIONARIA)**

- [OK] matrizDePermissoes(String, String, String, int) "200": "ADMIN" "GET" "/usuarios"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "GET" "/usuarios"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "GET" "/usuarios"
- [OK] matrizDePermissoes(String, String, String, int) "400": "ADMIN" "POST" "/usuarios"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "POST" "/usuarios"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "DELETE" "/usuarios/2"
- [OK] matrizDePermissoes(String, String, String, int) "400": "ADMIN" "POST" "/concessionarias"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "POST" "/concessionarias"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "POST" "/concessionarias"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "PATCH" "/concessionarias/1"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "DELETE" "/concessionarias/1"
- [OK] matrizDePermissoes(String, String, String, int) "400": "ADMIN" "POST" "/veiculos"
- [OK] matrizDePermissoes(String, String, String, int) "400": "CONCESSIONARIA" "POST" "/veiculos"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "POST" "/veiculos"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "DELETE" "/veiculos/1"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ANALISTA" "GET" "/veiculos"
- [OK] matrizDePermissoes(String, String, String, int) "400": "ADMIN" "POST" "/clientes"
- [OK] matrizDePermissoes(String, String, String, int) "400": "CONCESSIONARIA" "POST" "/clientes"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "POST" "/clientes"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "PATCH" "/clientes/1"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ANALISTA" "GET" "/clientes"
- [OK] matrizDePermissoes(String, String, String, int) "400": "CONCESSIONARIA" "POST" "/servicos"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "POST" "/servicos"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "DELETE" "/servicos/1"
- [OK] matrizDePermissoes(String, String, String, int) "403": "ANALISTA" "DELETE" "/servicos/1"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ANALISTA" "GET" "/servicos"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ADMIN" "GET" "/vinshare/dashboard"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ANALISTA" "GET" "/vinshare/dashboard"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "GET" "/vinshare/dashboard"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ANALISTA" "GET" "/vinshare/concessionarias"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "GET" "/vinshare/concessionarias"
- [OK] matrizDePermissoes(String, String, String, int) "200": "CONCESSIONARIA" "GET" "/vinshare/concessionarias/1"
- [OK] matrizDePermissoes(String, String, String, int) "403": "CONCESSIONARIA" "GET" "/vinshare/concessionarias/2"
- [OK] matrizDePermissoes(String, String, String, int) "200": "CONCESSIONARIA" "GET" "/vinshare/clientes-risco"
- [OK] matrizDePermissoes(String, String, String, int) "200": "CONCESSIONARIA" "GET" "/usuarios/me"
- [OK] matrizDePermissoes(String, String, String, int) "200": "ANALISTA" "GET" "/usuarios/me"


### Clientes
`controller.ClienteControllerTest`

**Clientes - /clientes (escopo por concessionária)**

- [OK] GET 200: ADMIN e ANALISTA veem todos os clientes
- [OK] GET 200 / 403 / 404: detalhe respeita o escopo da concessionária
- [OK] DELETE 204: exclusão lógica do cliente; depois 404
- [OK] POST 409: CPF duplicado ou veículo já vinculado a outro cliente
- [OK] PUT 200 e PATCH 200 em cliente próprio
- [OK] POST 400: CPF com dígito verificador inválido, e-mail e sexo inválidos
- [OK] GET 200: CONCESSIONARIA vê apenas os clientes da própria concessionária
- [OK] POST 422: veículo inexistente
- [OK] POST 403: CONCESSIONARIA não cadastra cliente para outra concessionária
- [OK] POST 201: para CONCESSIONARIA a concessionária vem do token, não do corpo
- [OK] POST 422: ADMIN precisa informar a concessionária
- [OK] PATCH 403: cliente de outra concessionária


### Concessionárias
`controller.ConcessionariaControllerTest`

**Concessionárias - /concessionarias**

- [OK] 403: perfil sem permissão de escrita
- [OK] POST 409: CNPJ já cadastrado
- [OK] PUT 400: representação incompleta é rejeitada (PUT não é atualização parcial)
- [OK] DELETE 204: exclusão lógica; depois o recurso responde 404
- [OK] PATCH 200: altera apenas os campos enviados
- [OK] PUT 200: substitui a representação completa
- [OK] GET 200: lista paginada e ordenada por nome, sem autenticação
- [OK] POST 400: CNPJ com dígito verificador inválido, nome vazio e UF inválida
- [OK] POST 201: cria e devolve Location apontando para o novo recurso
- [OK] GET 200: filtro por UF
- [OK] GET 200 / 404: detalhe por id
- [OK] PUT 409: CNPJ pertencente a outra concessionária


### Proteção de recursos com JWT
`controller.SegurancaJwtTest`

**Proteção de recursos com JWT**

- [OK] 401: header Authorization malformado 401: header Authorization ""Bearer isto-nao-e-jwt""
- [OK] 401: header Authorization malformado 401: header Authorization ""Basic YWRtaW46YWRtaW4=""
- [OK] 401: header Authorization malformado 401: header Authorization ""Token abc""
- [OK] 401: escrita em recurso de leitura pública também exige token
- [OK] 401: token emitido para outra audiência
- [OK] 403: claim 'perfil' forjado não concede privilégios (permissões vêm do cadastro do usuário)
- [OK] 401: token de usuário desativado deixa de valer imediatamente
- [OK] 200: token válido dá acesso e o usuário é identificado pelo claim sub
- [OK] 401: token sem assinatura (alg none)
- [OK] 200: endpoints públicos não exigem token 200: GET "/health-check" é público
- [OK] 200: endpoints públicos não exigem token 200: GET "/concessionarias" é público
- [OK] 200: endpoints públicos não exigem token 200: GET "/concessionarias/1" é público
- [OK] 200: endpoints públicos não exigem token 200: GET "/api-docs" é público
- [OK] 401: token assinado com outro segredo
- [OK] 401: token expirado
- [OK] 401: endpoints protegidos exigem token 401: GET "/clientes" sem token
- [OK] 401: endpoints protegidos exigem token 401: GET "/veiculos" sem token
- [OK] 401: endpoints protegidos exigem token 401: GET "/servicos" sem token
- [OK] 401: endpoints protegidos exigem token 401: GET "/usuarios/me" sem token
- [OK] 401: endpoints protegidos exigem token 401: GET "/vinshare/dashboard" sem token
- [OK] 401: endpoints protegidos exigem token 401: GET "/vinshare/clientes-risco" sem token
- [OK] 200: endpoint público continua acessível mesmo com token inválido


### Serviços
`controller.ServicoControllerTest`

**Serviços - /servicos (regras de negócio e escopo)**

- [OK] GET 403: serviço executado por outra concessionária
- [OK] PATCH 200: agendamento é concluído; depois o serviço finalizado não aceita alterações (422)
- [OK] DELETE 204 (ADMIN) e 404 para serviço inexistente
- [OK] POST 400: campos obrigatórios ausentes e valor negativo
- [OK] POST 422: serviço CONCLUIDO com data futura
- [OK] POST 201: concessionária registra serviço para o próprio cliente
- [OK] GET 200: filtro por status via query string; 400 para status inexistente
- [OK] GET 200: ADMIN vê todos; CONCESSIONARIA apenas os que executou
- [OK] POST 422: veículo que não pertence ao cliente
- [OK] PUT 422: serviço CANCELADO é imutável


### Padronização das respostas de erro (Problem Details)
`controller.TratamentoDeErrosTest`

**Padronização das respostas de erro (Problem Details)**

- [OK] 400: ordenação por propriedade inexistente
- [OK] 405: método não suportado informa os métodos permitidos no header Allow
- [OK] 400: valor de enum inválido no corpo indica o campo
- [OK] 404: rota inexistente (autenticado) segue o mesmo formato
- [OK] 200: tamanho de página limitado a 100 itens
- [OK] 400: erros de validação trazem a lista campo/mensagem em português
- [OK] 400: corpo ausente
- [OK] 400: parâmetro de rota com tipo inválido


### Usuários
`controller.UsuarioControllerTest`

**Usuários - /usuarios (gestão restrita ao ADMIN)**

- [OK] POST 201: cria usuário com senha armazenada em BCrypt e ele consegue fazer login
- [OK] PATCH 200: mudar o perfil de um usuário revoga os tokens que ele já tinha
- [OK] POST 422: perfil CONCESSIONARIA exige concessionária existente
- [OK] GET 200: listagem para ADMIN, sem expor senhas
- [OK] PATCH / DELETE 422: ADMIN não pode rebaixar nem desativar a si mesmo
- [OK] POST 400: senha fraca
- [OK] DELETE 204: desativa o usuário e bloqueia o token dele; 404 para id inexistente
- [OK] POST 409: e-mail já cadastrado (sem diferenciar maiúsculas)


### Veículos
`controller.VeiculoControllerTest`

**Veículos - /veiculos**

- [OK] DELETE 204: veículo sem vínculos é removido e passa a responder 404
- [OK] POST 400: VIN fora do padrão ISO 3779 (contém a letra O) e valor negativo
- [OK] GET 200: lista paginada e filtro por modelo
- [OK] PUT 200 e PATCH 200: substituição completa e atualização parcial
- [OK] POST 201: perfil CONCESSIONARIA cadastra veículo e recebe Location
- [OK] GET 200 / 404: detalhe por id
- [OK] GET 200: sub-recurso com o histórico de serviços do veículo (mais recente primeiro)
- [OK] DELETE 409: veículo com cliente/serviços vinculados não pode ser excluído
- [OK] POST 409: VIN já cadastrado


### VIN Share
`controller.VinShareControllerTest`

**VIN Share - /vinshare**

- [OK] GET 200: VIN Share por concessionária
- [OK] GET 200 / 403 / 404: CONCESSIONARIA consulta apenas o próprio VIN Share
- [OK] GET 200: clientes em risco com nível e CPF mascarado (LGPD)
- [OK] GET 200: dashboard consolida a rede (7 de 10 veículos voltaram = 70%)
- [OK] 201 / 200: fluxo completo - registrar a revisão de um cliente em risco eleva o VIN Share da concessionária para 100%


### Documentação OpenAPI / Swagger
`documentacao.OpenApiDocumentacaoTest`

**Documentação OpenAPI / Swagger**

- [OK] 200: Swagger UI disponível sem autenticação
- [OK] 200: contrato OpenAPI 3 publicado em /api-docs com segurança JWT e erros padronizados


### Regras de domínio
`domain.RegrasDeDominioTest`

**Regras de domínio**

- [OK] CONCESSIONARIA só acessa a própria concessionária; ADMIN e ANALISTA acessam todas
- [OK] authority do Spring Security segue o padrão ROLE_<PERFIL>
- [OK] refresh token expira no instante configurado e pode ser revogado
- [OK] perfis diferentes de CONCESSIONARIA não ficam vinculados a uma concessionária
- [OK] trocar perfil ou desativar incrementa a versão do token (revoga tokens emitidos)
- [OK] CONCLUIDO e CANCELADO são status finais


### TokenService
`infra.security.TokenServiceTest`

**TokenService - geração e validação de JWT**

- [OK] rejeita token assinado com outro segredo
- [OK] rejeita token expirado
- [OK] valida um token íntegro e devolve seus claims
- [OK] rejeita texto que não é um JWT
- [OK] não coloca a senha nem o hash da senha no token
- [OK] gera token com emissor, audiência, subject, perfil, versão, jti e expiração de 15 minutos
- [OK] rejeita token que não é do tipo access
- [OK] rejeita token com payload adulterado (ex.: troca de perfil)
- [OK] rejeita token sem assinatura (alg: none)
- [OK] não inicializa com segredo menor que 32 bytes
- [OK] inclui o claim concessionariaId apenas para o perfil CONCESSIONARIA
- [OK] rejeita token de outro emissor ou para outra audiência


### VinShareService
`service.VinShareServiceTest`

**VinShareService - cálculo de VIN Share e clientes em risco**

- [OK] VIN Share = veículos com serviço concluído / veículos vendidos, com 2 casas decimais
- [OK] dashboard consolida a rede inteira
- [OK] usuário CONCESSIONARIA recebe apenas os clientes em risco da própria concessionária
- [OK] concessionária sem vendas tem VIN Share 0 (sem divisão por zero)
- [OK] cliente sem serviço é risco ALTO; com um serviço é risco MEDIO; com dois ou mais não está em risco

