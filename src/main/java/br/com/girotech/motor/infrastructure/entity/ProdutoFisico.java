package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("FISICO")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class ProdutoFisico extends Produto {

    @Column(name = "peso_kg")
    private Double peso;

    @Column(name = "dimensoes", length = 50)
    private String dimensoes;

    @Column(name = "requer_entrega")
    private Boolean requerEntrega;

    @Override
    public String obterTipoProduto() {
        return "Físico";
    }
}
