package br.com.invplus.modules.produtos.specification;

import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.NaturezaProduto;
import br.com.invplus.modules.produtos.enums.StatusProduto;
import br.com.invplus.modules.produtos.enums.TipoProduto;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications reutilizaveis para consultas dinamicas de Produtos.
 */
public final class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    /**
     * Mantem o soft delete fora das consultas publicas.
     */
    public static Specification<Produto> naoDeletado() {
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    /**
     * Filtra por ativo quando informado; por padrao lista somente produtos ativos.
     */
    public static Specification<Produto> ativo(Boolean ativo) {
        return (root, query, builder) -> builder.equal(root.get("ativo"), ativo == null ? Boolean.TRUE : ativo);
    }

    public static Specification<Produto> nomeContem(String nome) {
        return textoInformado(nome)
                ? (root, query, builder) -> builder.like(builder.lower(root.get("nome")), like(nome))
                : sempreVerdadeiro();
    }

    public static Specification<Produto> codigoContem(String codigo) {
        return textoInformado(codigo)
                ? (root, query, builder) -> builder.like(builder.lower(root.get("codigo")), like(codigo))
                : sempreVerdadeiro();
    }

    public static Specification<Produto> skuContem(String sku) {
        return textoInformado(sku)
                ? (root, query, builder) -> builder.like(builder.lower(root.get("sku")), like(sku))
                : sempreVerdadeiro();
    }

    public static Specification<Produto> codigoBarrasContem(String codigoBarras) {
        return textoInformado(codigoBarras)
                ? (root, query, builder) -> builder.like(builder.lower(root.get("codigoBarras")), like(codigoBarras))
                : sempreVerdadeiro();
    }

    public static Specification<Produto> categoriaIgual(UUID categoriaId) {
        return categoriaId != null
                ? (root, query, builder) -> builder.equal(root.get("categoria").get("id"), categoriaId)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> marcaIgual(UUID marcaId) {
        return marcaId != null
                ? (root, query, builder) -> builder.equal(root.get("marca").get("id"), marcaId)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> tipoIgual(TipoProduto tipoProduto) {
        return tipoProduto != null
                ? (root, query, builder) -> builder.equal(root.get("tipoProduto"), tipoProduto)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> naturezaIgual(NaturezaProduto naturezaProduto) {
        return naturezaProduto != null
                ? (root, query, builder) -> builder.equal(root.get("naturezaProduto"), naturezaProduto)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> statusIgual(StatusProduto statusProduto) {
        return statusProduto != null
                ? (root, query, builder) -> builder.equal(root.get("statusProduto"), statusProduto)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> controlaEstoqueIgual(Boolean controlaEstoque) {
        return controlaEstoque != null
                ? (root, query, builder) -> builder.equal(root.get("controlaEstoque"), controlaEstoque)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> permiteCompraIgual(Boolean permiteCompra) {
        return permiteCompra != null
                ? (root, query, builder) -> builder.equal(root.get("permiteCompra"), permiteCompra)
                : sempreVerdadeiro();
    }

    public static Specification<Produto> permiteVendaIgual(Boolean permiteVenda) {
        return permiteVenda != null
                ? (root, query, builder) -> builder.equal(root.get("permiteVenda"), permiteVenda)
                : sempreVerdadeiro();
    }

    /**
     * Busca rapida usada por telas operacionais como compra, venda e PDV.
     */
    public static Specification<Produto> termoRapido(String termo) {
        if (!textoInformado(termo)) {
            return sempreVerdadeiro();
        }
        return (root, query, builder) -> {
            String like = like(termo);
            return builder.or(
                    builder.like(builder.lower(root.get("nome")), like),
                    builder.like(builder.lower(root.get("codigo")), like),
                    builder.like(builder.lower(root.get("sku")), like),
                    builder.like(builder.lower(root.get("codigoBarras")), like)
            );
        };
    }

    private static Specification<Produto> sempreVerdadeiro() {
        return (root, query, builder) -> builder.conjunction();
    }

    private static boolean textoInformado(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static String like(String valor) {
        return "%" + valor.trim().toLowerCase() + "%";
    }
}
