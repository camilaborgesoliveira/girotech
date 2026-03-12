package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("FISICO")
public class ProdutoFisico extends Produto {
    @Column(
            name = "peso_kg"
    )
    private Double peso;
    @Column(
            name = "dimensoes",
            length = 50
    )
    private String dimensoes;
    @Column(
            name = "requer_entrega"
    )
    private Boolean requerEntrega;

    public ProdutoFisico(String nome, String descricao, BigDecimal preco, Categoria categoria, Double peso, String dimensoes, Boolean requerEntrega) {
        super((Long)null, nome, descricao, preco, categoria);
        this.peso = peso;
        this.dimensoes = dimensoes;
        this.requerEntrega = requerEntrega;
    }

    public String obterTipoProduto() {
        return "Físico";
    }

    public static ProdutoFisicoBuilder builder() {
        return new ProdutoFisicoBuilder();
    }

    public Double getPeso() {
        return this.peso;
    }

    public String getDimensoes() {
        return this.dimensoes;
    }

    public Boolean getRequerEntrega() {
        return this.requerEntrega;
    }

    public void setPeso(final Double peso) {
        this.peso = peso;
    }

    public void setDimensoes(final String dimensoes) {
        this.dimensoes = dimensoes;
    }

    public void setRequerEntrega(final Boolean requerEntrega) {
        this.requerEntrega = requerEntrega;
    }

    public ProdutoFisico() {
    }

    public String toString() {
        String var10000 = super.toString();
        return "ProdutoFisico(super=" + var10000 + ", peso=" + this.getPeso() + ", dimensoes=" + this.getDimensoes() + ", requerEntrega=" + this.getRequerEntrega() + ")";
    }

    public static class ProdutoFisicoBuilder {
        private String nome;
        private String descricao;
        private BigDecimal preco;
        private Categoria categoria;
        private Double peso;
        private String dimensoes;
        private Boolean requerEntrega;

        ProdutoFisicoBuilder() {
        }

        public ProdutoFisicoBuilder nome(final String nome) {
            this.nome = nome;
            return this;
        }

        public ProdutoFisicoBuilder descricao(final String descricao) {
            this.descricao = descricao;
            return this;
        }

        public ProdutoFisicoBuilder preco(final BigDecimal preco) {
            this.preco = preco;
            return this;
        }

        public ProdutoFisicoBuilder categoria(final Categoria categoria) {
            this.categoria = categoria;
            return this;
        }

        public ProdutoFisicoBuilder peso(final Double peso) {
            this.peso = peso;
            return this;
        }

        public ProdutoFisicoBuilder dimensoes(final String dimensoes) {
            this.dimensoes = dimensoes;
            return this;
        }

        public ProdutoFisicoBuilder requerEntrega(final Boolean requerEntrega) {
            this.requerEntrega = requerEntrega;
            return this;
        }

        public ProdutoFisico build() {
            return new ProdutoFisico(this.nome, this.descricao, this.preco, this.categoria, this.peso, this.dimensoes, this.requerEntrega);
        }

        public String toString() {
            String var10000 = this.nome;
            return "ProdutoFisico.ProdutoFisicoBuilder(nome=" + var10000 + ", descricao=" + this.descricao + ", preco=" + String.valueOf(this.preco) + ", categoria=" + String.valueOf(this.categoria) + ", peso=" + this.peso + ", dimensoes=" + this.dimensoes + ", requerEntrega=" + this.requerEntrega + ")";
        }
    }
}
