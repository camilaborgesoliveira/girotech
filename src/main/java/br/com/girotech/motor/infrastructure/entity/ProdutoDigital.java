package br.com.girotech.motor.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("DIGITAL")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class ProdutoDigital extends Produto {

    @Column(name = "url_download", length = 500)
    private String urlDownload;

    @Column(name = "tamanho_mb")
    private Double tamanhoMb;

    @Override
    public String obterTipoProduto() {
        return "Digital";
    }
}
