package br.com.girotech.motor.loader;

import br.com.girotech.motor.infrastructure.entity.Categoria;
import br.com.girotech.motor.infrastructure.entity.Cliente;
import br.com.girotech.motor.infrastructure.entity.Pedido;
import br.com.girotech.motor.infrastructure.entity.Produto;
import br.com.girotech.motor.infrastructure.entity.ProdutoDigital;
import br.com.girotech.motor.infrastructure.entity.ProdutoFisico;
import br.com.girotech.motor.infrastructure.repository.CategoriaRepositorio;
import br.com.girotech.motor.infrastructure.repository.ClienteRepositorio;
import br.com.girotech.motor.infrastructure.repository.PedidoRepositorio;
import br.com.girotech.motor.infrastructure.repository.ProdutoRepositorio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GiroDataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GiroDataLoader.class);
    private static final int TOTAL_CLIENTES = 50;
    private static final int TOTAL_PEDIDOS = 200;
    private static final int TOTAL_PRODUTOS = 120;
    private static final String[] ESTADOS_BR = new String[]{"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
    private final CategoriaRepositorio categoriaRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private static final String[] ESPORTES = new String[]{"Futebol", "Vôlei", "Basquete", "Natação", "Ciclismo", "Corrida", "Tênis", "Judô", "Crossfit", "Pilates"};

    public GiroDataLoader(CategoriaRepositorio categoriaRepositorio, ProdutoRepositorio produtoRepositorio, ClienteRepositorio clienteRepositorio, PedidoRepositorio pedidoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    @Transactional
    public void run(String... args) {
        if (this.clienteRepositorio.count() > 0) {
            log.info("[GIRO TECH SYSTEM] ► Banco já populado ({} clientes encontrados). Carga ignorada.", this.clienteRepositorio.count());
            return;
        }
        this.exibirBannerInicial();
        Faker faker = new Faker(new Locale("pt", "BR"));
        Random random = new Random(42L);
        log.info("[GIRO TECH SYSTEM] ► Iniciando carga de categorias...");
        List<Categoria> categorias = this.criarCategorias();
        log.info("[GIRO TECH SYSTEM] ► Iniciando carga de produtos ({} itens)...", 120);
        List<Produto> produtos = this.criarProdutos(categorias, faker, random);
        log.info("[GIRO TECH SYSTEM] ► Iniciando carga de clientes ({} clientes)...", 50);
        List<Cliente> clientes = this.criarClientes(faker, random);
        log.info("[GIRO TECH SYSTEM] ► Iniciando carga de pedidos ({} pedidos)...", 200);
        this.criarPedidos(clientes, produtos, faker, random);
        this.exibirResumoFinal();
    }

    private List<Categoria> criarCategorias() {
        List<Object[]> definicoes = List.of(new Object[]{"Eletrônicos", "Smartphones, notebooks, tablets e periféricos", true}, new Object[]{"Moda", "Roupas, calçados e acessórios fashion brasileiros", true}, new Object[]{"Esportes", "Equipamentos e vestuário para atividade física", true}, new Object[]{"Games", "Jogos digitais, consoles e periféricos gamers", true}, new Object[]{"Livros Digitais", "E-books de ficção, negócios, tecnologia e autoajuda", true}, new Object[]{"Alimentos", "Produtos alimentícios e bebidas artesanais brasileiras", false}, new Object[]{"Casa e Jardim", "Decoração, utensílios domésticos e jardinagem", false}, new Object[]{"Beleza", "Cosméticos, perfumes e produtos de cuidado pessoal", false}, new Object[]{"Brinquedos", "Brinquedos educativos e de entretenimento", false}, new Object[]{"Automotivo", "Acessórios e peças para veículos", false});
        List<Categoria> salvas = new ArrayList();

        for(Object[] def : definicoes) {
            Categoria c = Categoria.builder().nome((String)def[0]).descricao((String)def[1]).tendencia((Boolean)def[2]).build();
            salvas.add((Categoria)this.categoriaRepositorio.save(c));
        }

        log.info("[GIRO TECH SYSTEM] ✔ {} categorias carregadas ({} em tendência).", salvas.size(), salvas.stream().filter(Categoria::getTendencia).count());
        return salvas;
    }

    private List<Produto> criarProdutos(List<Categoria> categorias, Faker faker, Random random) {
        List<Produto> todos = new ArrayList();
        int metade = 60;

        for(int i = 0; i < metade; ++i) {
            Categoria cat = (Categoria)categorias.get(random.nextInt(categorias.size()));
            BigDecimal preco = this.gerarPreco(random, 19.9, 499.9);
            ProdutoDigital digital = ProdutoDigital.builder().nome(this.gerarNomeProdutoDigital(faker, cat)).descricao(faker.lorem().sentence(8)).preco(preco).categoria(cat).urlDownload("https://cdn.girotech.com.br/produtos/" + faker.internet().slug()).tamanhoMb(this.round(random.nextDouble() * (double)4000.0F + (double)50.0F, 1)).build();
            todos.add((Produto)this.produtoRepositorio.save(digital));
        }

        for(int i = 0; i < metade; ++i) {
            Categoria cat = (Categoria)categorias.get(random.nextInt(categorias.size()));
            BigDecimal preco = this.gerarPreco(random, 29.9, 2999.9);
            ProdutoFisico fisico = ProdutoFisico.builder().nome(this.gerarNomeProdutoFisico(faker, random, cat)).descricao(faker.lorem().sentence(10)).preco(preco).categoria(cat).peso(this.round(random.nextDouble() * (double)10.0F + 0.1, 2)).dimensoes(String.format("%dx%dx%d cm", 10 + random.nextInt(40), 10 + random.nextInt(30), 5 + random.nextInt(20))).requerEntrega(random.nextDouble() > 0.15).build();
            todos.add((Produto)this.produtoRepositorio.save(fisico));
        }

        log.info("[GIRO TECH SYSTEM] ✔ {} produtos carregados ({} digitais, {} físicos).", new Object[]{todos.size(), metade, metade});
        return todos;
    }

    private List<Cliente> criarClientes(Faker faker, Random random) {
        List<Cliente> clientes = new ArrayList();

        for(int i = 0; i < 50; ++i) {
            String cpf;
            for(cpf = this.gerarCpfFormatado(random); this.clienteRepositorio.existsByCpf(cpf); cpf = this.gerarCpfFormatado(random)) {
            }

            Cliente cliente = Cliente.builder().nome(faker.name().fullName()).email(faker.internet().emailAddress()).cpf(cpf).cidade(faker.address().city()).estado(ESTADOS_BR[random.nextInt(ESTADOS_BR.length)]).dataCadastro(LocalDate.now().minusDays((long)random.nextInt(1095))).build();
            clientes.add((Cliente)this.clienteRepositorio.save(cliente));
        }

        log.info("[GIRO TECH SYSTEM] ✔ {} clientes carregados.", clientes.size());
        return clientes;
    }

    private void criarPedidos(List<Cliente> clientes, List<Produto> produtos, Faker faker, Random random) {
        List<Pedido> pedidos = new ArrayList();

        for(int i = 0; i < 200; ++i) {
            Cliente cliente = (Cliente)clientes.get(random.nextInt(clientes.size()));
            Produto produto = (Produto)produtos.get(random.nextInt(produtos.size()));
            double fatorVariacao = 0.8 + random.nextDouble() * 0.4;
            BigDecimal valorTotal = produto.getPreco().multiply(BigDecimal.valueOf(fatorVariacao)).setScale(2, RoundingMode.HALF_UP);
            Pedido pedido = Pedido.builder().cliente(cliente).produto(produto).dataCompra(LocalDate.now().minusDays((long)random.nextInt(730))).valorTotal(valorTotal).build();
            pedidos.add((Pedido)this.pedidoRepositorio.save(pedido));
        }

        log.info("[GIRO TECH SYSTEM] ✔ {} pedidos carregados.", pedidos.size());
    }

    private void exibirBannerInicial() {
        log.info("");
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║       [GIRO TECH SYSTEM] Inicializando motor        ║");
        log.info("║           de análise e recomendação...               ║");
        log.info("╚══════════════════════════════════════════════════════╝");
        log.info("");
        log.info("[GIRO TECH SYSTEM] Inicializando motor de análise...");
        log.info("[GIRO TECH SYSTEM] Ambiente: H2 em memória | Locale: pt-BR");
        log.info("[GIRO TECH SYSTEM] Carregando dados fictícios brasileiros com DataFaker...");
        log.info("");
    }

    private void exibirResumoFinal() {
        log.info("");
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║        [GIRO TECH SYSTEM] Carga concluída!          ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  Clientes : {:>5}                                    ║", this.clienteRepositorio.count());
        log.info("║  Produtos : {:>5}                                    ║", this.produtoRepositorio.count());
        log.info("║  Pedidos  : {:>5}                                    ║", this.pedidoRepositorio.count());
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  API REST  → http://localhost:8080/api/painel/status ║");
        log.info("║  H2 Console→ http://localhost:8080/h2-console        ║");
        log.info("╚══════════════════════════════════════════════════════╝");
        log.info("");
    }

    private BigDecimal gerarPreco(Random random, double min, double max) {
        double valor = min + random.nextDouble() * (max - min);
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private double round(double valor, int casas) {
        return BigDecimal.valueOf(valor).setScale(casas, RoundingMode.HALF_UP).doubleValue();
    }

    private String gerarCpfFormatado(Random random) {
        return String.format("%03d.%03d.%03d-%02d", random.nextInt(1000), random.nextInt(1000), random.nextInt(1000), random.nextInt(100));
    }

    private String gerarNomeProdutoDigital(Faker faker, Categoria categoria) {
        String var10000;
        switch (categoria.getNome()) {
            case "Games" -> var10000 = faker.esports().game() + " — Edição Digital";
            case "Livros Digitais" -> var10000 = "E-Book: " + faker.book().title();
            case "Eletrônicos" -> var10000 = "Software " + faker.app().name() + " Pro";
            default -> var10000 = faker.commerce().productName() + " Digital";
        }

        return var10000;
    }

    private String gerarNomeProdutoFisico(Faker faker, Random random, Categoria categoria) {
        String var10000;
        switch (categoria.getNome()) {
            case "Eletrônicos":
                var10000 = faker.commerce().productName() + " " + faker.commerce().material();
                break;
            case "Moda":
                var10000 = faker.color().name() + " " + faker.commerce().productName();
                break;
            case "Esportes":
                var10000 = "Kit " + ESPORTES[random.nextInt(ESPORTES.length)] + " Professional";
                break;
            case "Alimentos":
                var10000 = faker.food().ingredient() + " Artesanal";
                break;
            default:
                var10000 = faker.commerce().productName();
        }

        return var10000;
    }
}
