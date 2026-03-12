package br.com.girotech.motor.dto;

public record GiroEstatisticaDTO(Long clienteId, String nomeCliente, Double ticketMedio, String modaCategoria, Double desvioPadrao, String segmento, Long totalPedidos) {
    public String resumoEstatistico() {
        return String.format("[GIRO STATS] Cliente: %s | Segmento: %s | μ=R$%.2f | σ=%.2f | Moda=%s | Pedidos=%d", this.nomeCliente, this.segmento, this.ticketMedio, this.desvioPadrao, this.modaCategoria, this.totalPedidos);
    }
}