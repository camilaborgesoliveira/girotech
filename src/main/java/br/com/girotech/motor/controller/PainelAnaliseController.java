package br.com.girotech.motor.controller;

import br.com.girotech.motor.dto.GiroAnaliseGeralDTO;
import br.com.girotech.motor.dto.GiroEstatisticaDTO;
import br.com.girotech.motor.dto.GiroSugestaoDTO;
import br.com.girotech.motor.infrastructure.entity.Cliente;
import br.com.girotech.motor.infrastructure.entity.Produto;
import br.com.girotech.motor.infrastructure.repository.ClienteRepositorio;
import br.com.girotech.motor.infrastructure.repository.ProdutoRepositorio;
import br.com.girotech.motor.infrastructure.service.AnaliseEstatisticaService;
import br.com.girotech.motor.infrastructure.service.AnaliseGeralService;
import br.com.girotech.motor.infrastructure.service.RecomendacaoService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/painel"})
public class PainelAnaliseController {

    private static final Logger log = LoggerFactory.getLogger(PainelAnaliseController.class);
    private final AnaliseEstatisticaService analiseService;
    private final AnaliseGeralService       analiseGeralService;
    private final RecomendacaoService       recomendacaoService;
    private final ClienteRepositorio        clienteRepositorio;
    private final ProdutoRepositorio        produtoRepositorio;

    public PainelAnaliseController(AnaliseEstatisticaService analiseService,
                                   AnaliseGeralService analiseGeralService,
                                   RecomendacaoService recomendacaoService,
                                   ClienteRepositorio clienteRepositorio,
                                   ProdutoRepositorio produtoRepositorio) {
        this.analiseService      = analiseService;
        this.analiseGeralService = analiseGeralService;
        this.recomendacaoService = recomendacaoService;
        this.clienteRepositorio  = clienteRepositorio;
        this.produtoRepositorio  = produtoRepositorio;
    }

    /** Status do sistema */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "sistema",        "Motor de Recomendação Giro Tech",
            "versao",         "1.0.0",
            "status",         "OPERACIONAL",
            "totalClientes",  clienteRepositorio.count(),
            "totalProdutos",  produtoRepositorio.count()
        ));
    }

    /** ── ANÁLISE GERAL ─────────────────────────────────────────────────
     *  GET /api/painel/analise-geral
     *  Retorna: ticket médio global, desvio padrão, moda, vendasPorCategoria
     *           (HashMap), categoriasOrdenadas (TreeSet), historicoRecente
     *           (LinkedList), segmentação (conjuntos) e probabilidades P(Y|X).
     */
    @GetMapping("/analise-geral")
    public ResponseEntity<GiroAnaliseGeralDTO> analiseGeral() {
        log.info("[GIRO TECH] Gerando análise geral completa...");
        return ResponseEntity.ok(analiseGeralService.analisarGeral());
    }

    /** ── ESTATÍSTICAS POR CLIENTE ──────────────────────────────────────
     *  GET /api/painel/clientes/{id}/estatisticas
     *  Retorna: ticket médio, moda, desvio padrão e segmento do cliente.
     */
    @GetMapping("/clientes/{id}/estatisticas")
    public ResponseEntity<GiroEstatisticaDTO> obterEstatisticas(@PathVariable Long id) {
        log.info("[GIRO TECH] Calculando estatísticas para cliente id={}", id);
        return ResponseEntity.ok(analiseService.analisarCliente(id));
    }

    /** ── SUGESTÕES POR CLIENTE (MergeSort + Interseção de Conjuntos) ───
     *  GET /api/painel/clientes/{id}/sugestoes
     *  Retorna: produtos recomendados ordenados por pontuação (MergeSort).
     */
    @GetMapping("/clientes/{id}/sugestoes")
    public ResponseEntity<List<GiroSugestaoDTO>> obterSugestoes(@PathVariable Long id) {
        log.info("[GIRO TECH] Gerando sugestões para cliente id={}", id);
        return ResponseEntity.ok(recomendacaoService.sugerirProdutos(id));
    }

    /** ── INTERSEÇÃO DE CONJUNTOS ────────────────────────────────────────
     *  GET /api/painel/clientes/{id}/intersecao?tendencias=Games,Eletrônicos
     *  Retorna: A ∩ B — categorias do cliente que são tendência.
     */
    @GetMapping("/clientes/{id}/intersecao")
    public ResponseEntity<Map<String, Object>> calcularIntersecao(
            @PathVariable Long id,
            @RequestParam Set<String> tendencias) {
        log.info("[GIRO TECH] Calculando interseção para cliente id={}", id);
        Set<String> intersecao = recomendacaoService.calcularIntersecaoComTendencias(id, tendencias);
        return ResponseEntity.ok(Map.of(
            "clienteId",  id,
            "tendencias", tendencias,
            "intersecao", intersecao,
            "tamanho",    intersecao.size()
        ));
    }

    /** Listar todos os clientes */
    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteRepositorio.findAll());
    }

    /** Listar catálogo de produtos */
    @GetMapping("/produtos")
    public ResponseEntity<List<Produto>> listarProdutos() {
        return ResponseEntity.ok(produtoRepositorio.findAll());
    }
}
