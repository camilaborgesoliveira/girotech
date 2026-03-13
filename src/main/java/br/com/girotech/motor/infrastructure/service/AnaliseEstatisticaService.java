package br.com.girotech.motor.infrastructure.service;

import br.com.girotech.motor.dto.GiroEstatisticaDTO;
import br.com.girotech.motor.infrastructure.entity.Cliente;
import br.com.girotech.motor.infrastructure.entity.Pedido;
import br.com.girotech.motor.infrastructure.exceptions.RecursoNaoEncontradoException;
import br.com.girotech.motor.infrastructure.repository.ClienteRepositorio;
import br.com.girotech.motor.infrastructure.repository.PedidoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnaliseEstatisticaService {

    private static final double LIMIAR_PREMIUM = 800.0;
    private static final double LIMIAR_INTERMEDIARIO = 300.0;

    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    public GiroEstatisticaDTO analisarCliente(Long clienteId) {
        Cliente cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + clienteId));

        List<Pedido> pedidos = pedidoRepositorio.buscarPedidosComProdutoPorCliente(clienteId);
        if (pedidos.isEmpty()) {
            log.warn("[GIRO TECH] Cliente {} nao possui pedidos para analise.", clienteId);
            return new GiroEstatisticaDTO(clienteId, cliente.getNome(), 0.0, "N/A", 0.0, "Novo", 0L);
        }

        double ticketMedio = calcularTicketMedio(pedidos);
        String moda = calcularModaCategoria(pedidos);
        double desvio = calcularDesvioPadrao(pedidos, ticketMedio);
        String segmento = segmentarCliente(ticketMedio);

        var dto = new GiroEstatisticaDTO(
                clienteId, cliente.getNome(), ticketMedio,
                moda, desvio, segmento, (long) pedidos.size()
        );
        log.debug(dto.resumoEstatistico());
        return dto;
    }

    private double calcularTicketMedio(List<Pedido> pedidos) {
        return pedidos.stream()
                .mapToDouble(p -> p.getValorTotal().doubleValue())
                .average().orElse(0.0);
    }

    private String calcularModaCategoria(List<Pedido> pedidos) {
        var frequencia = pedidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProduto().getCategoria().getNome(),
                        Collectors.counting()));
        return Collections.max(frequencia.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double calcularDesvioPadrao(List<Pedido> pedidos, double media) {
        double variancia = pedidos.stream().mapToDouble(p -> {
            double diff = p.getValorTotal().doubleValue() - media;
            return diff * diff;
        }).average().orElse(0.0);
        return Math.sqrt(variancia);
    }

    private String segmentarCliente(double ticketMedio) {
        if (ticketMedio >= LIMIAR_PREMIUM) return "Premium";
        if (ticketMedio >= LIMIAR_INTERMEDIARIO) return "Intermediário";
        return "Econômico";
    }
}
