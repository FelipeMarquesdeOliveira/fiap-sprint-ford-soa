CREATE TABLE concessionarias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    regiao VARCHAR(100) NOT NULL,
    cidade VARCHAR(100),
    estado VARCHAR(50),
    ativa BOOLEAN DEFAULT TRUE
);

CREATE TABLE veiculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(17) NOT NULL UNIQUE,
    modelo VARCHAR(100) NOT NULL,
    versao VARCHAR(100) NOT NULL,
    ano_fabricacao INTEGER,
    ano_modelo INTEGER,
    cor VARCHAR(50),
    combustivel VARCHAR(50),
    valor_compra DECIMAL(12,2),
    tipo_veiculo VARCHAR(50)
);

CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(20),
    idade INTEGER,
    sexo VARCHAR(1),
    regiao VARCHAR(100),
    data_compra DATE,
    veiculo_id BIGINT,
    concessionaria_id BIGINT,
    perfil_cliente VARCHAR(50),
    ativa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id)
);

CREATE TABLE servicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    concessionaria_id BIGINT NOT NULL,
    tipo_servico VARCHAR(100) NOT NULL,
    data_servico DATE,
    valor_servico DECIMAL(10,2),
    garantia_ativa BOOLEAN DEFAULT TRUE,
    status_servico VARCHAR(50),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    FOREIGN KEY (concessionaria_id) REFERENCES concessionarias(id)
);

CREATE INDEX idx_clientes_cpf ON clientes(cpf);
CREATE INDEX idx_clientes_perfil ON clientes(perfil_cliente);
CREATE INDEX idx_clientes_ativo ON clientes(ativo);
CREATE INDEX idx_veiculos_vin ON veiculos(vin);
CREATE INDEX idx_servicos_cliente ON servicos(cliente_id);
CREATE INDEX idx_servicos_data ON servicos(data_servico);