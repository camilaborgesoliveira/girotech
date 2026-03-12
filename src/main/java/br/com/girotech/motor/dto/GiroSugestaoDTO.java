package br.com.girotech.motor.dto;

import java.math.BigDecimal;

public record GiroSugestaoDTO(Long produtoId, String nomeProduto, String categoria, BigDecimal preco, String tipoProduto, Double pontuacao, String motivo) implements Comparable<GiroSugestaoDTO> {
    public int compareTo(GiroSugestaoDTO outro) {
        return Double.compare(outro.pontuacao(), this.pontuacao());
    }
}

