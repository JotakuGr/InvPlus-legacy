package br.com.invplus.modules.produtos.mapper;

import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.entity.Marca;
import br.com.invplus.modules.cadastros.entity.UnidadeMedida;
import br.com.invplus.modules.produtos.dto.request.ProdutoRequest;
import br.com.invplus.modules.produtos.dto.response.ProdutoResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Produto entre entidade JPA e DTOs sem consultar repositories.
 */
@Component
public class ProdutoMapper implements CrudMapper<Produto, ProdutoRequest, ProdutoResponse> {

    @Override
    public Produto novaEntidade() {
        return new Produto();
    }

    /**
     * Cria produto sem relacionamentos; o service associa Categoria, Marca e Unidade.
     */
    public Produto toEntity(ProdutoRequest request) {
        Produto produto = novaEntidade();
        updateEntity(produto, request);
        return produto;
    }

    @Override
    public ProdutoResponse toResponse(Produto produto) {
        Categoria categoria = produto.getCategoria();
        Marca marca = produto.getMarca();
        UnidadeMedida unidade = produto.getUnidadeMedida();
        return new ProdutoResponse(
                produto.getId(),
                produto.getCodigo(),
                produto.getSku(),
                produto.getCodigoInterno(),
                produto.getCodigoBarras(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getTipoProduto(),
                produto.getNaturezaProduto(),
                produto.getStatusProduto(),
                categoria.getId(),
                categoria.getNome(),
                marca == null ? null : marca.getId(),
                marca == null ? null : marca.getNome(),
                unidade.getId(),
                unidade.getSigla(),
                produto.getPrecoCusto(),
                produto.getPrecoVenda(),
                produto.getPesoLiquido(),
                produto.getPesoBruto(),
                produto.getAlturaCm(),
                produto.getLarguraCm(),
                produto.getComprimentoCm(),
                produto.getEstoqueMinimo(),
                produto.getControlaLote(),
                produto.getControlaValidade(),
                produto.getPermiteFracionamento(),
                produto.getControlaEstoque(),
                produto.getPermiteCompra(),
                produto.getPermiteVenda(),
                produto.getAtivo(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(Produto produto, ProdutoRequest request) {
        updateEntity(produto, request);
    }

    /**
     * Atualiza apenas os campos proprios do produto.
     */
    public void updateEntity(Produto produto, ProdutoRequest request) {
        produto.atualizar(
                request.codigo(),
                request.sku(),
                request.codigoInterno(),
                request.codigoBarras(),
                request.nome(),
                request.descricao(),
                request.tipoProduto(),
                request.naturezaProduto(),
                request.statusProduto(),
                request.precoCusto(),
                request.precoVenda(),
                request.pesoLiquido(),
                request.pesoBruto(),
                request.alturaCm(),
                request.larguraCm(),
                request.comprimentoCm(),
                request.estoqueMinimo(),
                request.controlaLote(),
                request.controlaValidade(),
                request.permiteFracionamento(),
                request.controlaEstoque(),
                request.permiteCompra(),
                request.permiteVenda()
        );
    }
}
