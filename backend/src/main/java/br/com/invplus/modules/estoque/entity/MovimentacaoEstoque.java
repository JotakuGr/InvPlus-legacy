package br.com.invplus.modules.estoque.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.TipoMovimentacaoEstoque;
import br.com.invplus.modules.produtos.entity.Produto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Historico imutavel de alteracoes que impactam o saldo de estoque.
 */
@Getter
@Setter
@Entity
@Table(name = "movimentacao_estoque")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estoque_id", nullable = false, updatable = false)
    private Estoque estoque;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, updatable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id", updatable = false)
    private Filial filial;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id", nullable = false, updatable = false)
    private Deposito deposito;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localizacao_id", nullable = false, updatable = false)
    private LocalizacaoEstoque localizacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false, length = 40, updatable = false)
    private TipoMovimentacaoEstoque tipoMovimentacao;

    @NotNull
    @DecimalMin(value = "0.000", inclusive = false)
    @Column(name = "quantidade", nullable = false, precision = 14, scale = 3, updatable = false)
    private BigDecimal quantidade;

    @NotNull
    @Column(name = "saldo_anterior", nullable = false, precision = 14, scale = 3, updatable = false)
    private BigDecimal saldoAnterior;

    @NotNull
    @Column(name = "saldo_posterior", nullable = false, precision = 14, scale = 3, updatable = false)
    private BigDecimal saldoPosterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", length = 60, updatable = false)
    private MovimentacaoOrigem origem;

    @Column(name = "origem_id", updatable = false)
    private UUID origemId;

    @Column(name = "documento", length = 120, updatable = false)
    private String documento;

    @Column(name = "observacao", updatable = false)
    private String observacao;

    @NotNull
    @Column(name = "data_movimentacao", nullable = false, updatable = false)
    private OffsetDateTime dataMovimentacao;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @CreatedBy
    @Column(name = "criado_por", updatable = false)
    private UUID criadoPor;

    /**
     * Preenche datas imutaveis quando o registro for persistido.
     */
    @PrePersist
    protected void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (dataMovimentacao == null) {
            dataMovimentacao = now;
        }
        if (criadoEm == null) {
            criadoEm = now;
        }
    }

    /**
     * Copia os vinculos do saldo para congelar o contexto da movimentacao.
     */
    public void vincularEstoque(Estoque estoque) {
        this.estoque = estoque;
        this.produto = estoque.getProduto();
        this.empresa = estoque.getEmpresa();
        this.filial = estoque.getFilial();
        this.deposito = estoque.getDeposito();
        this.localizacao = estoque.getLocalizacao();
    }
}
