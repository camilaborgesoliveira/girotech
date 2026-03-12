# GiroTech — Motor de Recomendação e Análise de Perfil de Cliente

Projeto integrado que simula um sistema de recomendação de produtos e análise estatística do comportamento de compra de clientes. A aplicação utiliza dados fictícios gerados com a biblioteca DataFaker (pt-BR) e expõe uma API REST para consultar estatísticas, sugestões de produtos e análises baseadas em teoria dos conjuntos.

**Stack:** Java 17 · Spring Boot 4.0.2 · PostgreSQL 18 · Maven

---

## O que o sistema faz

Resumidamente, o GiroTech recebe um banco com clientes, produtos (digitais e físicos) e pedidos, e a partir disso:

- Calcula ticket médio, moda de categoria, desvio padrão e segmenta cada cliente (Premium / Intermediário / Econômico)
- Recomenda produtos novos pro cliente com base nas categorias que ele já comprou e nas tendências do mercado
- Faz uma análise geral do sistema todo, incluindo probabilidade condicional do tipo "quem comprou X também comprou Y"
- Usa na prática HashMap, TreeSet, LinkedList, interseção/união de conjuntos e ordenação por relevância

---

## Pré-requisitos

Pra rodar o projeto você precisa ter:

- **Java 17** ou superior (`java -version`)
- **PostgreSQL 18** rodando local na porta 5432
- **Maven** (pode usar o `./mvnw` que já vem no projeto)

Se no macOS o `psql` não for reconhecido no terminal, adicione ao PATH:

```bash
echo 'export PATH="/Library/PostgreSQL/18/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

---

## Configuração do banco

### Criar o database

```bash
psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE girotech;"
```

### Criar as tabelas

```bash
psql -U postgres -h localhost -p 5432 -d girotech -f create_tables.sql
```

O arquivo `create_tables.sql` já está na raiz do projeto com o DDL completo (categorias, clientes, produtos e pedidos).

### application.properties

As configurações de conexão ficam em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/girotech
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```

> **⚠️ Importante:** Nunca suba senhas reais no repositório. Crie um arquivo `.env` na raiz do projeto com a variável `DB_PASSWORD=sua_senha_aqui` ou passe via linha de comando:
>
> ```bash
> DB_PASSWORD=sua_senha ./mvnw spring-boot:run
> ```

---

## Como rodar

A forma mais simples é usar o script que já libera a porta automaticamente:

```bash
./start.sh
```

Ou direto pelo Maven:

```bash
./mvnw spring-boot:run
```

Pelo IntelliJ, basta dar Run na classe `br.com.girotech.motor.GiroTechApplication`.

Quando a aplicação subir, você vai ver no console algo como:

```
[GIRO TECH SYSTEM] ✔ 10 categorias carregadas (5 em tendência).
[GIRO TECH SYSTEM] ✔ 120 produtos carregados (60 digitais, 60 físicos).
[GIRO TECH SYSTEM] ✔ 50 clientes carregados.
[GIRO TECH SYSTEM] ✔ 200 pedidos carregados.
```

Pra confirmar que tá tudo certo, acesse no navegador:

```
http://localhost:8080/api/painel/status
```

---

## Estrutura do projeto

```
src/main/java/br/com/girotech/motor/
├── GiroTechApplication.java
├── controller/
│   └── PainelAnaliseController.java
├── dto/
│   ├── GiroAnaliseGeralDTO.java
│   ├── GiroEstatisticaDTO.java
│   └── GiroSugestaoDTO.java
├── infrastructure/
│   ├── config/SecurityConfig.java
│   ├── entity/
│   │   ├── Categoria.java
│   │   ├── Cliente.java
│   │   ├── Pedido.java
│   │   ├── Produto.java            (abstrata)
│   │   ├── ProdutoDigital.java
│   │   └── ProdutoFisico.java
│   ├── exceptions/
│   │   ├── GiroTechExceptionHandler.java
│   │   └── RecursoNaoEncontradoException.java
│   ├── repository/
│   │   ├── CategoriaRepositorio.java
│   │   ├── ClienteRepositorio.java
│   │   ├── PedidoRepositorio.java
│   │   └── ProdutoRepositorio.java
│   └── service/
│       ├── AnaliseEstatisticaService.java
│       ├── AnaliseGeralService.java
│       └── RecomendacaoService.java
└── loader/
    └── GiroDataLoader.java
```

