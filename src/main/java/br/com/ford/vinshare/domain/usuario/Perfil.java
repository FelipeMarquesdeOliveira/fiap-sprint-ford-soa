package br.com.ford.vinshare.domain.usuario;

/**
 * Perfis de acesso da API. A autorização usa a authority "ROLE_" + nome do perfil.
 */
public enum Perfil {
    /** Equipe Ford: acesso total, gestão de usuários e da rede de concessionárias. */
    ADMIN,
    /** Analista de pós-venda Ford: leitura de toda a rede e indicadores de VIN Share. */
    ANALISTA,
    /** Gestor de uma concessionária: opera clientes e serviços apenas da própria concessionária. */
    CONCESSIONARIA
}
