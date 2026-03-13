package br.com.girotech.motor.dto;

public record GiroEstatisticaDTO(
        Long clienteId,
        String nomeCliente,
        Double ticketMedio,
        String modaCategoria,
        Double desvioPadrao,
        String segmento,
        Long totalPedidos
) {
    public String resumoEstatistico() {
        return String.format(
                "[GIRO STATS] Cliente: %s | Segmento: %s | u=R$%.2f | o=%.2f | Moda=%s | Pedidos=%d",
                nomeCliente, segmento, ticketMedio, desvioPadrao, modaCategoria, totalPedidos
        );
    }
}