package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(
        name = "produtos"
)
@Inheritance(
        strategy = InheritanceType.SINGLE_TABLE
)
@DiscriminatorColumn(
        name = "tipo_produto",
        discriminatorType = DiscriminatorType.STRING,
        length = 10
)
public abstract class Produto {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @Column(
            nullable = false,
            length = 200
    )
    private String nome;
    @Column(
            length = 1000
    )
    private String descricao;
    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal preco;
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "categoria_id",
            nullable = false
    )
    private Categoria categoria;

    public abstract String obterTipoProduto();

    public Long getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public BigDecimal getPreco() {
        return this.preco;
    }

    public Categoria getCategoria() {
        return this.categoria;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setNome(final String nome) {
        this.nome = nome;
    }

    public void setDescricao(final String descricao) {
        this.descricao = descricao;
    }

    public void setPreco(final BigDecimal preco) {
        this.preco = preco;
    }

    public void setCategoria(final Categoria categoria) {
        this.categoria = categoria;
    }

    public Produto() {
    }

    public Produto(final Long id, final String nome, final String descricao, final BigDecimal preco, final Categoria categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
    }

    public String toString() {
        Long var10000 = this.getId();
        return "Produto(id=" + var10000 + ", nome=" + this.getNome() + ", preco=" + String.valueOf(this.getPreco()) + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Produto)) {
            return false;
        } else {
            Produto other = (Produto)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$id = this.getId();
                Object other$id = other.getId();
                if (this$id == null) {
                    if (other$id != null) {
                        return false;
                    }
                } else if (!this$id.equals(other$id)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Produto;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        return result;
    }
}
