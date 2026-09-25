package br.com.ford.vinshare.domain.servico;

public enum StatusServico {
    AGENDADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO;

    /** Status finais não podem mais ser alterados. */
    public boolean isFinal() {
        return this == CONCLUIDO || this == CANCELADO;
    }
}
