package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Linha individual importada dentro de um extrato bancario.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "extrato_bancario_lancamento")
public class ExtratoBancarioLancamento extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "extrato_bancario_id", nullable = false)
    private ExtratoBancario extratoBancario;

    @NotNull
    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @NotNull
    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "documento", length = 80)
    private String documento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoLancamentoBancario tipo;

    @NotNull
    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_apos_lancamento", precision = 15, scale = 2)
    private BigDecimal saldoAposLancamento;

    @Column(name = "identificador_bancario", length = 120)
    private String identificadorBancario;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_bancaria", nullable = false, length = 30)
    private CategoriaLancamentoBancario categoriaBancaria = CategoriaLancamentoBancario.OUTRO;

    @NotNull
    @Column(name = "conciliado", nullable = false)
    private Boolean conciliado = false;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    /**
     * Aplica os dados operacionais do lancamento sem alterar conciliacao.
     */
    public void atualizar(ExtratoBancario extratoBancario, LocalDate dataLancamento, String descricao,
            String documento, TipoLancamentoBancario tipo, BigDecimal valor, BigDecimal saldoAposLancamento,
            String identificadorBancario, CategoriaLancamentoBancario categoriaBancaria, String observacao) {
        this.extratoBancario = extratoBancario;
        this.dataLancamento = dataLancamento;
        this.descricao = normalizarObrigatorio(descricao);
        this.documento = normalizarTexto(documento);
        this.tipo = tipo;
        this.valor = valor;
        this.saldoAposLancamento = saldoAposLancamento;
        this.identificadorBancario = normalizarTexto(identificadorBancario);
        this.categoriaBancaria = categoriaBancaria == null ? CategoriaLancamentoBancario.OUTRO : categoriaBancaria;
        this.observacao = normalizarTexto(observacao);
        if (this.conciliado == null) {
            this.conciliado = false;
        }
    }

    /**
     * Marca a linha como conciliada para impedir alteracoes manuais posteriores.
     */
    public void marcarComoConciliado() {
        this.conciliado = true;
    }

    /**
     * Libera a linha para ajustes quando uma conciliacao futura for desfeita.
     */
    public void desmarcarComoConciliado() {
        this.conciliado = false;
    }

    /**
     * Garante que lancamentos conciliados nao sejam editados ou excluidos.
     */
    public void validarNaoConciliado(String acao) {
        if (Boolean.TRUE.equals(this.conciliado)) {
            throw new InvalidOperationException("Lancamento conciliado nao pode ser " + acao);
        }
    }

    private String normalizarObrigatorio(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
