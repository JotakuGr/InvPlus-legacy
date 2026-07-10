package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.shared.domain.entity.BaseEntity;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Preserva o vinculo auditavel entre uma linha bancaria e um movimento financeiro.
 * Nao usa soft delete porque cancelamentos fazem parte do historico.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conciliacao_bancaria")
public class ConciliacaoBancaria extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "extrato_bancario_lancamento_id", nullable = false)
    private ExtratoBancarioLancamento extratoBancarioLancamento;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movimento_financeiro_id", nullable = false)
    private MovimentoFinanceiro movimentoFinanceiro;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_bancaria_id", nullable = false)
    private ContaBancaria contaBancaria;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conciliacao", nullable = false, length = 20)
    private TipoConciliacaoBancaria tipoConciliacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusConciliacaoBancaria status;

    @NotNull
    @Column(name = "valor_extrato", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorExtrato;

    @NotNull
    @Column(name = "valor_movimento", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMovimento;

    @NotNull
    @Column(name = "diferenca", nullable = false, precision = 15, scale = 2)
    private BigDecimal diferenca;

    @NotNull
    @Column(name = "conciliado_em", nullable = false)
    private OffsetDateTime conciliadoEm;

    @Column(name = "conciliado_por")
    private UUID conciliadoPor;

    @Column(name = "cancelado_em")
    private OffsetDateTime canceladoEm;

    @Column(name = "cancelado_por")
    private UUID canceladoPor;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    /** Inicializa uma conciliacao exata depois que todas as regras foram validadas. */
    public void conciliar(ExtratoBancarioLancamento lancamento, MovimentoFinanceiro movimento,
            TipoConciliacaoBancaria tipo, UUID usuarioId, String observacao) {
        this.extratoBancarioLancamento = lancamento;
        this.movimentoFinanceiro = movimento;
        this.contaBancaria = lancamento.getExtratoBancario().getContaBancaria();
        this.tipoConciliacao = tipo;
        this.status = StatusConciliacaoBancaria.CONCILIADA;
        this.valorExtrato = lancamento.getValor();
        this.valorMovimento = movimento.getValor();
        this.diferenca = lancamento.getValor().subtract(movimento.getValor()).abs();
        this.conciliadoEm = OffsetDateTime.now();
        this.conciliadoPor = usuarioId;
        this.observacao = normalizar(observacao);
    }

    /** Cancela o vinculo sem apagar a evidencia historica. */
    public void cancelar(String motivo, UUID usuarioId) {
        if (status == StatusConciliacaoBancaria.CANCELADA) {
            throw new InvalidOperationException("Conciliacao bancaria ja esta cancelada");
        }
        if (motivo == null || motivo.isBlank() || motivo.trim().length() < 5) {
            throw new InvalidOperationException("Motivo do cancelamento deve possuir ao menos 5 caracteres");
        }
        this.status = StatusConciliacaoBancaria.CANCELADA;
        this.canceladoEm = OffsetDateTime.now();
        this.canceladoPor = usuarioId;
        this.motivoCancelamento = motivo.trim();
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