---

## Conceitos aplicados

### POO

A classe `Produto` é abstrata e usa herança SINGLE_TABLE com discriminator. `ProdutoDigital` tem campos como `urlDownload` e `tamanhoMb`, enquanto `ProdutoFisico` tem `pesoKg`, `dimensoes` e `requerEntrega`. O polimorfismo aparece no método `obterTipoProduto()` que cada subclasse implementa de forma diferente.

### Estruturas de dados

- **HashMap** — usado em `AnaliseGeralService` pra mapear categoria → quantidade de vendas, permitindo busca em O(1)
- **TreeSet** — mantém as categorias sempre ordenadas alfabeticamente de forma automática
- **LinkedList** — armazena o histórico das últimas compras, aproveitando a inserção eficiente nas extremidades
- **HashSet** — usado no serviço de recomendação pra verificar rapidamente se um produto já foi comprado

### Estatística

- **Ticket médio (μ):** soma dos valores dos pedidos dividido pela quantidade
- **Moda:** categoria que mais aparece nos pedidos do cliente
- **Desvio padrão (σ):** raiz quadrada da variância, mostra o quanto os gastos variam
- **Segmentação:** classifica o cliente como Premium (≥ R$800), Intermediário (≥ R$300) ou Econômico (abaixo de R$300) com base no ticket médio

### Teoria dos conjuntos

O sistema calcula a interseção entre o conjunto de categorias que o cliente comprou e o conjunto de categorias em tendência. Essa interseção é usada como fator de peso na pontuação de recomendação.

### Probabilidade condicional

No endpoint de análise geral, o sistema calcula P(Y|X) — a probabilidade de um cliente comprar na categoria Y dado que já comprou na categoria X. A fórmula usada é: número de clientes que compraram em X e Y dividido pelo número de clientes que compraram em X.

### Algoritmo de ordenação

As sugestões de produtos são ordenadas por pontuação de relevância usando a ordenação nativa do Java (TimSort, que é baseado em MergeSort). A pontuação combina peso de interseção (+5.0), tendência (+3.0) e frequência de compra do cliente naquela categoria (+2.0 × freq).

---

## Endpoints

Todos os endpoints são GET e ficam sob `/api/painel`.

### Status

```
GET /api/painel/status
```

Retorna se o sistema está operacional e os totais de clientes/produtos.

### Análise geral

```
GET /api/painel/analise-geral
```

Esse é o endpoint principal. Retorna tudo de uma vez:
- Ticket médio e desvio padrão global
- Produto e categoria mais vendidos (moda)
- Vendas por categoria (HashMap)
- Categorias ordenadas (TreeSet)
- Últimas 10 compras (LinkedList)
- Clientes por segmento (conjuntos)
- Top 10 probabilidades condicionais P(Y|X)

### Estatísticas por cliente

```
GET /api/painel/clientes/{id}/estatisticas
```

Passa o ID do cliente (de 1 a 50) e recebe ticket médio, moda de categoria, desvio padrão, segmento e total de pedidos dele.

Exemplo de resposta:

```json
{
  "clienteId": 1,
  "nomeCliente": "Davi Luca Marques",
  "ticketMedio": 1236.93,
  "modaCategoria": "Beleza",
  "desvioPadrao": 1243.13,
  "segmento": "Premium",
  "totalPedidos": 4
}
```

### Sugestões de produtos

```
GET /api/painel/clientes/{id}/sugestoes
```

Retorna até 10 produtos que o cliente ainda não comprou, ordenados por relevância.

### Interseção de conjuntos

```
GET /api/painel/clientes/{id}/intersecao?tendencias=Games,Moda
```

