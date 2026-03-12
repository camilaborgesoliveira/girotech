package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "pedidos",
        indexes = {@Index(
                name = "idx_pedido_cliente",
                columnList = "cliente_id"
        ), @Index(
                name = "idx_pedido_data",
                columnList = "data_compra"
        )}
)
public class Pedido {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "cliente_id",
            nullable = false
    )
    private Cliente cliente;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "produto_id",
            nullable = false
    )
    private Produto produto;
    @Column(
            name = "data_compra",
            nullable = false
    )
    private LocalDate dataCompra;
    @Column(
            name = "valor_total",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal valorTotal;

    public static PedidoBuilder builder() {
        return new PedidoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Cliente getCliente() {
        return this.cliente;
    }

    public Produto getProduto() {
        return this.produto;
    }

    public LocalDate getDataCompra() {
        return this.dataCompra;
    }

    public BigDecimal getValorTotal() {
        return this.valorTotal;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setCliente(final Cliente cliente) {
        this.cliente = cliente;
    }

    public void setProduto(final Produto produto) {
        this.produto = produto;
    }

    public void setDataCompra(final LocalDate dataCompra) {
        this.dataCompra = dataCompra;
    }

    public void setValorTotal(final BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Pedido() {
    }

    public Pedido(final Long id, final Cliente cliente, final Produto produto, final LocalDate dataCompra, final BigDecimal valorTotal) {
        this.id = id;
        this.cliente = cliente;
        this.produto = produto;
        this.dataCompra = dataCompra;
        this.valorTotal = valorTotal;
    }

    public String toString() {
        Long var10000 = this.getId();
        return "Pedido(id=" + var10000 + ", dataCompra=" + String.valueOf(this.getDataCompra()) + ", valorTotal=" + String.valueOf(this.getValorTotal()) + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Pedido)) {
            return false;
        } else {
            Pedido other = (Pedido)o;
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
        return other instanceof Pedido;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        return result;
    }

    public static class PedidoBuilder {
        private Long id;
        private Cliente cliente;
        private Produto produto;
        private LocalDate dataCompra;
        private BigDecimal valorTotal;

        PedidoBuilder() {
        }

        public PedidoBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        public PedidoBuilder cliente(final Cliente cliente) {
            this.cliente = cliente;
            return this;
        }

        public PedidoBuilder produto(final Produto produto) {
            this.produto = produto;
            return this;
        }

        public PedidoBuilder dataCompra(final LocalDate dataCompra) {
            this.dataCompra = dataCompra;
            return this;
        }

        public PedidoBuilder valorTotal(final BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }

        public Pedido build() {
            return new Pedido(this.id, this.cliente, this.produto, this.dataCompra, this.valorTotal);
        }

        public String toString() {
            Long var10000 = this.id;
            return "Pedido.PedidoBuilder(id=" + var10000 + ", cliente=" + String.valueOf(this.cliente) + ", produto=" + String.valueOf(this.produto) + ", dataCompra=" + String.valueOf(this.dataCompra) + ", valorTotal=" + String.valueOf(this.valorTotal) + ")";
        }
    }
}
