package br.com.ford.vinshare.domain.cliente;

/** Perfis comportamentais de pós-venda definidos no Desafio 02 (hipótese de segmentação da Ford). */
public enum PerfilCliente {
    /** Retorna consistentemente à rede oficial para manutenção. */
    FIEL,
    /** Faz no máximo a primeira revisão e deixa a rede. */
    ABANDONO,
    /** Perde o timing da manutenção e se frustra com a marca. */
    ESQUECIDO,
    /** Mantém relacionamento, mas é sensível a preço e promoções. */
    ECONOMICO
}