Calcula a interseção entre as categorias que o cliente já comprou e as tendências informadas na query string.

### Listagens

```
GET /api/painel/clientes
GET /api/painel/produtos
```

---

## Exemplo da análise geral

Resposta resumida do `GET /api/painel/analise-geral`:

```json
{
  "totalClientes": 50,
  "totalProdutos": 120,
  "totalPedidos": 200,
  "ticketMedioGeral": 800.42,
  "desvioPadraoGeral": 818.97,
  "produtoMaisVendido": "Hearthstone — Edição Digital",
  "categoriaMaisVendida": "Games",
  "vendasPorCategoria": {
    "Games": 29,
    "Automotivo": 28,
    "Eletrônicos": 26,
    "Brinquedos": 23,
    "Beleza": 17,
    "Moda": 17
  },
  "categoriasOrdenadas": [
    "Alimentos", "Automotivo", "Beleza", "Brinquedos",
    "Casa e Jardim", "Eletrônicos", "Esportes",
    "Games", "Livros Digitais", "Moda"
  ],
  "clientesPorSegmento": {
    "Premium": 21,
    "Intermediário": 18,
    "Econômico": 9
  },
  "topProbabilidades": [
    {
      "categoriaX": "Moda",
      "categoriaY": "Games",
      "probabilidade": 0.67,
      "descricao": "67% dos clientes que compraram 'Moda' também compraram 'Games'"
    }
  ]
}
```

---

## Sobre os dados

Na primeira vez que a aplicação roda, o `GiroDataLoader` popula o banco automaticamente usando o DataFaker com locale pt-BR:

- 10 categorias (5 marcadas como tendência)
- 120 produtos (60 digitais e 60 físicos)
- 50 clientes com nomes, CPFs e endereços brasileiros
- 200 pedidos distribuídos aleatoriamente

O loader verifica se já existem dados antes de inserir, então pode reiniciar a aplicação sem medo de duplicar registros.

Pra limpar tudo e recriar do zero:

```bash
psql -U postgres -h localhost -p 5432 -d girotech -c "
TRUNCATE TABLE pedidos    RESTART IDENTITY CASCADE;
TRUNCATE TABLE produtos   RESTART IDENTITY CASCADE;
TRUNCATE TABLE clientes   RESTART IDENTITY CASCADE;
TRUNCATE TABLE categorias RESTART IDENTITY CASCADE;"
```

Depois é só reiniciar a aplicação.

---

## Testando

Dá pra testar de qualquer uma dessas formas:

**No navegador** — só abrir a URL direto, por exemplo `http://localhost:8080/api/painel/analise-geral`

**No terminal com curl:**

```bash
curl http://localhost:8080/api/painel/status | python3 -m json.tool
curl http://localhost:8080/api/painel/analise-geral | python3 -m json.tool
curl http://localhost:8080/api/painel/clientes/1/estatisticas | python3 -m json.tool
curl http://localhost:8080/api/painel/clientes/1/sugestoes | python3 -m json.tool
```

**No Postman** — cria uma collection com os GETs acima e manda ver.

Pra testar vários clientes de uma vez:

```bash
for id in 1 5 10 20 30; do
  echo "--- Cliente $id ---"
  curl -s http://localhost:8080/api/painel/clientes/$id/estatisticas | python3 -m json.tool
done
```

---

## Problemas comuns

**Porta 8080 ocupada:**

```bash
lsof -ti:8080 | xargs kill -9
```

Ou use o `./start.sh` que já faz isso sozinho.

**psql não encontrado:**

```bash
echo 'export PATH="/Library/PostgreSQL/18/bin:$PATH"' >> ~/.zshrc && source ~/.zshrc
```

**Banco sem dados depois de iniciar:**

Rode o script SQL manualmente e reinicie:

```bash
psql -U postgres -h localhost -p 5432 -d girotech -f create_tables.sql
./start.sh
```

**Erro de dependências no Maven:**

```bash
./mvnw dependency:resolve -U
./mvnw clean compile
```
