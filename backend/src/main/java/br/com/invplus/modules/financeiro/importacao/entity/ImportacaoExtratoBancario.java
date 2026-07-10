package br.com.invplus.modules.financeiro.importacao.entity;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.importacao.enums.FormatoArquivoExtrato;
import br.com.invplus.modules.financeiro.importacao.enums.StatusImportacaoExtrato;
import br.com.invplus.shared.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registra o processo de importacao sem substituir o extrato bancario. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "importacao_extrato_bancario")
public class ImportacaoExtratoBancario extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id")
    private ContaBancaria contaBancaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extrato_bancario_id")
    private ExtratoBancario extratoBancario;

    @NotNull
    @Column(name = "arquivo_nome", nullable = false, length = 255)
    private String arquivoNome;

    @NotNull
    @Column(name = "arquivo_hash", nullable = false, length = 64)
    private String arquivoHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "formato", nullable = false, length = 20)
    private FormatoArquivoExtrato formato;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusImportacaoExtrato status;

    @NotNull
    @Column(name = "quantidade_lancamentos_lidos", nullable = false)
    private Integer quantidadeLancamentosLidos = 0;

    @NotNull
    @Column(name = "quantidade_lancamentos_importados", nullable = false)
    private Integer quantidadeLancamentosImportados = 0;

    @NotNull
    @Column(name = "quantidade_lancamentos_ignorados", nullable = false)
    private Integer quantidadeLancamentosIgnorados = 0;

    @Column(name = "mensagem_erro", length = 1000)
    private String mensagemErro;

    @NotNull
    @Column(name = "iniciada_em", nullable = false)
    private OffsetDateTime iniciadaEm;

    @Column(name = "finalizada_em")
    private OffsetDateTime finalizadaEm;

    public void receber(String nome, String hash) {
        this.arquivoNome = nome;
        this.arquivoHash = hash;
        this.formato = FormatoArquivoExtrato.OFX;
        this.status = StatusImportacaoExtrato.RECEBIDA;
        this.iniciadaEm = OffsetDateTime.now();
    }

    public void iniciar() {
        this.status = StatusImportacaoExtrato.PROCESSANDO;
    }

    public void concluir(ContaBancaria conta, ExtratoBancario extrato, int lidos,
            int importados, int ignorados) {
        this.contaBancaria = conta;
        this.extratoBancario = extrato;
        this.quantidadeLancamentosLidos = lidos;
        this.quantidadeLancamentosImportados = importados;
        this.quantidadeLancamentosIgnorados = ignorados;
        this.status = ignorados > 0 ? StatusImportacaoExtrato.CONCLUIDA_COM_ALERTAS
                : StatusImportacaoExtrato.CONCLUIDA;
        this.finalizadaEm = OffsetDateTime.now();
    }

    public void falhar(String mensagem) {
        this.status = StatusImportacaoExtrato.FALHOU;
        this.mensagemErro = mensagem == null ? "Falha nao detalhada" : mensagem.substring(0, Math.min(1000, mensagem.length()));
        this.finalizadaEm = OffsetDateTime.now();
    }
}
