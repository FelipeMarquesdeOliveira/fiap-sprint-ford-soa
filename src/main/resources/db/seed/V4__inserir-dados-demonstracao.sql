-- Carga de demonstração (dados fictícios). Fica em db/seed para poder ser
-- removida de spring.flyway.locations em um ambiente produtivo.

INSERT INTO concessionarias (cnpj, nome, regiao, cidade, estado, ativa) VALUES
('11.222.333/0001-81', 'Ford Paulista Centro', 'Sudeste', 'São Paulo', 'SP', TRUE),
('44.555.666/0001-81', 'Ford Carioca Barra', 'Sudeste', 'Rio de Janeiro', 'RJ', TRUE),
('77.888.999/0001-81', 'Ford Mineira Savassi', 'Sudeste', 'Belo Horizonte', 'MG', TRUE),
('22.111.444/0001-37', 'Ford Gaúcha Moinhos', 'Sul', 'Porto Alegre', 'RS', TRUE);

INSERT INTO veiculos (vin, modelo, versao, ano_fabricacao, ano_modelo, cor, combustivel, valor_compra, tipo_veiculo) VALUES
('9BFZH55L8R8000001', 'Ranger', 'XLS 2.2 Diesel', 2024, 2024, 'Preto', 'Diesel', 229900.00, 'Picape'),
('9BFZH55L8R8000002', 'Ranger', 'Raptor 3.0 V6', 2024, 2025, 'Laranja', 'Gasolina', 459900.00, 'Picape'),
('9BFZTERR1R8000003', 'Territory', 'Titanium 1.5 Turbo', 2024, 2024, 'Branco', 'Gasolina', 214900.00, 'SUV'),
('9BFZMAVE2R8000004', 'Maverick', 'Lariat Hybrid', 2024, 2024, 'Azul', 'Híbrido', 249900.00, 'Picape'),
('9BFZBRNC3P8000005', 'Bronco Sport', 'Wildtrak 2.0', 2023, 2023, 'Verde', 'Gasolina', 279900.00, 'SUV'),
('9BFZMACH4R8000006', 'Mustang Mach-E', 'GT Performance', 2024, 2024, 'Vermelho', 'Elétrico', 486000.00, 'SUV'),
('9BFZTRAN5P8000007', 'Transit', 'Furgão 2.2 Diesel', 2023, 2023, 'Branco', 'Diesel', 289900.00, 'Van'),
('9BFZRANG6P8000008', 'Ranger', 'XLT 3.0 V6', 2023, 2023, 'Prata', 'Diesel', 309900.00, 'Picape'),
('9BFZF150RR8000009', 'F-150', 'Lariat 5.0 V8', 2024, 2024, 'Cinza', 'Gasolina', 549900.00, 'Picape'),
('9BFZMUST7P8000010', 'Mustang', 'GT 5.0 V8', 2023, 2023, 'Amarelo', 'Gasolina', 529000.00, 'Esportivo');

