package br.com.invplus.modules.pagamentos.service;

import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.request.SimulacaoCondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.CondicaoPagamentoResponse;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.financeiro.service.GeradorParcelasService;
import br.com.invplus.modules.financeiro.service.ParcelaGerada;
import br.com.invplus.modules.pagamentos.mapper.CondicaoPagamentoMapper;
import br.com.invplus.modules.pagamentos.repository.CondicaoPagamentoRepository;
import br.com.invplus.modules.pagamentos.specification.CondicaoPagamentoSpecification;
import br.com.invplus.modules.pagamentos.validator.CondicaoPagamentoValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para condicoes de pagamento.
 */
@Service
public class CondicaoPagamentoService extends BaseCrudService<CondicaoPagamento, CondicaoPagamentoRequest, CondicaoPagamentoResponse> {

    private final CondicaoPagamentoRepository repository;
    private final CondicaoPagamentoMapper mapper;
    private final CondicaoPagamentoValidator validator;
    private final GeradorParcelasService geradorParcelasService;

    public CondicaoPagamentoService(
            CondicaoPagamentoRepository repository,
            CondicaoPagamentoMapper mapper,
            CondicaoPagamentoValidator validator,
            GeradorParcelasService geradorParcelasService
    ) {
        super(repository, mapper, "Condicao de pagamento");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
        this.geradorParcelasService = geradorParcelasService;
    }

    @Transactional(readOnly = true)
    public Page<CondicaoPagamentoResponse> listarComFiltros(String codigo, String nome, Boolean ativo, Pageable pageable) {
        Specification<CondicaoPagamento> specification = CondicaoPagamentoSpecification.naoDeletado();
        specification = and(specification, CondicaoPagamentoSpecification.ativo(ativo));
        specification = and(specification, CondicaoPagamentoSpecification.codigoContem(codigo));
        specification = and(specification, CondicaoPagamentoSpecification.nomeContem(nome));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CondicaoPagamentoResponse criar(CondicaoPagamentoRequest request) {
        validator.validarCriacao(request);
        return super.criar(request);
    }

    @Override
    @Transactional
    public CondicaoPagamentoResponse atualizar(UUID id, CondicaoPagamentoRequest request) {
        validator.validarAtualizacao(id, request);
        CondicaoPagamento entity = buscarEntidade(id);
        entity.getParcelas().clear();
        repository.flush();
        mapper.aplicar(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    @Transactional(readOnly = true)
    public java.util.List<ParcelaGerada> simular(UUID id, SimulacaoCondicaoPagamentoRequest request) {
        CondicaoPagamento condicao = buscarEntidade(id);
        return geradorParcelasService.gerar(request.dataBase(), request.valorTotal(), condicao);
    }

    private Specification<CondicaoPagamento> and(Specification<CondicaoPagamento> base, Specification<CondicaoPagamento> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
