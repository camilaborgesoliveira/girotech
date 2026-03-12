-- GiroTech - Script de criação de tabelas
-- Banco: girotech | PostgreSQL 18

CREATE TABLE IF NOT EXISTS categorias (
    id        BIGSERIAL    PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    tendencia BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS clientes (
    id            BIGSERIAL    PRIMARY KEY,
    nome          VARCHAR(200) NOT NULL,
    email         VARCHAR(200) NOT NULL UNIQUE,
    cpf           VARCHAR(14)  NOT NULL UNIQUE,
    cidade        VARCHAR(100),
    estado        VARCHAR(2),
    data_cadastro DATE         NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_cliente_email ON clientes (email);
CREATE INDEX IF NOT EXISTS idx_cliente_cpf   ON clientes (cpf);

CREATE TABLE IF NOT EXISTS produtos (
    id             BIGSERIAL        PRIMARY KEY,
    tipo_produto   VARCHAR(10)      NOT NULL,
    nome           VARCHAR(200)     NOT NULL,
    descricao      VARCHAR(1000),
    preco          NUMERIC(10,2)    NOT NULL,
    categoria_id   BIGINT           NOT NULL REFERENCES categorias(id),
    url_download   VARCHAR(500),
    tamanho_mb     DOUBLE PRECISION,
    peso_kg        DOUBLE PRECISION,
    dimensoes      VARCHAR(50),
    requer_entrega BOOLEAN
);

CREATE TABLE IF NOT EXISTS pedidos (
    id          BIGSERIAL     PRIMARY KEY,
    cliente_id  BIGINT        NOT NULL REFERENCES clientes(id),
    produto_id  BIGINT        NOT NULL REFERENCES produtos(id),
    data_compra DATE          NOT NULL,
    valor_total NUMERIC(10,2) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pedido_cliente ON pedidos (cliente_id);
CREATE INDEX IF NOT EXISTS idx_pedido_data    ON pedidos (data_compra);

-- Verificação final
SELECT tablename AS tabela
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;


