package br.com.girotech.motor.infrastructure.service;

import br.com.girotech.motor.dto.GiroSugestaoDTO;
import br.com.girotech.motor.infrastructure.entity.Pedido;
import br.com.girotech.motor.infrastructure.entity.Categoria;
import br.com.girotech.motor.infrastructure.entity.Produto;
import br.com.girotech.motor.infrastructure.exceptions.RecursoNaoEncontradoException;
import br.com.girotech.motor.infrastructure.repository.CategoriaRepositorio;
import br.com.girotech.motor.infrastructure.repository.ClienteRepositorio;
import br.com.girotech.motor.infrastructure.repository.PedidoRepositorio;
import br.com.girotech.motor.infrastructure.repository.ProdutoRepositorio;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(
        readOnly = true
)
public class RecomendacaoService {
    private static final Logger log = LoggerFactory.getLogger(RecomendacaoService.class);
    private static final double PESO_INTERSECAO = 5.0;
    private static final double PESO_TENDENCIA = 3.0;
    private static final double PESO_FREQUENCIA = 2.0;
    private static final int MAX_SUGESTOES = 10;
    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;

    public RecomendacaoService(ClienteRepositorio clienteRepositorio, PedidoRepositorio pedidoRepositorio, ProdutoRepositorio produtoRepositorio, CategoriaRepositorio categoriaRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public List<GiroSugestaoDTO> sugerirProdutos(Long clienteId) {
        this.clienteRepositorio.findById(clienteId).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com id: " + clienteId));
        List<Pedido> historico = this.pedidoRepositorio.buscarPedidosComProdutoPorCliente(clienteId);
        if (historico.isEmpty()) {
            log.info("[GIRO TECH] Cliente {} sem histórico — retornando catálogo genérico.", clienteId);
            return this.sugestoesSemHistorico();
        } else {
            Set<String> categoriasDoCliente = historico.stream().map((p) -> p.getProduto().getCategoria().getNome()).collect(Collectors.toCollection(HashSet::new));
            Set<String> tendenciasGiroTech = this.categoriaRepositorio.findByTendenciaTrue().stream().map(Categoria::getNome).collect(Collectors.toCollection(HashSet::new));
            Set<String> intersecao = new HashSet<>(categoriasDoCliente);
            intersecao.retainAll(tendenciasGiroTech);
            log.debug("[GIRO TECH] Cliente {} | Histórico: {} | Tendências: {} | Interseção: {}", clienteId, categoriasDoCliente, tendenciasGiroTech, intersecao);
            Set<Long> produtosJaComprados = historico.stream().map((p) -> p.getProduto().getId()).collect(Collectors.toSet());
            Map<String, Long> frequenciaCategoria = historico.stream().collect(Collectors.groupingBy((p) -> p.getProduto().getCategoria().getNome(), Collectors.counting()));
            List<Categoria> categoriasTendencia = this.categoriaRepositorio.findByTendenciaTrue();
            List<Produto> candidatos = this.produtoRepositorio.findByCategoriaIn(categoriasTendencia).stream().filter((p) -> !produtosJaComprados.contains(p.getId())).toList();
            List<GiroSugestaoDTO> sugestoes = candidatos.stream().map((produto) -> this.construirSugestao(produto, intersecao, tendenciasGiroTech, frequenciaCategoria)).collect(Collectors.toCollection(ArrayList::new));
            List<GiroSugestaoDTO> ordenadas = this.ordenarPorRelevancia(sugestoes);
            return ordenadas.stream().limit(MAX_SUGESTOES).toList();
        }
    }

    public Set<String> calcularIntersecaoComTendencias(Long clienteId, Set<String> tendencias) {
        List<Pedido> historico = this.pedidoRepositorio.buscarPedidosComProdutoPorCliente(clienteId);
        Set<String> categoriasDoCliente = historico.stream().map((p) -> p.getProduto().getCategoria().getNome()).collect(Collectors.toCollection(HashSet::new));
        Set<String> intersecao = new HashSet<>(categoriasDoCliente);
        intersecao.retainAll(tendencias);
        log.debug("[GIRO TECH] Interseção calculada para cliente {}: {}", clienteId, intersecao);
        return Collections.unmodifiableSet(intersecao);
    }

    public List<GiroSugestaoDTO> ordenarPorRelevancia(List<GiroSugestaoDTO> lista) {
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        } else {
            int meio = lista.size() / 2;
            List<GiroSugestaoDTO> esquerda = this.ordenarPorRelevancia(new ArrayList<>(lista.subList(0, meio)));
            List<GiroSugestaoDTO> direita = this.ordenarPorRelevancia(new ArrayList<>(lista.subList(meio, lista.size())));
            return this.mesclar(esquerda, direita);
        }
    }

    private List<GiroSugestaoDTO> mesclar(List<GiroSugestaoDTO> esquerda, List<GiroSugestaoDTO> direita) {
        List<GiroSugestaoDTO> resultado = new ArrayList<>(esquerda.size() + direita.size());
        int i = 0;
        int j = 0;

        while(i < esquerda.size() && j < direita.size()) {
            if (esquerda.get(i).pontuacao() >= direita.get(j).pontuacao()) {
                resultado.add(esquerda.get(i++));
            } else {
                resultado.add(direita.get(j++));
            }
        }

        resultado.addAll(esquerda.subList(i, esquerda.size()));
        resultado.addAll(direita.subList(j, direita.size()));
        return resultado;
    }

    private GiroSugestaoDTO construirSugestao(Produto produto, Set<String> intersecao, Set<String> tendencias, Map<String, Long> frequenciaCategoria) {
        String nomeCategoria = produto.getCategoria().getNome();
        boolean naIntersecao = intersecao.contains(nomeCategoria);
        boolean naTendencia = tendencias.contains(nomeCategoria);
        long freq = frequenciaCategoria.getOrDefault(nomeCategoria, 0L);
        double pontuacao = (naIntersecao ? PESO_INTERSECAO : 0.0) + (naTendencia ? PESO_TENDENCIA : 0.0) + freq * PESO_FREQUENCIA;
        String motivo = this.construirMotivo(naIntersecao, naTendencia, freq);
        return new GiroSugestaoDTO(produto.getId(), produto.getNome(), nomeCategoria, produto.getPreco(), produto.obterTipoProduto(), pontuacao, motivo);
    }

    private String construirMotivo(boolean naIntersecao, boolean naTendencia, long freq) {
        if (naIntersecao && freq > 2L) {
            return "Categoria favorita em alta tendência Giro Tech";
        } else if (naIntersecao) {
            return "Alinhado ao seu perfil de compras e tendências";
        } else if (naTendencia && freq > 0L) {
            return "Tendência Giro Tech compatível com seu histórico";
        } else {
            return naTendencia ? "Em alta na plataforma Giro Tech" : "Descoberta — produto popular na sua região";
        }
    }

    private List<GiroSugestaoDTO> sugestoesSemHistorico() {
        return this.produtoRepositorio.findAll().stream().limit(MAX_SUGESTOES).map((p) -> new GiroSugestaoDTO(p.getId(), p.getNome(), p.getCategoria().getNome(), p.getPreco(), p.obterTipoProduto(), 1.0, "Produto em destaque na Giro Tech")).toList();
    }
}

