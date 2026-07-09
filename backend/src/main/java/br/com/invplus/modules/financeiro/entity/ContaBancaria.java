package br.com.invplus.modules.financeiro.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Conta bancaria ou caixa interno usado para pagamentos, recebimentos e conciliacao futura.
 */
@Getter
@Setter
@Entity
@Table(name = "conta_bancaria", indexes = {
        @Index(name = "idx_conta_bancaria_empresa_jpa", columnList = "empresa_id"),
        @Index(name = "idx_conta_bancaria_filial_jpa", columnList = "filial_id"),
        @Index(name = "idx_conta_bancaria_banco_jpa", columnList = "banco_codigo"),
        @Index(name = "idx_conta_bancaria_tipo_jpa", columnList = "tipo_conta")
})
@NoArgsConstructor
public class ContaBancaria extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotBlank
    @Size(max = 20)
    @Column(name = "banco_codigo", nullable = false, length = 20)
    private String bancoCodigo;

    @NotBlank
    @Size(max = 120)
    @Column(name = "banco_nome", nullable = false, length = 120)
    private String bancoNome;

    @NotBlank
    @Size(max = 20)
    @Column(name = "agencia", nullable = false, length = 20)
    private String agencia;

    @Size(max = 5)
    @Column(name = "agencia_digito", length = 5)
    private String agenciaDigito;

    @NotBlank
    @Size(max = 30)
    @Column(name = "numero_conta", nullable = false, length = 30)
    private String numeroConta;

    @Size(max = 5)
    @Column(name = "conta_digito", length = 5)
    private String contaDigito;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 20)
    private TipoContaBancaria tipoConta;

    @NotBlank
    @Size(max = 160)
    @Column(name = "titular", nullable = false, length = 160)
    private String titular;

    @Size(max = 14)
    @Column(name = "documento_titular", length = 14)
    private String documentoTitular;

    @Size(max = 160)
    @Column(name = "pix_chave", length = 160)
    private String pixChave;

    @NotNull
    @PositiveOrZero
    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "saldo_atual", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoAtual = BigDecimal.ZERO;

    @Column(name = "data_saldo_inicial")
    private LocalDate dataSaldoInicial;

    @NotNull
    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    /**
     * Atualiza dados cadastrais e reinicia o saldo atual quando a conta ainda esta em cadastro.
     */
    public void atualizar(Empresa empresa, Filial filial, String bancoCodigo, String bancoNome,
            String agencia, String agenciaDigito, String numeroConta, String contaDigito,
            TipoContaBancaria tipoConta, String titular, String documentoTitular, String pixChave,
            BigDecimal saldoInicial, LocalDate dataSaldoInicial, Boolean principal) {
        this.empresa = empresa;
        this.filial = filial;
        this.bancoCodigo = normalizarTextoCurto(bancoCodigo);
        this.bancoNome = normalizarTexto(bancoNome);
        this.agencia = normalizarTextoCurto(agencia);
        this.agenciaDigito = normalizarTextoCurto(agenciaDigito);
        this.numeroConta = normalizarTextoCurto(numeroConta);
        this.contaDigito = normalizarTextoCurto(contaDigito);
        this.tipoConta = tipoConta;
        this.titular = normalizarTexto(titular);
        this.documentoTitular = normalizarDocumento(documentoTitular);
        this.pixChave = normalizarTexto(pixChave);
        this.saldoInicial = saldoInicial == null ? BigDecimal.ZERO : saldoInicial;
        this.saldoAtual = this.saldoInicial;
        this.dataSaldoInicial = dataSaldoInicial;
        this.principal = Boolean.TRUE.equals(principal);
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarTextoCurto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }

    private String normalizarDocumento(String valor) {
        return valor == null || valor.isBlank() ? null : valor.replaceAll("\\D", "");
    }
}
