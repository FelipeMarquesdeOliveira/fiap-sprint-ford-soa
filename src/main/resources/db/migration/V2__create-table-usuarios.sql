-- Usuários da API (autenticação JWT + controle de acesso por perfil)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    concessionaria_id BIGINT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    -- incrementado a cada logout/alteração de perfil: invalida os access tokens já emitidos
    versao_token INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_concessionaria FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id)
);

CREATE INDEX idx_usuarios_perfil ON usuarios(perfil);
