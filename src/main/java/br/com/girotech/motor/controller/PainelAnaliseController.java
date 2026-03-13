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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
public class PainelAnaliseController {

    private final AnaliseEstatisticaService analiseService;
    private final AnaliseGeralService analiseGeralService;
    private final RecomendacaoService recomendacaoService;
    private final ClienteRepositorio clienteRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "sistema", "Motor de Recomendacao Giro Tech",
                "versao", "1.0.0",
                "status", "OPERACIONAL",
                "totalClientes", clienteRepositorio.count(),
                "totalProdutos", produtoRepositorio.count()
        ));
    }

    @GetMapping("/analise-geral")
    public ResponseEntity<GiroAnaliseGeralDTO> analiseGeral() {
        log.info("[GIRO TECH] Gerando analise geral completa...");
        return ResponseEntity.ok(analiseGeralService.analisarGeral());
    }

    @GetMapping("/clientes/{id}/estatisticas")
    public ResponseEntity<GiroEstatisticaDTO> obterEstatisticas(@PathVariable Long id) {
        log.info("[GIRO TECH] Calculando estatisticas para cliente id={}", id);
        return ResponseEntity.ok(analiseService.analisarCliente(id));
    }

    @GetMapping("/clientes/{id}/sugestoes")
    public ResponseEntity<List<GiroSugestaoDTO>> obterSugestoes(@PathVariable Long id) {
        log.info("[GIRO TECH] Gerando sugestoes para cliente id={}", id);
        return ResponseEntity.ok(recomendacaoService.sugerirProdutos(id));
    }

    @GetMapping("/clientes/{id}/intersecao")
    public ResponseEntity<Map<String, Object>> calcularIntersecao(
            @PathVariable Long id, @RequestParam Set<String> tendencias) {
        log.info("[GIRO TECH] Calculando intersecao para cliente id={}", id);
        Set<String> intersecao = recomendacaoService.calcularIntersecaoComTendencias(id, tendencias);
        return ResponseEntity.ok(Map.of(
                "clienteId", id,
                "tendencias", tendencias,
                "intersecao", intersecao,
                "tamanho", intersecao.size()
        ));
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteRepositorio.findAll());
    }

    @GetMapping("/produtos")
    public ResponseEntity<List<Produto>> listarProdutos() {
        return ResponseEntity.ok(produtoRepositorio.findAll());
    }
}
