package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DIGITAL")
public class ProdutoDigital extends Produto {
    @Column(
            name = "url_download",
            length = 500
    )
    private String urlDownload;
    @Column(
            name = "tamanho_mb"
    )
    private Double tamanhoMb;

    public ProdutoDigital(String nome, String descricao, BigDecimal preco, Categoria categoria, String urlDownload, Double tamanhoMb) {
        super((Long)null, nome, descricao, preco, categoria);
        this.urlDownload = urlDownload;
        this.tamanhoMb = tamanhoMb;
    }

    public String obterTipoProduto() {
        return "Digital";
    }

    public static ProdutoDigitalBuilder builder() {
        return new ProdutoDigitalBuilder();
    }

    public String getUrlDownload() {
        return this.urlDownload;
    }

    public Double getTamanhoMb() {
        return this.tamanhoMb;
    }

    public void setUrlDownload(final String urlDownload) {
        this.urlDownload = urlDownload;
    }

    public void setTamanhoMb(final Double tamanhoMb) {
        this.tamanhoMb = tamanhoMb;
    }

    public ProdutoDigital() {
    }

    public String toString() {
        String var10000 = super.toString();
        return "ProdutoDigital(super=" + var10000 + ", urlDownload=" + this.getUrlDownload() + ", tamanhoMb=" + this.getTamanhoMb() + ")";
    }

    public static class ProdutoDigitalBuilder {
        private String nome;
        private String descricao;
        private BigDecimal preco;
        private Categoria categoria;
        private String urlDownload;
        private Double tamanhoMb;

        ProdutoDigitalBuilder() {
        }

        public ProdutoDigitalBuilder nome(final String nome) {
            this.nome = nome;
            return this;
        }

        public ProdutoDigitalBuilder descricao(final String descricao) {
            this.descricao = descricao;
            return this;
        }

        public ProdutoDigitalBuilder preco(final BigDecimal preco) {
            this.preco = preco;
            return this;
        }

        public ProdutoDigitalBuilder categoria(final Categoria categoria) {
            this.categoria = categoria;
            return this;
        }

        public ProdutoDigitalBuilder urlDownload(final String urlDownload) {
            this.urlDownload = urlDownload;
            return this;
        }

        public ProdutoDigitalBuilder tamanhoMb(final Double tamanhoMb) {
            this.tamanhoMb = tamanhoMb;
            return this;
        }

        public ProdutoDigital build() {
            return new ProdutoDigital(this.nome, this.descricao, this.preco, this.categoria, this.urlDownload, this.tamanhoMb);
        }

        public String toString() {
            String var10000 = this.nome;
            return "ProdutoDigital.ProdutoDigitalBuilder(nome=" + var10000 + ", descricao=" + this.descricao + ", preco=" + String.valueOf(this.preco) + ", categoria=" + String.valueOf(this.categoria) + ", urlDownload=" + this.urlDownload + ", tamanhoMb=" + this.tamanhoMb + ")";
        }
    }
}
