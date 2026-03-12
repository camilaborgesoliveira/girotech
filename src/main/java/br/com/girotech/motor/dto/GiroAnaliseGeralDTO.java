package br.com.girotech.motor.dto;

import java.util.List;
import java.util.Map;

public record GiroAnaliseGeralDTO(
        // Totais
        long totalClientes,
        long totalProdutos,
        long totalPedidos,

        // Estatísticas globais
        double ticketMedioGeral,
        double desvioPadraoGeral,
        String produtoMaisVendido,
        String categoriaMaisVendida,

        // Estruturas de dados
        Map<String, Long> vendasPorCategoria,      // HashMap categoria → vendas
        List<String> categoriasOrdenadas,           // TreeSet → lista ordenada
        List<String> ultimasComprasGlobais,         // LinkedList → histórico recente

        // Teoria dos conjuntos
        Map<String, Long> clientesPorSegmento,     // Econômico | Intermediário | Premium

        // Probabilidade
        List<ProbabilidadeDTO> topProbabilidades    // P(Y|X) — top pares de categorias
) {
    public record ProbabilidadeDTO(
            String categoriaX,
            String categoriaY,
            double probabilidade,
            String descricao
    ) {}
}