INSERT INTO clientes (cpf, nome, email, telefone, idade, sexo, regiao, data_compra, veiculo_id, concessionaria_id, perfil_cliente, ativa) VALUES
('529.982.247-25', 'Ana Beatriz Souza',   'ana.souza@email.test',    '(11) 98888-0001', 42, 'F', 'Sudeste', '2024-02-10', (SELECT id FROM veiculos WHERE vin = '9BFZH55L8R8000001'), (SELECT id FROM concessionarias WHERE cnpj = '11.222.333/0001-81'), 'FIEL', TRUE),
('111.444.777-35', 'Bruno Carvalho',      'bruno.carvalho@email.test','(11) 98888-0002', 35, 'M', 'Sudeste', '2024-03-15', (SELECT id FROM veiculos WHERE vin = '9BFZTERR1R8000003'), (SELECT id FROM concessionarias WHERE cnpj = '11.222.333/0001-81'), 'ECONOMICO', TRUE),
('123.456.789-09', 'Carla Mendes',        'carla.mendes@email.test',  '(11) 98888-0003', 29, 'F', 'Sudeste', '2024-01-20', (SELECT id FROM veiculos WHERE vin = '9BFZMAVE2R8000004'), (SELECT id FROM concessionarias WHERE cnpj = '11.222.333/0001-81'), 'ABANDONO', TRUE),
('987.654.321-00', 'Diego Ramos',         'diego.ramos@email.test',   '(21) 97777-0004', 51, 'M', 'Sudeste', '2023-11-05', (SELECT id FROM veiculos WHERE vin = '9BFZH55L8R8000002'), (SELECT id FROM concessionarias WHERE cnpj = '44.555.666/0001-81'), 'FIEL', TRUE),
('246.813.579-28', 'Eduarda Lima',        'eduarda.lima@email.test',  '(21) 97777-0005', 38, 'F', 'Sudeste', '2023-08-22', (SELECT id FROM veiculos WHERE vin = '9BFZBRNC3P8000005'), (SELECT id FROM concessionarias WHERE cnpj = '44.555.666/0001-81'), 'ESQUECIDO', TRUE),
('135.792.468-28', 'Felipe Andrade',      'felipe.andrade@email.test','(21) 97777-0006', 45, 'M', 'Sudeste', '2024-04-02', (SELECT id FROM veiculos WHERE vin = '9BFZMACH4R8000006'), (SELECT id FROM concessionarias WHERE cnpj = '44.555.666/0001-81'), 'ABANDONO', TRUE),
('314.159.265-90', 'Gabriela Nunes',      'gabriela.nunes@email.test','(31) 96666-0007', 33, 'F', 'Sudeste', '2023-06-18', (SELECT id FROM veiculos WHERE vin = '9BFZTRAN5P8000007'), (SELECT id FROM concessionarias WHERE cnpj = '77.888.999/0001-81'), 'ECONOMICO', TRUE),
('271.828.182-05', 'Henrique Batista',    'henrique.b@email.test',    '(31) 96666-0008', 57, 'M', 'Sudeste', '2023-05-30', (SELECT id FROM veiculos WHERE vin = '9BFZRANG6P8000008'), (SELECT id FROM concessionarias WHERE cnpj = '77.888.999/0001-81'), 'FIEL', TRUE),
('161.803.398-05', 'Isabela Rocha',       'isabela.rocha@email.test', '(51) 95555-0009', 40, 'F', 'Sul',     '2024-05-12', (SELECT id FROM veiculos WHERE vin = '9BFZF150RR8000009'), (SELECT id FROM concessionarias WHERE cnpj = '22.111.444/0001-37'), 'ESQUECIDO', TRUE),
('141.421.356-51', 'João Pedro Alves',    'joao.alves@email.test',    '(51) 95555-0010', 26, 'M', 'Sul',     '2023-09-09', (SELECT id FROM veiculos WHERE vin = '9BFZMUST7P8000010'), (SELECT id FROM concessionarias WHERE cnpj = '22.111.444/0001-37'), 'ABANDONO', TRUE);

