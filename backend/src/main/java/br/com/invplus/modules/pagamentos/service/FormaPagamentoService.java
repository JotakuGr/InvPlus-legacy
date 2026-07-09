package br.com.invplus.modules.pagamentos.service;

import br.com.invplus.modules.pagamentos.dto.request.FormaPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.FormaPagamentoResponse;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pagamentos.enums.TipoFormaPagamento;
import br.com.invplus.modules.pagamentos.mapper.FormaPagamentoMapper;
import br.com.invplus.modules.pagamentos.repository.FormaPagamentoRepository;
import br.com.invplus.modules.pagamentos.specification.FormaPagamentoSpecification;
import br.com.invplus.modules.pagamentos.validator.FormaPagamentoValidator;
import br.com.invplus.shared.domain.service.BaseCrudService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para formas de pagamento.
 */
@Service
public class FormaPagamentoService extends BaseCrudService<FormaPagamento, FormaPagamentoRequest, FormaPagamentoResponse> {

    private final FormaPagamentoRepository repository;
    private final FormaPagamentoMapper mapper;
    private final FormaPagamentoValidator validator;

    public FormaPagamentoService(FormaPagamentoRepository repository, FormaPagamentoMapper mapper, FormaPagamentoValidator validator) {
        super(repository, mapper, "Forma de pagamento");
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public Page<FormaPagamentoResponse> listarComFiltros(String codigo, String nome, TipoFormaPagamento tipo, Boolean ativo, Pageable pageable) {
        Specification<FormaPagamento> specification = FormaPagamentoSpecification.naoDeletado();
        specification = and(specification, FormaPagamentoSpecification.ativo(ativo));
        specification = and(specification, FormaPagamentoSpecification.codigoContem(codigo));
        specification = and(specification, FormaPagamentoSpecification.nomeContem(nome));
        specification = and(specification, FormaPagamentoSpecification.tipoIgual(tipo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public FormaPagamentoResponse criar(FormaPagamentoRequest request) {
        validator.validarCriacao(request);
        return super.criar(request);
    }

    @Override
    @Transactional
    public FormaPagamentoResponse atualizar(UUID id, FormaPagamentoRequest request) {
        validator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private Specification<FormaPagamento> and(Specification<FormaPagamento> base, Specification<FormaPagamento> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
