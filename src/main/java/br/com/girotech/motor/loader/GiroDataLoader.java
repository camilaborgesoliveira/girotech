package br.com.girotech.motor.loader;

import br.com.girotech.motor.infrastructure.entity.*;
import br.com.girotech.motor.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiroDataLoader implements CommandLineRunner {

    private static final int TOTAL_CLIENTES = 50;
    private static final int TOTAL_PRODUTOS = 120;
    private static final int TOTAL_PEDIDOS = 200;

    private static final String[] ESTADOS_BR = {
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    };
    private static final String[] ESPORTES = {
            "Futebol", "Volei", "Basquete", "Natacao", "Ciclismo",
            "Corrida", "Tenis", "Judo", "Crossfit", "Pilates"
    };

    private final CategoriaRepositorio categoriaRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    @Override
    @Transactional
    public void run(String... args) {
        if (clienteRepositorio.count() > 0) {
            log.info("[GIRO TECH] Banco ja populado ({} clientes). Carga ignorada.", clienteRepositorio.count());
            return;
        }

        log.info("[GIRO TECH] Iniciando carga de dados sinteticos (DataFaker pt-BR / PostgreSQL)...");

        var faker = new Faker(new Locale("pt", "BR"));
        var rng = new Random(42L);

        var categorias = criarCategorias();
        var produtos = criarProdutos(categorias, faker, rng);
        var clientes = criarClientes(faker, rng);
        criarPedidos(clientes, produtos, rng);

        log.info("[GIRO TECH] Carga concluida: {} clientes | {} produtos | {} pedidos",
                clienteRepositorio.count(), produtoRepositorio.count(), pedidoRepositorio.count());
        log.info("[GIRO TECH] API disponivel em http://localhost:8080/api/painel/status");
    }

    private List<Categoria> criarCategorias() {
        List<Object[]> defs = List.of(
                new Object[]{"Eletrônicos", "Smartphones, notebooks, tablets e periféricos", true},
                new Object[]{"Moda", "Roupas, calçados e acessórios fashion brasileiros", true},
                new Object[]{"Esportes", "Equipamentos e vestuário para atividade física", true},
                new Object[]{"Games", "Jogos digitais, consoles e periféricos gamers", true},
                new Object[]{"Livros Digitais", "E-books de ficção, negócios, tecnologia e autoajuda", true},
                new Object[]{"Alimentos", "Produtos alimentícios e bebidas artesanais brasileiras", false},
                new Object[]{"Casa e Jardim", "Decoração, utensílios domésticos e jardinagem", false},
                new Object[]{"Beleza", "Cosméticos, perfumes e produtos de cuidado pessoal", false},
                new Object[]{"Brinquedos", "Brinquedos educativos e de entretenimento", false},
                new Object[]{"Automotivo", "Acessórios e peças para veículos", false}
        );

        List<Categoria> salvas = new ArrayList<>();
        for (Object[] d : defs) {
            var cat = Categoria.builder()
                    .nome((String) d[0])
                    .descricao((String) d[1])
                    .tendencia((Boolean) d[2])
                    .build();
            salvas.add(categoriaRepositorio.save(cat));
        }
        log.info("[GIRO TECH] {} categorias carregadas ({} em tendencia).",
                salvas.size(), salvas.stream().filter(Categoria::getTendencia).count());
        return salvas;
    }

    private List<Produto> criarProdutos(List<Categoria> categorias, Faker faker, Random rng) {
        List<Produto> todos = new ArrayList<>();
        int metade = TOTAL_PRODUTOS / 2;

        for (int i = 0; i < metade; i++) {
            var cat = categorias.get(rng.nextInt(categorias.size()));
            var digital = ProdutoDigital.builder()
                    .nome(nomeDigital(faker, cat))
                    .descricao(faker.lorem().sentence(8))
                    .preco(precoAleatorio(rng, 19.9, 499.9))
                    .categoria(cat)
                    .urlDownload("https://cdn.girotech.com.br/produtos/" + faker.internet().slug())
                    .tamanhoMb(arredondar(rng.nextDouble() * 4000 + 50, 1))
                    .build();
            todos.add(produtoRepositorio.save(digital));
        }

        for (int i = 0; i < metade; i++) {
            var cat = categorias.get(rng.nextInt(categorias.size()));
            var fisico = ProdutoFisico.builder()
                    .nome(nomeFisico(faker, rng, cat))
                    .descricao(faker.lorem().sentence(10))
                    .preco(precoAleatorio(rng, 29.9, 2999.9))
                    .categoria(cat)
                    .peso(arredondar(rng.nextDouble() * 10 + 0.1, 2))
                    .dimensoes(String.format("%dx%dx%d cm",
                            10 + rng.nextInt(40), 10 + rng.nextInt(30), 5 + rng.nextInt(20)))
                    .requerEntrega(rng.nextDouble() > 0.15)
                    .build();
            todos.add(produtoRepositorio.save(fisico));
        }

        log.info("[GIRO TECH] {} produtos carregados ({} digitais, {} fisicos).",
                todos.size(), metade, metade);
        return todos;
    }

    private List<Cliente> criarClientes(Faker faker, Random rng) {
        List<Cliente> clientes = new ArrayList<>();
        for (int i = 0; i < TOTAL_CLIENTES; i++) {
            String cpf = gerarCpf(rng);
            while (clienteRepositorio.existsByCpf(cpf)) cpf = gerarCpf(rng);

            var cliente = Cliente.builder()
                    .nome(faker.name().fullName())
                    .email(faker.internet().emailAddress())
                    .cpf(cpf)
                    .cidade(faker.address().city())
                    .estado(ESTADOS_BR[rng.nextInt(ESTADOS_BR.length)])
                    .dataCadastro(LocalDate.now().minusDays(rng.nextInt(1095)))
                    .build();
            clientes.add(clienteRepositorio.save(cliente));
        }
        log.info("[GIRO TECH] {} clientes carregados.", clientes.size());
        return clientes;
    }

    private void criarPedidos(List<Cliente> clientes, List<Produto> produtos, Random rng) {
        for (int i = 0; i < TOTAL_PEDIDOS; i++) {
            var cliente = clientes.get(rng.nextInt(clientes.size()));
            var produto = produtos.get(rng.nextInt(produtos.size()));
            double fator = 0.8 + rng.nextDouble() * 0.4; // variacao +/- 20%
            BigDecimal valor = produto.getPreco()
                    .multiply(BigDecimal.valueOf(fator))
                    .setScale(2, RoundingMode.HALF_UP);

            pedidoRepositorio.save(Pedido.builder()
                    .cliente(cliente)
                    .produto(produto)
                    .dataCompra(LocalDate.now().minusDays(rng.nextInt(730)))
                    .valorTotal(valor)
                    .build());
        }
        log.info("[GIRO TECH] {} pedidos carregados.", TOTAL_PEDIDOS);
    }

    private BigDecimal precoAleatorio(Random rng, double min, double max) {
        return BigDecimal.valueOf(min + rng.nextDouble() * (max - min))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private double arredondar(double valor, int casas) {
        return BigDecimal.valueOf(valor).setScale(casas, RoundingMode.HALF_UP).doubleValue();
    }

    private String gerarCpf(Random rng) {
        return String.format("%03d.%03d.%03d-%02d",
                rng.nextInt(1000), rng.nextInt(1000), rng.nextInt(1000), rng.nextInt(100));
    }

    private String nomeDigital(Faker faker, Categoria cat) {
        return switch (cat.getNome()) {
            case "Games" -> faker.esports().game() + " -- Edicao Digital";
            case "Livros Digitais" -> "E-Book: " + faker.book().title();
            case "Eletrônicos" -> "Software " + faker.app().name() + " Pro";
            default -> faker.commerce().productName() + " Digital";
        };
    }

    private String nomeFisico(Faker faker, Random rng, Categoria cat) {
        return switch (cat.getNome()) {
            case "Eletrônicos" -> faker.commerce().productName() + " " + faker.commerce().material();
            case "Moda" -> faker.color().name() + " " + faker.commerce().productName();
            case "Esportes" -> "Kit " + ESPORTES[rng.nextInt(ESPORTES.length)] + " Professional";
            case "Alimentos" -> faker.food().ingredient() + " Artesanal";
            default -> faker.commerce().productName();
        };
    }
}
