package br.com.invplus.modules.estoque.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.enums.MovimentacaoOrigem;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Quantidade separada de um saldo de estoque sem baixa fisica imediata.
 */
@Getter
@Setter
@Entity
@Table(name = "reserva_estoque")
@NoArgsConstructor
public class ReservaEstoque extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estoque_id", nullable = false)
    private Estoque estoque;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id", nullable = false)
    private Deposito deposito;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "localizacao_id", nullable = false)
    private LocalizacaoEstoque localizacao;

    @NotNull
    @DecimalMin(value = "0.000", inclusive = false)
    @Column(name = "quantidade", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusReservaEstoque status = StatusReservaEstoque.ATIVA;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 60)
    private MovimentacaoOrigem origem;

    @Column(name = "origem_id")
    private UUID origemId;

    @Column(name = "expira_em")
    private OffsetDateTime expiraEm;

    @Column(name = "baixada_em")
    private OffsetDateTime baixadaEm;

    @Column(name = "cancelada_em")
    private OffsetDateTime canceladaEm;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Copia os vinculos atuais do saldo para preservar a fotografia da reserva.
     */
    public void vincularEstoque(Estoque estoque) {
        this.estoque = estoque;
        this.produto = estoque.getProduto();
        this.empresa = estoque.getEmpresa();
        this.filial = estoque.getFilial();
        this.deposito = estoque.getDeposito();
        this.localizacao = estoque.getLocalizacao();
    }

    /**
     * Preenche os dados funcionais informados na criacao.
     */
    public void atualizarDados(BigDecimal quantidade, MovimentacaoOrigem origem, UUID origemId,
                               OffsetDateTime expiraEm, String observacao) {
        this.quantidade = quantidade;
        this.origem = origem;
        this.origemId = origemId;
        this.expiraEm = expiraEm;
        this.observacao = observacao;
    }

    /**
     * Marca a reserva como cancelada e preserva a data da transicao.
     */
    public void cancelar() {
        this.status = StatusReservaEstoque.CANCELADA;
        this.canceladaEm = OffsetDateTime.now();
    }

    /**
     * Marca a reserva como baixada e preserva a data da transicao.
     */
    public void baixar() {
        this.status = StatusReservaEstoque.BAIXADA;
        this.baixadaEm = OffsetDateTime.now();
    }
}
