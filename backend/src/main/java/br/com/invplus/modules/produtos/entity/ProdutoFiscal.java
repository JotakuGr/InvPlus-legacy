package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.produtos.enums.OrigemMercadoria;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados fiscais do produto, separados do cadastro comercial para suportar regras tributarias.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto_fiscal",
        indexes = {
                @Index(name = "idx_produto_fiscal_produto", columnList = "produto_id"),
                @Index(name = "idx_produto_fiscal_ncm", columnList = "ncm"),
                @Index(name = "idx_produto_fiscal_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class ProdutoFiscal extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotBlank
    @Size(min = 8, max = 8)
    @Column(name = "ncm", nullable = false, length = 8)
    private String ncm;

    @Size(max = 7)
    @Column(name = "cest", length = 7)
    private String cest;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "origem_mercadoria", nullable = false, length = 60)
    private OrigemMercadoria origemMercadoria = OrigemMercadoria.NACIONAL;

    @Size(max = 10)
    @Column(name = "cfop_padrao", length = 10)
    private String cfopPadrao;

    @Size(max = 10)
    @Column(name = "cst_icms", length = 10)
    private String cstIcms;

    @Size(max = 10)
    @Column(name = "csosn", length = 10)
    private String csosn;

    @Size(max = 10)
    @Column(name = "cst_pis", length = 10)
    private String cstPis;

    @Size(max = 10)
    @Column(name = "cst_cofins", length = 10)
    private String cstCofins;

    @NotNull
    @PositiveOrZero
    @Column(name = "aliquota_icms", nullable = false, precision = 7, scale = 4)
    private BigDecimal aliquotaIcms = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "aliquota_ipi", nullable = false, precision = 7, scale = 4)
    private BigDecimal aliquotaIpi = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "aliquota_pis", nullable = false, precision = 7, scale = 4)
    private BigDecimal aliquotaPis = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "aliquota_cofins", nullable = false, precision = 7, scale = 4)
    private BigDecimal aliquotaCofins = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza os campos fiscais mantendo o Produto associado sob responsabilidade do service.
     */
    public void atualizar(
            String ncm,
            String cest,
            OrigemMercadoria origemMercadoria,
            String cfopPadrao,
            String cstIcms,
            String csosn,
            String cstPis,
            String cstCofins,
            BigDecimal aliquotaIcms,
            BigDecimal aliquotaIpi,
            BigDecimal aliquotaPis,
            BigDecimal aliquotaCofins,
            String observacao
    ) {
        this.ncm = normalizarNumeros(ncm);
        this.cest = normalizarNumeros(cest);
        this.origemMercadoria = origemMercadoria == null ? OrigemMercadoria.NACIONAL : origemMercadoria;
        this.cfopPadrao = normalizarTexto(cfopPadrao);
        this.cstIcms = normalizarTexto(cstIcms);
        this.csosn = normalizarTexto(csosn);
        this.cstPis = normalizarTexto(cstPis);
        this.cstCofins = normalizarTexto(cstCofins);
        this.aliquotaIcms = valorOuZero(aliquotaIcms);
        this.aliquotaIpi = valorOuZero(aliquotaIpi);
        this.aliquotaPis = valorOuZero(aliquotaPis);
        this.aliquotaCofins = valorOuZero(aliquotaCofins);
        this.observacao = normalizarTexto(observacao);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarNumeros(String valor) {
        return valor == null || valor.isBlank() ? null : valor.replaceAll("\\D", "");
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
