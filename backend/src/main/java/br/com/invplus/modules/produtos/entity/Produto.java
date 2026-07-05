package br.com.invplus.modules.produtos.entity;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
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
 * Produto comercializado ou controlado pelo ERP, base para estoque, compras, vendas e fiscal.
 */
@Getter
@Setter
@Entity
@Table(
        name = "produto",
        indexes = {
                @Index(name = "idx_produto_categoria", columnList = "categoria_id"),
                @Index(name = "idx_produto_marca", columnList = "marca_id"),
                @Index(name = "idx_produto_unidade", columnList = "unidade_medida_id"),
                @Index(name = "idx_produto_tipo_natureza", columnList = "tipo_produto,natureza_produto"),
                @Index(name = "idx_produto_status", columnList = "status_produto")
        }
)
@NoArgsConstructor
public class Produto extends SoftDeleteEntity {

    @NotBlank
    @Size(max = 60)
    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Size(max = 80)
    @Column(name = "sku", length = 80)
    private String sku;

    @Size(max = 80)
    @Column(name = "codigo_interno", length = 80)
    private String codigoInterno;

    @Size(max = 80)
    @Column(name = "codigo_barras", length = 80)
    /*
     * Campo legado mantido por compatibilidade. Novos fluxos devem usar ProdutoCodigoBarras.
     */
    private String codigoBarras;

    @NotBlank
    @Size(max = 180)
    @Column(name = "nome", nullable = false, length = 180)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false, length = 40)
    private TipoProduto tipoProduto = TipoProduto.PRODUTO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_produto", nullable = false, length = 20)
    private NaturezaProduto naturezaProduto = NaturezaProduto.AMBOS;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_produto", nullable = false, length = 30)
    private StatusProduto statusProduto = StatusProduto.ATIVO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_medida_id", nullable = false)
    private UnidadeMedida unidadeMedida;

    @NotNull
    @PositiveOrZero
    @Column(name = "preco_custo", nullable = false, precision = 15, scale = 4)
    /*
     * Campo legado mantido por compatibilidade. Novos fluxos devem usar ProdutoPreco ou ProdutoFornecedorPreco.
     */
    private BigDecimal precoCusto = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 4)
    /*
     * Campo legado mantido por compatibilidade. Novos fluxos devem usar ProdutoPreco.
     */
    private BigDecimal precoVenda = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "peso_liquido", nullable = false, precision = 14, scale = 3)
    private BigDecimal pesoLiquido = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "peso_bruto", nullable = false, precision = 14, scale = 3)
    private BigDecimal pesoBruto = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "altura_cm", nullable = false, precision = 14, scale = 3)
    private BigDecimal alturaCm = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "largura_cm", nullable = false, precision = 14, scale = 3)
    private BigDecimal larguraCm = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "comprimento_cm", nullable = false, precision = 14, scale = 3)
    private BigDecimal comprimentoCm = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "estoque_minimo", nullable = false, precision = 14, scale = 3)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    @Column(name = "controla_lote", nullable = false)
    private Boolean controlaLote = false;

    @Column(name = "controla_validade", nullable = false)
    private Boolean controlaValidade = false;

    @Column(name = "permite_fracionamento", nullable = false)
    private Boolean permiteFracionamento = false;

    @Column(name = "controla_estoque", nullable = false)
    private Boolean controlaEstoque = true;

    @Column(name = "permite_compra", nullable = false)
    private Boolean permiteCompra = true;

    @Column(name = "permite_venda", nullable = false)
    private Boolean permiteVenda = true;

    /**
     * Atualiza dados descritivos, precos e controles operacionais do produto.
     */
    public void atualizar(
            String codigo,
            String sku,
            String codigoInterno,
            String codigoBarras,
            String nome,
            String descricao,
            TipoProduto tipoProduto,
            NaturezaProduto naturezaProduto,
            StatusProduto statusProduto,
            BigDecimal precoCusto,
            BigDecimal precoVenda,
            BigDecimal pesoLiquido,
            BigDecimal pesoBruto,
            BigDecimal alturaCm,
            BigDecimal larguraCm,
            BigDecimal comprimentoCm,
            BigDecimal estoqueMinimo,
            Boolean controlaLote,
            Boolean controlaValidade,
            Boolean permiteFracionamento,
            Boolean controlaEstoque,
            Boolean permiteCompra,
            Boolean permiteVenda
    ) {
        this.codigo = normalizarTexto(codigo);
        this.sku = normalizarTexto(sku);
        this.codigoInterno = normalizarTexto(codigoInterno);
        this.codigoBarras = normalizarTexto(codigoBarras);
        this.nome = normalizarTexto(nome);
        this.descricao = normalizarTexto(descricao);
        this.tipoProduto = tipoProduto == null ? TipoProduto.PRODUTO : tipoProduto;
        this.naturezaProduto = naturezaProduto == null ? NaturezaProduto.AMBOS : naturezaProduto;
        this.statusProduto = statusProduto == null ? StatusProduto.ATIVO : statusProduto;
        this.precoCusto = valorOuZero(precoCusto);
        this.precoVenda = valorOuZero(precoVenda);
        this.pesoLiquido = valorOuZero(pesoLiquido);
        this.pesoBruto = valorOuZero(pesoBruto);
        this.alturaCm = valorOuZero(alturaCm);
        this.larguraCm = valorOuZero(larguraCm);
        this.comprimentoCm = valorOuZero(comprimentoCm);
        this.estoqueMinimo = valorOuZero(estoqueMinimo);
        this.controlaLote = Boolean.TRUE.equals(controlaLote);
        this.controlaValidade = Boolean.TRUE.equals(controlaValidade);
        this.permiteFracionamento = Boolean.TRUE.equals(permiteFracionamento);
        this.controlaEstoque = controlaEstoque == null ? Boolean.TRUE : controlaEstoque;
        this.permiteCompra = permiteCompra == null ? Boolean.TRUE : permiteCompra;
        this.permiteVenda = permiteVenda == null ? Boolean.TRUE : permiteVenda;
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
