package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.PlanoContaRequest;
import br.com.invplus.modules.financeiro.dto.response.PlanoContaResponse;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.mapper.PlanoContaMapper;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.modules.financeiro.specification.PlanoContaSpecification;
import br.com.invplus.modules.financeiro.validator.PlanoContaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o cadastro hierarquico do plano de contas por empresa.
 */
@Service
@RequiredArgsConstructor
public class PlanoContaService {

    private final PlanoContaRepository repository;
    private final EmpresaRepository empresaRepository;
    private final PlanoContaMapper mapper;
    private final PlanoContaValidator validator;

    @Transactional
    public PlanoContaResponse criar(PlanoContaRequest request) {
        Empresa empresa = buscarEmpresa(request.empresaId());
        PlanoConta pai = buscarPai(request.planoContaPaiId());
        validator.validarCriacao(request, empresa, pai);

        PlanoConta entity = new PlanoConta();
        mapper.aplicar(entity, request, empresa, pai);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public PlanoContaResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PlanoContaResponse> listar(UUID empresaId, UUID planoContaPaiId,
            String codigo, String nome, TipoPlanoConta tipo, NaturezaPlanoConta natureza,
            Boolean ativo, Pageable pageable) {
        Specification<PlanoConta> specification = PlanoContaSpecification.naoDeletado();
        specification = and(specification, PlanoContaSpecification.empresaIgual(empresaId));
        specification = and(specification, PlanoContaSpecification.paiIgual(planoContaPaiId));
        specification = and(specification, PlanoContaSpecification.codigoContem(codigo));
        specification = and(specification, PlanoContaSpecification.nomeContem(nome));
        specification = and(specification, PlanoContaSpecification.tipoIgual(tipo));
        specification = and(specification, PlanoContaSpecification.naturezaIgual(natureza));
        specification = and(specification, PlanoContaSpecification.ativo(ativo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public PlanoContaResponse atualizar(UUID id, PlanoContaRequest request) {
        PlanoConta entity = buscarEntidade(id);
        Empresa empresa = buscarEmpresa(request.empresaId());
        PlanoConta pai = buscarPai(request.planoContaPaiId());
        validator.validarAtualizacao(id, request, empresa, pai);
        mapper.aplicar(entity, request, empresa, pai);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    public PlanoConta buscarEntidade(UUID id) {
        return repository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de conta nao encontrado"));
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private PlanoConta buscarPai(UUID id) {
        return id == null ? null : buscarEntidade(id);
    }

    private Specification<PlanoConta> and(Specification<PlanoConta> base,
            Specification<PlanoConta> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
