package br.com.ford.vinshare.infra.exception;

import br.com.ford.vinshare.domain.exception.AcessoNegadoException;
import br.com.ford.vinshare.domain.exception.ConflitoException;
import br.com.ford.vinshare.domain.exception.RecursoNaoEncontradoException;
import br.com.ford.vinshare.domain.exception.RegraDeNegocioException;
import br.com.ford.vinshare.infra.security.SecurityFilter;
import br.com.ford.vinshare.infra.security.TokenInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tratamento centralizado de erros. Toda resposta de erro da API segue o padrão
 * Problem Details (RFC 9457, application/problem+json), enriquecido com:
 * <ul>
 *   <li>{@code codigo}: identificador estável do erro (ver {@link CodigoErro});</li>
 *   <li>{@code timestamp}: instante do erro em UTC;</li>
 *   <li>{@code erros}: lista campo/mensagem em falhas de validação.</li>
 * </ul>
 * Stack traces e mensagens internas nunca são expostos ao cliente.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static final String TIPO_BASE = "https://github.com/FelipeMarquesdeOliveira/fiap-sprint-ford-soa/blob/main/docs/ERROS.md#";
    private static final String REALM = "Bearer realm=\"vinshare-api\"";

    // ------------------------------------------------------------------ domínio

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return responder(CodigoErro.RECURSO_NAO_ENCONTRADO, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> tratarConflito(ConflitoException ex, HttpServletRequest request) {
        return responder(CodigoErro.CONFLITO, ex.getMessage(), request);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ProblemDetail> tratarRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest request) {
        return responder(CodigoErro.REGRA_DE_NEGOCIO, ex.getMessage(), request);
    }

    /** Ordenação por propriedade inexistente (ex.: ?sort=campoQueNaoExiste). */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ProblemDetail> tratarOrdenacaoInvalida(PropertyReferenceException ex, HttpServletRequest request) {
        return responder(CodigoErro.REQUISICAO_INVALIDA,
                "Não é possível ordenar pela propriedade '" + ex.getPropertyName() + "'.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {} {}", request.getMethod(), request.getRequestURI());
        return responder(CodigoErro.CONFLITO,
                "A operação viola uma restrição de integridade dos dados (registro duplicado ou em uso).", request);
    }

    // ------------------------------------------------------------------ segurança

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ProblemDetail> tratarAcessoNegadoDominio(AcessoNegadoException ex, HttpServletRequest request) {
        registrarEventoSeguranca(HttpStatus.FORBIDDEN, request);
        return responder(CodigoErro.ACESSO_NEGADO, ex.getMessage(), request);
    }

    /** 403: autenticado, mas o perfil não tem permissão (@PreAuthorize ou regra de URL). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest request) {
        registrarEventoSeguranca(HttpStatus.FORBIDDEN, request);
        return responder(CodigoErro.ACESSO_NEGADO, "Seu perfil não tem permissão para acessar este recurso.", request);
    }

    /** 401: falha no login ou requisição sem token válido para endpoint protegido. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> tratarNaoAutenticado(AuthenticationException ex, HttpServletRequest request) {
        if (ex instanceof BadCredentialsException || ex instanceof AccountStatusException) {
            // Mesma mensagem para usuário inexistente, senha errada ou conta inativa (evita enumeração de usuários)
            log.warn("Falha de login a partir de {}", request.getRemoteAddr());
            return responder(CodigoErro.CREDENCIAIS_INVALIDAS, "E-mail ou senha inválidos.", request, REALM);
        }

        var erroToken = request.getAttribute(SecurityFilter.ATRIBUTO_ERRO_TOKEN) instanceof CodigoErro codigo
                ? codigo : CodigoErro.NAO_AUTENTICADO;
        registrarEventoSeguranca(HttpStatus.UNAUTHORIZED, request);

        return switch (erroToken) {
            case TOKEN_EXPIRADO -> responder(erroToken,
                    "O access token expirou. Renove-o em POST /auth/refresh ou faça login novamente.", request,
                    REALM + ", error=\"invalid_token\", error_description=\"The access token expired\"");
            case TOKEN_REVOGADO -> responder(erroToken,
                    "O access token foi revogado (logout, alteração de perfil ou usuário inativo). Faça login novamente.", request,
                    REALM + ", error=\"invalid_token\", error_description=\"The access token was revoked\"");
            case TOKEN_INVALIDO -> responder(erroToken,
                    "O access token é inválido (assinatura, formato, emissor ou audiência incorretos).", request,
                    REALM + ", error=\"invalid_token\", error_description=\"The access token is invalid\"");
            default -> responder(CodigoErro.NAO_AUTENTICADO,
                    "Este recurso exige autenticação. Envie o header Authorization: Bearer <token>.", request, REALM);
        };
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ProblemDetail> tratarRefreshToken(TokenInvalidoException ex, HttpServletRequest request) {
        return responder(ex.getCodigo(), ex.getMessage(), request, REALM + ", error=\"invalid_token\"");
    }

    // ------------------------------------------------------------------ erros do Spring MVC

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErroResponse.CampoInvalido> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(this::campoInvalido)
                .collect(Collectors.toList());
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> erros.add(new ErroResponse.CampoInvalido(e.getObjectName(), e.getDefaultMessage())));

        ProblemDetail problema = criarProblema(CodigoErro.DADOS_INVALIDOS,
                "Um ou mais campos estão inválidos. Veja a lista em 'erros'.", caminho(request));
        problema.setProperty("erros", erros);
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalhe = "Corpo da requisição ausente ou malformado.";
        if (ex.getMostSpecificCause() instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
            String campo = jackson.getPath().stream()
                    .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            detalhe = "Valor inválido ou em formato incorreto para o campo '" + campo + "'.";
        }
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(criarProblema(CodigoErro.REQUISICAO_INVALIDA, detalhe, caminho(request)));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String nome = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        String detalhe = "Valor '" + ex.getValue() + "' inválido para o parâmetro '" + nome + "'.";
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(criarProblema(CodigoErro.REQUISICAO_INVALIDA, detalhe, caminho(request)));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(criarProblema(CodigoErro.RECURSO_NAO_ENCONTRADO,
                        "Endpoint não encontrado: " + ex.getHttpMethod() + " /" + ex.getResourcePath(), caminho(request)));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String suportados = ex.getSupportedHttpMethods() == null ? "" : ex.getSupportedHttpMethods().stream()
                .map(HttpMethod::name).sorted().collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(criarProblema(CodigoErro.METODO_NAO_PERMITIDO,
                        "O método " + ex.getMethod() + " não é suportado por este recurso. Métodos suportados: " + suportados + ".",
                        caminho(request)));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).headers(headers)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(criarProblema(CodigoErro.TIPO_DE_MIDIA_NAO_SUPORTADO,
                        "Content-Type '" + ex.getContentType() + "' não suportado. Use application/json.", caminho(request)));
    }

    /** Demais exceções padrão do Spring MVC: mantém o status e padroniza título, código e timestamp. */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> resposta = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (resposta != null && resposta.getBody() instanceof ProblemDetail problema) {
            enriquecer(problema, CodigoErro.porStatus(statusCode.value()), caminho(request));
        }
        return resposta;
    }

    // ------------------------------------------------------------------ fallback

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(CodigoErro.ERRO_INTERNO, "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    // ------------------------------------------------------------------ utilitários

    private ResponseEntity<ProblemDetail> responder(CodigoErro codigo, String detalhe, HttpServletRequest request) {
        return responder(codigo, detalhe, request, null);
    }

    private ResponseEntity<ProblemDetail> responder(CodigoErro codigo, String detalhe, HttpServletRequest request,
                                                    @Nullable String wwwAuthenticate) {
        var builder = ResponseEntity.status(codigo.status()).contentType(MediaType.APPLICATION_PROBLEM_JSON);
        if (wwwAuthenticate != null) {
            builder.header(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
        }
        return builder.body(criarProblema(codigo, detalhe, request.getRequestURI()));
    }

    private ProblemDetail criarProblema(CodigoErro codigo, String detalhe, String caminho) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(codigo.status(), detalhe);
        enriquecer(problema, codigo, caminho);
        return problema;
    }

    private void enriquecer(ProblemDetail problema, CodigoErro codigo, String caminho) {
        problema.setType(URI.create(TIPO_BASE + codigo.name().toLowerCase()));
        problema.setTitle(codigo.titulo());
        problema.setInstance(URI.create(caminho));
        problema.setProperty("codigo", codigo.name());
        problema.setProperty("timestamp", Instant.now().toString());
    }

    private ErroResponse.CampoInvalido campoInvalido(FieldError erro) {
        return new ErroResponse.CampoInvalido(erro.getField(), erro.getDefaultMessage());
    }

    private String caminho(WebRequest request) {
        return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : "/";
    }

    private void registrarEventoSeguranca(HttpStatus status, HttpServletRequest request) {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        String usuario = autenticacao != null && autenticacao.isAuthenticated() ? autenticacao.getName() : "anônimo";
        log.warn("Evento de segurança: {} {} {} (usuário: {}, ip: {})",
                status.value(), request.getMethod(), request.getRequestURI(), usuario, request.getRemoteAddr());
    }
}
