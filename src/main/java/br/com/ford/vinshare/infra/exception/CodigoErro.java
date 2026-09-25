package br.com.ford.vinshare.infra.exception;

import org.springframework.http.HttpStatus;

/**
 * Catálogo de erros da API. Cada código vira o campo "codigo" do Problem Details
 * e é documentado em docs/ERROS.md.
 */
public enum CodigoErro {
    DADOS_INVALIDOS(HttpStatus.BAD_REQUEST, "Dados inválidos"),
    REQUISICAO_INVALIDA(HttpStatus.BAD_REQUEST, "Requisição inválida"),
    NAO_AUTENTICADO(HttpStatus.UNAUTHORIZED, "Autenticação necessária"),
    CREDENCIAIS_INVALIDAS(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"),
    TOKEN_INVALIDO(HttpStatus.UNAUTHORIZED, "Token inválido"),
    TOKEN_EXPIRADO(HttpStatus.UNAUTHORIZED, "Token expirado"),
    TOKEN_REVOGADO(HttpStatus.UNAUTHORIZED, "Token revogado"),
    REFRESH_TOKEN_INVALIDO(HttpStatus.UNAUTHORIZED, "Refresh token inválido"),
    REFRESH_TOKEN_EXPIRADO(HttpStatus.UNAUTHORIZED, "Refresh token expirado"),
    REFRESH_TOKEN_REUTILIZADO(HttpStatus.UNAUTHORIZED, "Refresh token reutilizado"),
    ACESSO_NEGADO(HttpStatus.FORBIDDEN, "Acesso negado"),
    RECURSO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, "Recurso não encontrado"),
    METODO_NAO_PERMITIDO(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido"),
    FORMATO_NAO_ACEITAVEL(HttpStatus.NOT_ACCEPTABLE, "Formato de resposta não aceitável"),
    CONFLITO(HttpStatus.CONFLICT, "Conflito com o estado atual do recurso"),
    TIPO_DE_MIDIA_NAO_SUPORTADO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de mídia não suportado"),
    REGRA_DE_NEGOCIO(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada"),
    ERRO_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");

    private final HttpStatus status;
    private final String titulo;

    CodigoErro(HttpStatus status, String titulo) {
        this.status = status;
        this.titulo = titulo;
    }

    public HttpStatus status() {
        return status;
    }

    public String titulo() {
        return titulo;
    }

    /** Código padrão para erros gerados pelo próprio Spring MVC (404, 405, 415...). */
    public static CodigoErro porStatus(int status) {
        return switch (status) {
            case 400 -> REQUISICAO_INVALIDA;
            case 401 -> NAO_AUTENTICADO;
            case 403 -> ACESSO_NEGADO;
            case 404 -> RECURSO_NAO_ENCONTRADO;
            case 405 -> METODO_NAO_PERMITIDO;
            case 406 -> FORMATO_NAO_ACEITAVEL;
            case 409 -> CONFLITO;
            case 415 -> TIPO_DE_MIDIA_NAO_SUPORTADO;
            case 422 -> REGRA_DE_NEGOCIO;
            default -> ERRO_INTERNO;
        };
    }
}