-- Serviços na rede oficial: clientes FIEL/ECONOMICO retornam, ABANDONO/ESQUECIDO pouco ou nada
INSERT INTO servicos (cliente_id, veiculo_id, concessionaria_id, tipo_servico, data_servico, valor_servico, garantia_ativa, status_servico) VALUES
((SELECT id FROM clientes WHERE cpf = '529.982.247-25'), (SELECT veiculo_id FROM clientes WHERE cpf = '529.982.247-25'), (SELECT concessionaria_id FROM clientes WHERE cpf = '529.982.247-25'), 'Revisão 10.000 km', '2024-08-10', 890.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '529.982.247-25'), (SELECT veiculo_id FROM clientes WHERE cpf = '529.982.247-25'), (SELECT concessionaria_id FROM clientes WHERE cpf = '529.982.247-25'), 'Revisão 20.000 km', '2025-02-12', 1250.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '529.982.247-25'), (SELECT veiculo_id FROM clientes WHERE cpf = '529.982.247-25'), (SELECT concessionaria_id FROM clientes WHERE cpf = '529.982.247-25'), 'Revisão 30.000 km', '2025-08-15', 1480.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '111.444.777-35'), (SELECT veiculo_id FROM clientes WHERE cpf = '111.444.777-35'), (SELECT concessionaria_id FROM clientes WHERE cpf = '111.444.777-35'), 'Revisão 10.000 km', '2024-09-20', 790.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '111.444.777-35'), (SELECT veiculo_id FROM clientes WHERE cpf = '111.444.777-35'), (SELECT concessionaria_id FROM clientes WHERE cpf = '111.444.777-35'), 'Troca de pastilhas de freio', '2025-03-11', 640.00, FALSE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '987.654.321-00'), (SELECT veiculo_id FROM clientes WHERE cpf = '987.654.321-00'), (SELECT concessionaria_id FROM clientes WHERE cpf = '987.654.321-00'), 'Revisão 10.000 km', '2024-05-06', 1350.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '987.654.321-00'), (SELECT veiculo_id FROM clientes WHERE cpf = '987.654.321-00'), (SELECT concessionaria_id FROM clientes WHERE cpf = '987.654.321-00'), 'Revisão 20.000 km', '2024-11-08', 1720.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '987.654.321-00'), (SELECT veiculo_id FROM clientes WHERE cpf = '987.654.321-00'), (SELECT concessionaria_id FROM clientes WHERE cpf = '987.654.321-00'), 'Alinhamento e balanceamento', '2026-10-15', 380.00, FALSE, 'AGENDADO'),
((SELECT id FROM clientes WHERE cpf = '246.813.579-28'), (SELECT veiculo_id FROM clientes WHERE cpf = '246.813.579-28'), (SELECT concessionaria_id FROM clientes WHERE cpf = '246.813.579-28'), 'Revisão 10.000 km', '2024-03-01', 980.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '314.159.265-90'), (SELECT veiculo_id FROM clientes WHERE cpf = '314.159.265-90'), (SELECT concessionaria_id FROM clientes WHERE cpf = '314.159.265-90'), 'Revisão 20.000 km', '2024-07-19', 1150.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '314.159.265-90'), (SELECT veiculo_id FROM clientes WHERE cpf = '314.159.265-90'), (SELECT concessionaria_id FROM clientes WHERE cpf = '314.159.265-90'), 'Troca de óleo e filtros', '2025-01-22', 520.00, FALSE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '271.828.182-05'), (SELECT veiculo_id FROM clientes WHERE cpf = '271.828.182-05'), (SELECT concessionaria_id FROM clientes WHERE cpf = '271.828.182-05'), 'Revisão 30.000 km', '2024-12-03', 1640.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '271.828.182-05'), (SELECT veiculo_id FROM clientes WHERE cpf = '271.828.182-05'), (SELECT concessionaria_id FROM clientes WHERE cpf = '271.828.182-05'), 'Revisão 40.000 km', '2025-06-04', 1890.00, FALSE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '161.803.398-05'), (SELECT veiculo_id FROM clientes WHERE cpf = '161.803.398-05'), (SELECT concessionaria_id FROM clientes WHERE cpf = '161.803.398-05'), 'Revisão 10.000 km', '2025-05-20', 1420.00, TRUE, 'CONCLUIDO'),
((SELECT id FROM clientes WHERE cpf = '141.421.356-51'), (SELECT veiculo_id FROM clientes WHERE cpf = '141.421.356-51'), (SELECT concessionaria_id FROM clientes WHERE cpf = '141.421.356-51'), 'Revisão 10.000 km', '2024-03-15', 1600.00, TRUE, 'CANCELADO');

-- Usuários de demonstração (senhas com hash BCrypt):
--   admin@vinshare.test             / Admin@123     -> ADMIN
--   analista@vinshare.test          / Analista@123  -> ANALISTA
--   concessionaria.sp@vinshare.test / Conc@1234     -> CONCESSIONARIA (Ford Paulista Centro)
--   concessionaria.rj@vinshare.test / Conc@1234     -> CONCESSIONARIA (Ford Carioca Barra)
INSERT INTO usuarios (nome, email, senha, perfil, concessionaria_id, ativo, versao_token) VALUES
('Administrador Ford', 'admin@vinshare.test', '$2a$10$Z1dGyQSrdPjbL1An53VnFu.raNaIQ/kuEl78Cn9dcT0iQM08QGZ.m', 'ADMIN', NULL, TRUE, 0),
('Analista de Pós-Venda', 'analista@vinshare.test', '$2a$10$zwkc2PF9Y0BvGyM4XNq8xu4SbwYvMsLkjHd3vG0p63s1BfB/0b06W', 'ANALISTA', NULL, TRUE, 0),
('Gestor Ford Paulista', 'concessionaria.sp@vinshare.test', '$2a$10$6VJzPIZ57bty4wSiI9jDGuqBa8EVFdA9MlE98BpgYoDL4t5zV6ps.', 'CONCESSIONARIA', (SELECT id FROM concessionarias WHERE cnpj = '11.222.333/0001-81'), TRUE, 0),
('Gestor Ford Carioca', 'concessionaria.rj@vinshare.test', '$2a$10$6VJzPIZ57bty4wSiI9jDGuqBa8EVFdA9MlE98BpgYoDL4t5zV6ps.', 'CONCESSIONARIA', (SELECT id FROM concessionarias WHERE cnpj = '44.555.666/0001-81'), TRUE, 0);
