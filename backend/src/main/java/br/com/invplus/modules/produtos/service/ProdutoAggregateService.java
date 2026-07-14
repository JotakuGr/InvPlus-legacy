package br.com.invplus.modules.produtos.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.invplus.modules.produtos.dto.response.ProdutoCodigoBarrasResponse;
import br.com.invplus.modules.produtos.dto.response.ProdutoCompletoResponse;
import br.com.invplus.modules.produtos.dto.response.ProdutoFornecedorResponse;
import br.com.invplus.modules.produtos.dto.response.ProdutoImagemResponse;
import br.com.invplus.modules.produtos.dto.response.ProdutoPrecoResponse;
import br.com.invplus.modules.produtos.dto.response.ProdutoUnidadeResponse;
import br.com.invplus.modules.produtos.entity.Produto;
import br.com.invplus.modules.produtos.enums.TipoPrecoProduto;
import br.com.invplus.modules.produtos.mapper.ProdutoCodigoBarrasMapper;
import br.com.invplus.modules.produtos.mapper.ProdutoFiscalMapper;
import br.com.invplus.modules.produtos.mapper.ProdutoFornecedorMapper;
import br.com.invplus.modules.produtos.mapper.ProdutoImagemMapper;
import br.com.invplus.modules.produtos.mapper.ProdutoMapper;
import br.com.invplus.modules.produtos.mapper.ProdutoPrecoMapper;
import br.com.invplus.modules.produtos.mapper.ProdutoUnidadeMapper;
import br.com.invplus.modules.produtos.repository.ProdutoCodigoBarrasRepository;
import br.com.invplus.modules.produtos.repository.ProdutoFiscalRepository;
import br.com.invplus.modules.produtos.repository.ProdutoFornecedorRepository;
import br.com.invplus.modules.produtos.repository.ProdutoImagemRepository;
import br.com.invplus.modules.produtos.repository.ProdutoPrecoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoRepository;
import br.com.invplus.modules.produtos.repository.ProdutoUnidadeRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;

/**
 * Monta uma visao consolidada do Produto e seus cadastros filhos.
 */
@Service
public class ProdutoAggregateService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoFornecedorRepository fornecedorRepository;
    private final ProdutoPrecoRepository precoRepository;
    private final ProdutoImagemRepository imagemRepository;
    private final ProdutoCodigoBarrasRepository codigoBarrasRepository;
    private final ProdutoFiscalRepository fiscalRepository;
    private final ProdutoUnidadeRepository unidadeRepository;
    private final ProdutoMapper produtoMapper;
    private final ProdutoFornecedorMapper fornecedorMapper;
    private final ProdutoPrecoMapper precoMapper;
    private final ProdutoImagemMapper imagemMapper;
    private final ProdutoCodigoBarrasMapper codigoBarrasMapper;
    private final ProdutoFiscalMapper fiscalMapper;
    private final ProdutoUnidadeMapper unidadeMapper;

    public ProdutoAggregateService(
            ProdutoRepository produtoRepository,
            ProdutoFornecedorRepository fornecedorRepository,
            ProdutoPrecoRepository precoRepository,
            ProdutoImagemRepository imagemRepository,
            ProdutoCodigoBarrasRepository codigoBarrasRepository,
            ProdutoFiscalRepository fiscalRepository,
            ProdutoUnidadeRepository unidadeRepository,
            ProdutoMapper produtoMapper,
            ProdutoFornecedorMapper fornecedorMapper,
            ProdutoPrecoMapper precoMapper,
            ProdutoImagemMapper imagemMapper,
            ProdutoCodigoBarrasMapper codigoBarrasMapper,
            ProdutoFiscalMapper fiscalMapper,
            ProdutoUnidadeMapper unidadeMapper
    ) {
        this.produtoRepository = produtoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.precoRepository = precoRepository;
        this.imagemRepository = imagemRepository;
        this.codigoBarrasRepository = codigoBarrasRepository;
        this.fiscalRepository = fiscalRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoMapper = produtoMapper;
        this.fornecedorMapper = fornecedorMapper;
        this.precoMapper = precoMapper;
        this.imagemMapper = imagemMapper;
        this.codigoBarrasMapper = codigoBarrasMapper;
        this.fiscalMapper = fiscalMapper;
        this.unidadeMapper = unidadeMapper;
    }

    /**
     * Carrega Produto com fornecedores, precos, imagens, codigos, fiscal e unidades ativas.
     */
    @Transactional(readOnly = true)
    public ProdutoCompletoResponse buscar(UUID id) {
        Produto produto = produtoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));

        List<ProdutoFornecedorResponse> fornecedores = fornecedorRepository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(id)
                .stream()
                .map(fornecedorMapper::toResponse)
                .toList();
        List<ProdutoImagemResponse> imagens = imagemRepository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(id)
                .stream()
                .sorted(Comparator.comparing(entity -> entity.getOrdem() == null ? 0 : entity.getOrdem()))
                .map(imagemMapper::toResponse)
                .toList();
        List<ProdutoCodigoBarrasResponse> codigos = codigoBarrasRepository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(id)
                .stream()
                .map(codigoBarrasMapper::toResponse)
                .toList();
        List<ProdutoPrecoResponse> precos = precoRepository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(id)
                .stream()
                .map(precoMapper::toResponse)
                .toList();
        List<ProdutoUnidadeResponse> unidades = unidadeRepository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(id)
                .stream()
                .map(unidadeMapper::toResponse)
                .toList();

        return new ProdutoCompletoResponse(
                produtoMapper.toResponse(produto),
                fornecedores.stream().filter(item -> Boolean.TRUE.equals(item.principal())).findFirst().orElse(null),
                fornecedores,
                imagens.stream().filter(item -> Boolean.TRUE.equals(item.principal())).findFirst().orElse(null),
                imagens,
                codigos.stream().filter(item -> Boolean.TRUE.equals(item.principal())).findFirst().orElse(null),
                codigos,
                fiscalRepository.findByProdutoIdAndAtivoTrueAndDeletedAtIsNull(id).map(fiscalMapper::toResponse).orElse(null),
                precoPorTipo(precos, TipoPrecoProduto.VENDA),
                precoPorTipo(precos, TipoPrecoProduto.ATACADO),
                precoPorTipo(precos, TipoPrecoProduto.PROMOCIONAL),
                precos,
                unidades.stream().filter(item -> Boolean.TRUE.equals(item.unidadePadrao())).findFirst().orElse(null),
                unidades.stream().filter(unidade -> !Boolean.TRUE.equals(unidade.unidadePadrao())).toList()
        );
    }

    private ProdutoPrecoResponse precoPorTipo(List<ProdutoPrecoResponse> precos, TipoPrecoProduto tipoPreco) {
        return precos.stream()
                .filter(preco -> preco.tipoPreco() == tipoPreco)
                .filter(preco -> Boolean.TRUE.equals(preco.principal()))
                .findFirst()
                .orElseGet(() -> precos.stream().filter(preco -> preco.tipoPreco() == tipoPreco).findFirst().orElse(null));
    }
}
