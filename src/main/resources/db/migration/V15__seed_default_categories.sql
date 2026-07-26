-- ===========================================
-- Default Categories and Subcategories
--
-- This migration seeds the default catalog
-- used by every Finance Family installation.
-- ===========================================

-- Categories

INSERT INTO categories (id, name, type) VALUES
    ('6aa5b3de-8ec9-4c7f-a001-000000000001', 'Receita', 'INCOME'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000002', 'Alimentação', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000003', 'Moradia', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000004', 'Transporte', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000005', 'Saúde', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000006', 'Educação', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000007', 'Lazer', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000008', 'Assinaturas', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000009', 'Investimentos', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000010', 'Financeiro', 'EXPENSE'),
    ('6aa5b3de-8ec9-4c7f-a001-000000000011', 'Outros', 'EXPENSE');

-- Subcategories

INSERT INTO sub_categories (id, name, category_id) VALUES

-- Receita
    ('7bb5b3de-8ec9-4c7f-a001-000000000001','Salário','6aa5b3de-8ec9-4c7f-a001-000000000001'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000002','PLR','6aa5b3de-8ec9-4c7f-a001-000000000001'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000003','13º Salário','6aa5b3de-8ec9-4c7f-a001-000000000001'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000004','Férias','6aa5b3de-8ec9-4c7f-a001-000000000001'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000005','Rendimentos','6aa5b3de-8ec9-4c7f-a001-000000000001'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000006','Restituição IR','6aa5b3de-8ec9-4c7f-a001-000000000001'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000007','Outras Receitas','6aa5b3de-8ec9-4c7f-a001-000000000001'),

-- Alimentação
    ('7bb5b3de-8ec9-4c7f-a001-000000000008','Supermercado','6aa5b3de-8ec9-4c7f-a001-000000000002'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000009','Restaurante','6aa5b3de-8ec9-4c7f-a001-000000000002'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000010','Delivery','6aa5b3de-8ec9-4c7f-a001-000000000002'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000011','Café','6aa5b3de-8ec9-4c7f-a001-000000000002'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000012','Outros','6aa5b3de-8ec9-4c7f-a001-000000000002'),

-- Moradia
    ('7bb5b3de-8ec9-4c7f-a001-000000000013','Aluguel','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000014','Condomínio','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000015','Energia','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000016','Água','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000017','Gás','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000018','IPTU','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000019','Internet','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000020','Telefone','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000021','Serviços','6aa5b3de-8ec9-4c7f-a001-000000000003'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000022','Manutenção Residencial','6aa5b3de-8ec9-4c7f-a001-000000000003'),

-- Transporte
    ('7bb5b3de-8ec9-4c7f-a001-000000000023','Combustível','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000024','Uber','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000025','Estacionamento','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000026','Manutenção','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000027','Seguro','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000028','IPVA','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000029','Licenciamento','6aa5b3de-8ec9-4c7f-a001-000000000004'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000030','Multa','6aa5b3de-8ec9-4c7f-a001-000000000004'),

-- Saúde
    ('7bb5b3de-8ec9-4c7f-a001-000000000031','Plano de Saúde','6aa5b3de-8ec9-4c7f-a001-000000000005'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000032','Farmácia','6aa5b3de-8ec9-4c7f-a001-000000000005'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000033','Consultas','6aa5b3de-8ec9-4c7f-a001-000000000005'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000034','Exames','6aa5b3de-8ec9-4c7f-a001-000000000005'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000035','Academia','6aa5b3de-8ec9-4c7f-a001-000000000005'),

-- Educação
    ('7bb5b3de-8ec9-4c7f-a001-000000000036','Faculdade','6aa5b3de-8ec9-4c7f-a001-000000000006'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000037','Cursos','6aa5b3de-8ec9-4c7f-a001-000000000006'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000038','Livros','6aa5b3de-8ec9-4c7f-a001-000000000006'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000039','Certificações','6aa5b3de-8ec9-4c7f-a001-000000000006'),

-- Lazer
    ('7bb5b3de-8ec9-4c7f-a001-000000000040','Cinema','6aa5b3de-8ec9-4c7f-a001-000000000007'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000041','Viagens','6aa5b3de-8ec9-4c7f-a001-000000000007'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000042','Passeios','6aa5b3de-8ec9-4c7f-a001-000000000007'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000043','Streaming','6aa5b3de-8ec9-4c7f-a001-000000000007'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000044','Outros','6aa5b3de-8ec9-4c7f-a001-000000000007'),

-- Assinaturas
    ('7bb5b3de-8ec9-4c7f-a001-000000000045','Netflix','6aa5b3de-8ec9-4c7f-a001-000000000008'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000046','Spotify','6aa5b3de-8ec9-4c7f-a001-000000000008'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000047','Amazon Prime','6aa5b3de-8ec9-4c7f-a001-000000000008'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000048','ChatGPT','6aa5b3de-8ec9-4c7f-a001-000000000008'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000049','Gemini','6aa5b3de-8ec9-4c7f-a001-000000000008'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000050','Google One','6aa5b3de-8ec9-4c7f-a001-000000000008'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000051','Outros','6aa5b3de-8ec9-4c7f-a001-000000000008'),

-- Investimentos
    ('7bb5b3de-8ec9-4c7f-a001-000000000052','Tesouro Direto','6aa5b3de-8ec9-4c7f-a001-000000000009'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000053','Ações','6aa5b3de-8ec9-4c7f-a001-000000000009'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000054','ETF','6aa5b3de-8ec9-4c7f-a001-000000000009'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000055','CDI','6aa5b3de-8ec9-4c7f-a001-000000000009'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000056','Fundo Imobiliário','6aa5b3de-8ec9-4c7f-a001-000000000009'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000057','Criptomoedas','6aa5b3de-8ec9-4c7f-a001-000000000009'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000058','Previdência','6aa5b3de-8ec9-4c7f-a001-000000000009'),

-- Financeiro
    ('7bb5b3de-8ec9-4c7f-a001-000000000059','Empréstimo','6aa5b3de-8ec9-4c7f-a001-000000000010'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000060','Financiamento','6aa5b3de-8ec9-4c7f-a001-000000000010'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000061','Imposto de Renda','6aa5b3de-8ec9-4c7f-a001-000000000010'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000062','Taxas Bancárias','6aa5b3de-8ec9-4c7f-a001-000000000010'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000063','Saque','6aa5b3de-8ec9-4c7f-a001-000000000010'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000064','Outros Tributos','6aa5b3de-8ec9-4c7f-a001-000000000010'),

-- Outros
    ('7bb5b3de-8ec9-4c7f-a001-000000000065','Presentes','6aa5b3de-8ec9-4c7f-a001-000000000011'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000066','Doações','6aa5b3de-8ec9-4c7f-a001-000000000011'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000067','Pet','6aa5b3de-8ec9-4c7f-a001-000000000011'),
    ('7bb5b3de-8ec9-4c7f-a001-000000000068','Diversos','6aa5b3de-8ec9-4c7f-a001-000000000011');