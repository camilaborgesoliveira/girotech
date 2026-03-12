package br.com.girotech.motor.infrastructure.service;

import br.com.girotech.motor.dto.GiroEstatisticaDTO;
import br.com.girotech.motor.infrastructure.exceptions.RecursoNaoEncontradoException;
import br.com.girotech.motor.infrastructure.entity.Cliente;
import br.com.girotech.motor.infrastructure.entity.Pedido;
import br.com.girotech.motor.infrastructure.repository.ClienteRepositorio;
import br.com.girotech.motor.infrastructure.repository.PedidoRepositorio;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(
        readOnly = true
)
public class AnaliseEstatisticaService {
    private static final Logger log = LoggerFactory.getLogger(AnaliseEstatisticaService.class);
    private static final double LIMIAR_PREMIUM = 800.0;
    private static final double LIMIAR_INTERMEDIARIO = 300.0;
    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    public AnaliseEstatisticaService(ClienteRepositorio clienteRepositorio, PedidoRepositorio pedidoRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    public GiroEstatisticaDTO analisarCliente(Long clienteId) {
        Cliente cliente = this.clienteRepositorio.findById(clienteId).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com id: " + clienteId));
        List<Pedido> pedidos = this.pedidoRepositorio.buscarPedidosComProdutoPorCliente(clienteId);
        if (pedidos.isEmpty()) {
            log.warn("[GIRO TECH] Cliente {} não possui pedidos para análise.", clienteId);
            return new GiroEstatisticaDTO(clienteId, cliente.getNome(), 0.0, "N/A", 0.0, "Novo", 0L);
        } else {
            double ticketMedio = this.calcularTicketMedio(pedidos);
            String moda = this.calcularModaCategoria(pedidos);
            double desvioPadrao = this.calcularDesvioPadrao(pedidos, ticketMedio);
            String segmento = this.segmentarCliente(ticketMedio);
            GiroEstatisticaDTO estatistica = new GiroEstatisticaDTO(clienteId, cliente.getNome(), ticketMedio, moda, desvioPadrao, segmento, (long)pedidos.size());
            log.debug(estatistica.resumoEstatistico());
            return estatistica;
        }
    }

    private double calcularTicketMedio(List<Pedido> pedidos) {
        return pedidos.stream().mapToDouble((p) -> p.getValorTotal().doubleValue()).average().orElse(0.0);
    }

    private String calcularModaCategoria(List<Pedido> pedidos) {
        Map<String, Long> frequencia = pedidos.stream().collect(Collectors.groupingBy((p) -> p.getProduto().getCategoria().getNome(), Collectors.counting()));
        return Collections.max(frequencia.entrySet(), Entry.comparingByValue()).getKey();
    }

    private double calcularDesvioPadrao(List<Pedido> pedidos, double ticketMedio) {
        double variancia = pedidos.stream().mapToDouble((p) -> {
            double diferenca = p.getValorTotal().doubleValue() - ticketMedio;
            return diferenca * diferenca;
        }).average().orElse(0.0);
        return Math.sqrt(variancia);
    }

    private String segmentarCliente(double ticketMedio) {
        if (ticketMedio >= LIMIAR_PREMIUM) {
            return "Premium";
        } else {
            return ticketMedio >= LIMIAR_INTERMEDIARIO ? "Intermediário" : "Econômico";
        }
    }
}
