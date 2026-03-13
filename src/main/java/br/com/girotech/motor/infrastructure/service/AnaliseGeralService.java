package br.com.girotech.motor.infrastructure.service;

import br.com.girotech.motor.dto.GiroAnaliseGeralDTO;
import br.com.girotech.motor.dto.GiroAnaliseGeralDTO.ProbabilidadeDTO;
import br.com.girotech.motor.infrastructure.entity.Pedido;
import br.com.girotech.motor.infrastructure.repository.ClienteRepositorio;
import br.com.girotech.motor.infrastructure.repository.PedidoRepositorio;
import br.com.girotech.motor.infrastructure.repository.ProdutoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnaliseGeralService {

    private static final double LIMIAR_PREMIUM = 800.0;
    private static final double LIMIAR_INTERMEDIARIO = 300.0;

    private final PedidoRepositorio pedidoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final AnaliseEstatisticaService analiseEstatisticaService;

    public GiroAnaliseGeralDTO analisarGeral() {
        List<Pedido> todosPedidos = pedidoRepositorio.findAll();
        log.info("[GIRO TECH] Analisando {} pedidos...", todosPedidos.size());

        long totalClientes = clienteRepositorio.count();
        long totalProdutos = produtoRepositorio.count();
        long totalPedidos = todosPedidos.size();

        double ticketMedioGeral = calcularMedia(todosPedidos);
        double desvioPadraoGeral = calcularDesvioPadrao(todosPedidos, ticketMedioGeral);

        // HashMap p/ acesso O(1) por categoria/produto
        Map<String, Long> vendasPorCategoria = new HashMap<>();
        Map<String, Long> vendasPorProduto = new HashMap<>();
        for (Pedido p : todosPedidos) {
            vendasPorCategoria.merge(p.getProduto().getCategoria().getNome(), 1L, Long::sum);
            vendasPorProduto.merge(p.getProduto().getNome(), 1L, Long::sum);
        }

        String produtoMaisVendido = maxKey(vendasPorProduto);
        String categoriaMaisVendida = maxKey(vendasPorCategoria);

        // TreeSet mantém categorias ordenadas alfabeticamente
        List<String> categoriasOrdenadas = new ArrayList<>(new TreeSet<>(vendasPorCategoria.keySet()));

        // LinkedList p/ historico recente (insercao eficiente nas pontas)
        LinkedList<String> historicoRecente = new LinkedList<>();
        todosPedidos.stream()
                .sorted(Comparator.comparing(Pedido::getDataCompra).reversed())
                .limit(10)
                .forEach(p -> historicoRecente.addLast(
                        p.getCliente().getNome() + " -> " + p.getProduto().getNome()
                                + " (R$ " + p.getValorTotal() + ")"));

        // Segmentacao por conjuntos: A=Premium, B=Intermediario, C=Economico
        Map<String, Long> clientesPorSegmento = new HashMap<>();
        clienteRepositorio.findAll().forEach(cliente -> {
            var pedidosCliente = pedidoRepositorio.buscarPedidosComProdutoPorCliente(cliente.getId());
            if (!pedidosCliente.isEmpty()) {
                double media = calcularMedia(pedidosCliente);
                String seg = media >= LIMIAR_PREMIUM ? "Premium"
                        : media >= LIMIAR_INTERMEDIARIO ? "Intermediário"
                        : "Econômico";
                clientesPorSegmento.merge(seg, 1L, Long::sum);
            }
        });

        List<ProbabilidadeDTO> topProbabilidades = calcularProbabilidades(todosPedidos);

        return new GiroAnaliseGeralDTO(
                totalClientes, totalProdutos, totalPedidos,
                ticketMedioGeral, desvioPadraoGeral,
                produtoMaisVendido, categoriaMaisVendida,
                vendasPorCategoria, categoriasOrdenadas,
                new ArrayList<>(historicoRecente),
                clientesPorSegmento, topProbabilidades
        );
    }

    // P(Y|X) = |clientes que compraram X e Y| / |clientes que compraram X|
    private List<ProbabilidadeDTO> calcularProbabilidades(List<Pedido> pedidos) {
        Map<Long, Set<String>> comprasPorCliente = new HashMap<>();
        for (Pedido p : pedidos) {
            comprasPorCliente
                    .computeIfAbsent(p.getCliente().getId(), k -> new HashSet<>())
                    .add(p.getProduto().getCategoria().getNome());
        }

        Map<String, Long> freqCategoria = new HashMap<>();
        comprasPorCliente.values().forEach(cats ->
                cats.forEach(c -> freqCategoria.merge(c, 1L, Long::sum)));

        Map<String, Long> coOcorrencias = new HashMap<>();
        comprasPorCliente.values().forEach(cats -> {
            var lista = new ArrayList<>(cats);
            for (int i = 0; i < lista.size(); i++) {
                for (int j = i + 1; j < lista.size(); j++) {
                    coOcorrencias.merge(lista.get(i) + " -> " + lista.get(j), 1L, Long::sum);
                    coOcorrencias.merge(lista.get(j) + " -> " + lista.get(i), 1L, Long::sum);
                }
            }
        });

        return coOcorrencias.entrySet().stream()
                .map(e -> {
                    String[] partes = e.getKey().split(" -> ");
                    String catX = partes[0];
                    double prob = (double) e.getValue() / freqCategoria.getOrDefault(catX, 1L);
                    return new ProbabilidadeDTO(catX, partes[1], prob,
                            String.format("%.0f%% dos clientes que compraram '%s' tambem compraram '%s'",
                                    prob * 100, catX, partes[1]));
                })
                .sorted(Comparator.comparingDouble(ProbabilidadeDTO::probabilidade).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private double calcularMedia(List<Pedido> pedidos) {
        return pedidos.stream()
                .mapToDouble(p -> p.getValorTotal().doubleValue())
                .average().orElse(0.0);
    }

    private double calcularDesvioPadrao(List<Pedido> pedidos, double media) {
        double variancia = pedidos.stream().mapToDouble(p -> {
            double d = p.getValorTotal().doubleValue() - media;
            return d * d;
        }).average().orElse(0.0);
        return Math.sqrt(variancia);
    }

    private String maxKey(Map<String, Long> mapa) {
        return mapa.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }
}
