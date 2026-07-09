package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa uma importacao de extrato bancario e seus saldos de referencia.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "extrato_bancario")
public class ExtratoBancario extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_bancaria_id", nullable = false)
    private ContaBancaria contaBancaria;

    @NotNull
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @NotNull
    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @NotNull
    @Column(name = "saldo_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoFinal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 30)
    private OrigemExtratoBancario origem;

    @Column(name = "arquivo_nome", length = 255)
    private String arquivoNome;

    @Column(name = "arquivo_hash", length = 64)
    private String arquivoHash;

    @NotNull
    @Column(name = "importado_em", nullable = false)
    private OffsetDateTime importadoEm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusExtratoBancario status;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    /**
     * Prepara uma importacao nova com status inicial e data automatica.
     */
    public void importar(ContaBancaria contaBancaria, LocalDate dataInicio, LocalDate dataFim,
            BigDecimal saldoInicial, BigDecimal saldoFinal, OrigemExtratoBancario origem,
            String arquivoNome, String arquivoHash, String observacao) {
        this.contaBancaria = contaBancaria;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.saldoInicial = saldoInicial;
        this.saldoFinal = saldoFinal;
        this.origem = origem;
        this.arquivoNome = normalizarTexto(arquivoNome);
        this.arquivoHash = normalizarHash(arquivoHash);
        this.importadoEm = OffsetDateTime.now();
        this.status = StatusExtratoBancario.IMPORTADO;
        this.observacao = normalizarTexto(observacao);
    }

    /**
     * Cancela o extrato sem remove-lo do historico financeiro.
     */
    public void cancelar() {
        if (StatusExtratoBancario.CANCELADO.equals(this.status)) {
            throw new InvalidOperationException("Extrato bancario ja esta cancelado");
        }
        this.status = StatusExtratoBancario.CANCELADO;
    }

    /**
     * Monta uma descricao curta da conta para respostas de API.
     */
    public String descricaoContaBancaria() {
        if (contaBancaria == null) {
            return null;
        }
        return contaBancaria.getBancoNome() + " - Ag. " + contaBancaria.getAgencia()
                + " - Conta " + contaBancaria.getNumeroConta();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarHash(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toLowerCase();
    }
}
