package br.com.girotech.motor.infrastructure.service;

import br.com.girotech.motor.dto.GiroSugestaoDTO;
import br.com.girotech.motor.infrastructure.entity.Categoria;
import br.com.girotech.motor.infrastructure.entity.Pedido;
import br.com.girotech.motor.infrastructure.entity.Produto;
import br.com.girotech.motor.infrastructure.exceptions.RecursoNaoEncontradoException;
import br.com.girotech.motor.infrastructure.repository.CategoriaRepositorio;
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
public class RecomendacaoService {

    private static final double PESO_INTERSECAO = 5.0;
    private static final double PESO_TENDENCIA = 3.0;
    private static final double PESO_FREQUENCIA = 2.0;
    private static final int MAX_SUGESTOES = 10;

    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;

    public List<GiroSugestaoDTO> sugerirProdutos(Long clienteId) {
        clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + clienteId));

        List<Pedido> historico = pedidoRepositorio.buscarPedidosComProdutoPorCliente(clienteId);
        if (historico.isEmpty()) {
            log.info("[GIRO TECH] Cliente {} sem historico -- catalogo generico.", clienteId);
            return sugestoesFallback();
        }

        Set<String> categoriasCliente = historico.stream()
                .map(p -> p.getProduto().getCategoria().getNome())
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> tendencias = categoriaRepositorio.findByTendenciaTrue().stream()
                .map(Categoria::getNome)
                .collect(Collectors.toCollection(HashSet::new));

        // A n B -- categorias do cliente que tambem sao tendencia
        Set<String> intersecao = new HashSet<>(categoriasCliente);
        intersecao.retainAll(tendencias);

        log.debug("[GIRO TECH] Cliente {} | Historico: {} | Tendencias: {} | Intersecao: {}",
                clienteId, categoriasCliente, tendencias, intersecao);

        Set<Long> jaComprou = historico.stream()
                .map(p -> p.getProduto().getId())
                .collect(Collectors.toSet());

        Map<String, Long> freqCategoria = historico.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProduto().getCategoria().getNome(),
                        Collectors.counting()));

        List<Produto> candidatos = produtoRepositorio
                .findByCategoriaIn(categoriaRepositorio.findByTendenciaTrue())
                .stream()
                .filter(p -> !jaComprou.contains(p.getId()))
                .toList();

        List<GiroSugestaoDTO> sugestoes = candidatos.stream()
                .map(prod -> pontuar(prod, intersecao, tendencias, freqCategoria))
                .collect(Collectors.toCollection(ArrayList::new));

        return ordenarPorRelevancia(sugestoes).stream()
                .limit(MAX_SUGESTOES)
                .toList();
    }

    public Set<String> calcularIntersecaoComTendencias(Long clienteId, Set<String> tendencias) {
        List<Pedido> historico = pedidoRepositorio.buscarPedidosComProdutoPorCliente(clienteId);
        Set<String> categoriasCliente = historico.stream()
                .map(p -> p.getProduto().getCategoria().getNome())
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> intersecao = new HashSet<>(categoriasCliente);
        intersecao.retainAll(tendencias);
        return Collections.unmodifiableSet(intersecao);
    }

    // MergeSort manual -- requisito academico do projeto
    public List<GiroSugestaoDTO> ordenarPorRelevancia(List<GiroSugestaoDTO> lista) {
        if (lista.size() <= 1) return new ArrayList<>(lista);

        int meio = lista.size() / 2;
        var esq = ordenarPorRelevancia(new ArrayList<>(lista.subList(0, meio)));
        var dir = ordenarPorRelevancia(new ArrayList<>(lista.subList(meio, lista.size())));
        return merge(esq, dir);
    }

    private List<GiroSugestaoDTO> merge(List<GiroSugestaoDTO> esq, List<GiroSugestaoDTO> dir) {
        List<GiroSugestaoDTO> resultado = new ArrayList<>(esq.size() + dir.size());
        int i = 0, j = 0;
        while (i < esq.size() && j < dir.size()) {
            if (esq.get(i).pontuacao() >= dir.get(j).pontuacao()) {
                resultado.add(esq.get(i++));
            } else {
                resultado.add(dir.get(j++));
            }
        }
        resultado.addAll(esq.subList(i, esq.size()));
        resultado.addAll(dir.subList(j, dir.size()));
        return resultado;
    }

    private GiroSugestaoDTO pontuar(Produto produto, Set<String> intersecao,
                                    Set<String> tendencias, Map<String, Long> freqCategoria) {
        String cat = produto.getCategoria().getNome();
        boolean naIntersecao = intersecao.contains(cat);
        boolean naTendencia = tendencias.contains(cat);
        long freq = freqCategoria.getOrDefault(cat, 0L);

        double pontuacao = (naIntersecao ? PESO_INTERSECAO : 0)
                + (naTendencia ? PESO_TENDENCIA : 0)
                + freq * PESO_FREQUENCIA;

        return new GiroSugestaoDTO(
                produto.getId(), produto.getNome(), cat,
                produto.getPreco(), produto.obterTipoProduto(),
                pontuacao, gerarMotivo(naIntersecao, naTendencia, freq)
        );
    }

    private String gerarMotivo(boolean naIntersecao, boolean naTendencia, long freq) {
        if (naIntersecao && freq > 2) return "Categoria favorita em alta tendencia Giro Tech";
        if (naIntersecao) return "Alinhado ao seu perfil de compras e tendencias";
        if (naTendencia && freq > 0) return "Tendencia Giro Tech compativel com seu historico";
        if (naTendencia) return "Em alta na plataforma Giro Tech";
        return "Descoberta -- produto popular na sua regiao";
    }

    private List<GiroSugestaoDTO> sugestoesFallback() {
        return produtoRepositorio.findAll().stream()
                .limit(MAX_SUGESTOES)
                .map(p -> new GiroSugestaoDTO(
                        p.getId(), p.getNome(), p.getCategoria().getNome(),
                        p.getPreco(), p.obterTipoProduto(),
                        1.0, "Produto em destaque na Giro Tech"))
                .toList();
    }
}
