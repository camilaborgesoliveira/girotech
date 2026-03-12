package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "clientes",
        indexes = {@Index(
                name = "idx_cliente_email",
                columnList = "email",
                unique = true
        ), @Index(
                name = "idx_cliente_cpf",
                columnList = "cpf",
                unique = true
        )}
)
public class Cliente {
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
            nullable = false,
            unique = true,
            length = 200
    )
    private String email;
    @Column(
            nullable = false,
            unique = true,
            length = 14
    )
    private String cpf;
    @Column(
            length = 100
    )
    private String cidade;
    @Column(
            length = 2
    )
    private String estado;
    @Column(
            name = "data_cadastro",
            nullable = false
    )
    private LocalDate dataCadastro;
    @lombok.Setter
    @lombok.Getter
    @OneToMany(
            mappedBy = "cliente",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<Pedido> pedidos = new ArrayList<>();

    public List<Pedido> getPedidos() {
        return this.pedidos;
    }

    public void setPedidos(final List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public static ClienteBuilder builder() {
        return new ClienteBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public String getEmail() {
        return this.email;
    }

    public String getCpf() {
        return this.cpf;
    }

    public String getCidade() {
        return this.cidade;
    }

    public String getEstado() {
        return this.estado;
    }

    public LocalDate getDataCadastro() {
        return this.dataCadastro;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setNome(final String nome) {
        this.nome = nome;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setCpf(final String cpf) {
        this.cpf = cpf;
    }

    public void setCidade(final String cidade) {
        this.cidade = cidade;
    }

    public void setEstado(final String estado) {
        this.estado = estado;
    }

    public void setDataCadastro(final LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Cliente() {
    }

    public Cliente(final Long id, final String nome, final String email, final String cpf, final String cidade, final String estado, final LocalDate dataCadastro, final List<Pedido> pedidos) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.cidade = cidade;
        this.estado = estado;
        this.dataCadastro = dataCadastro;
        this.pedidos = pedidos;
    }

    public String toString() {
        Long var10000 = this.getId();
        return "Cliente(id=" + var10000 + ", nome=" + this.getNome() + ", email=" + this.getEmail() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Cliente)) {
            return false;
        } else {
            Cliente other = (Cliente)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$cpf = this.getCpf();
                Object other$cpf = other.getCpf();
                if (this$cpf == null) {
                    if (other$cpf != null) {
                        return false;
                    }
                } else if (!this$cpf.equals(other$cpf)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Cliente;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $cpf = this.getCpf();
        result = result * 59 + ($cpf == null ? 43 : $cpf.hashCode());
        return result;
    }

    public static class ClienteBuilder {
        private Long id;
        private String nome;
        private String email;
        private String cpf;
        private String cidade;
        private String estado;
        private LocalDate dataCadastro;
        private boolean pedidos$set;
        private List<Pedido> pedidos$value;

        ClienteBuilder() {
        }

        public ClienteBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        public ClienteBuilder nome(final String nome) {
            this.nome = nome;
            return this;
        }

        public ClienteBuilder email(final String email) {
            this.email = email;
            return this;
        }

        public ClienteBuilder cpf(final String cpf) {
            this.cpf = cpf;
            return this;
        }

        public ClienteBuilder cidade(final String cidade) {
            this.cidade = cidade;
            return this;
        }

        public ClienteBuilder estado(final String estado) {
            this.estado = estado;
            return this;
        }

        public ClienteBuilder dataCadastro(final LocalDate dataCadastro) {
            this.dataCadastro = dataCadastro;
            return this;
        }

        public ClienteBuilder pedidos(final List<Pedido> pedidos) {
            this.pedidos$value = pedidos;
            this.pedidos$set = true;
            return this;
        }

        public Cliente build() {
            List<Pedido> pedidos$value = this.pedidos$set ? this.pedidos$value : new ArrayList<>();
            return new Cliente(this.id, this.nome, this.email, this.cpf,
                               this.cidade, this.estado, this.dataCadastro, pedidos$value);
        }

        public String toString() {
            return "Cliente.ClienteBuilder(id=" + this.id + ", nome=" + this.nome + ", email=" + this.email
                    + ", cpf=" + this.cpf + ", cidade=" + this.cidade + ", estado=" + this.estado
                    + ", dataCadastro=" + this.dataCadastro + ")";
        }
    }
}
