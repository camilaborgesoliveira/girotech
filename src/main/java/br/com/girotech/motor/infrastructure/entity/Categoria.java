package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

    @Entity
    @Table(
            name = "categorias"
    )
    public class Categoria {
        @Id
        @GeneratedValue(
                strategy = GenerationType.IDENTITY
        )
        private Long id;
        @Column(
                nullable = false,
                unique = true,
                length = 100
        )
        private String nome;
        @Column(
                length = 500
        )
        private String descricao;
        @Column(
                nullable = false
        )
        private Boolean tendencia;

        private static Boolean $default$tendencia() {
            return false;
        }

        public static CategoriaBuilder builder() {
            return new CategoriaBuilder();
        }

        public Long getId() {
            return this.id;
        }

        public String getNome() {
            return this.nome;
        }

        public String getDescricao() {
            return this.descricao;
        }

        public Boolean getTendencia() {
            return this.tendencia;
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

        public void setTendencia(final Boolean tendencia) {
            this.tendencia = tendencia;
        }

        public Categoria() {
            this.tendencia = $default$tendencia();
        }

        public Categoria(final Long id, final String nome, final String descricao, final Boolean tendencia) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
            this.tendencia = tendencia;
        }

        public String toString() {
            String var10000 = this.getNome();
            return "Categoria(nome=" + var10000 + ", descricao=" + this.getDescricao() + ", tendencia=" + this.getTendencia() + ")";
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Categoria)) {
                return false;
            } else {
                Categoria other = (Categoria)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$nome = this.getNome();
                    Object other$nome = other.getNome();
                    if (this$nome == null) {
                        if (other$nome != null) {
                            return false;
                        }
                    } else if (!this$nome.equals(other$nome)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Categoria;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $nome = this.getNome();
            result = result * 59 + ($nome == null ? 43 : $nome.hashCode());
            return result;
        }

        public static class CategoriaBuilder {
            private Long id;
            private String nome;
            private String descricao;
            private boolean tendencia$set;
            private Boolean tendencia$value;

            CategoriaBuilder() {
            }

            public CategoriaBuilder id(final Long id) {
                this.id = id;
                return this;
            }

            public CategoriaBuilder nome(final String nome) {
                this.nome = nome;
                return this;
            }

            public CategoriaBuilder descricao(final String descricao) {
                this.descricao = descricao;
                return this;
            }

            public CategoriaBuilder tendencia(final Boolean tendencia) {
                this.tendencia$value = tendencia;
                this.tendencia$set = true;
                return this;
            }

            public Categoria build() {
                Boolean tendencia$value = this.tendencia$value;
                if (!this.tendencia$set) {
                    tendencia$value = Categoria.$default$tendencia();
                }

                return new Categoria(this.id, this.nome, this.descricao, tendencia$value);
            }

            public String toString() {
                return "Categoria.CategoriaBuilder(id=" + this.id + ", nome=" + this.nome + ", descricao=" + this.descricao + ", tendencia$value=" + this.tendencia$value + ")";
            }
        }
    }

