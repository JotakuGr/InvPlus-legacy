package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.financeiro.dto.request.CentroCustoRequest;
import br.com.invplus.modules.financeiro.dto.response.CentroCustoResponse;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.mapper.CentroCustoMapper;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.modules.financeiro.specification.CentroCustoSpecification;
import br.com.invplus.modules.financeiro.validator.CentroCustoValidator;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o cadastro hierarquico de centros de custo por empresa.
 */
@Service
@RequiredArgsConstructor
public class CentroCustoService {

    private final CentroCustoRepository repository;
    private final EmpresaRepository empresaRepository;
    private final CentroCustoMapper mapper;
    private final CentroCustoValidator validator;
    private final EmpresaContext empresaContext;

    @Transactional
    public CentroCustoResponse criar(CentroCustoRequest request) {
        Empresa empresa = buscarEmpresa(empresaContext.getEmpresaId());
        CentroCusto pai = buscarPai(request.centroCustoPaiId());
        validator.validarCriacao(request, empresa, pai);

        CentroCusto entity = new CentroCusto();
        mapper.aplicar(entity, request, empresa, pai);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public CentroCustoResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<CentroCustoResponse> listar(UUID centroCustoPaiId,
            String codigo, String nome, TipoCentroCusto tipo, Boolean ativo, Pageable pageable) {
        UUID empresaId = empresaContext.getEmpresaId();
        Specification<CentroCusto> specification = CentroCustoSpecification.naoDeletado();
        specification = and(specification, CentroCustoSpecification.empresaIgual(empresaId));
        specification = and(specification, CentroCustoSpecification.paiIgual(centroCustoPaiId));
        specification = and(specification, CentroCustoSpecification.codigoContem(codigo));
        specification = and(specification, CentroCustoSpecification.nomeContem(nome));
        specification = and(specification, CentroCustoSpecification.tipoIgual(tipo));
        specification = and(specification, CentroCustoSpecification.ativo(ativo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public CentroCustoResponse atualizar(UUID id, CentroCustoRequest request) {
        CentroCusto entity = buscarEntidade(id);
        Empresa empresa = buscarEmpresa(empresaContext.getEmpresaId());
        CentroCusto pai = buscarPai(request.centroCustoPaiId());
        validator.validarAtualizacao(id, request, empresa, pai);
        mapper.aplicar(entity, request, empresa, pai);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    public CentroCusto buscarEntidade(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(
                        id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Centro de custo nao encontrado"));
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private CentroCusto buscarPai(UUID id) {
        return id == null ? null : buscarEntidade(id);
    }

    private Specification<CentroCusto> and(Specification<CentroCusto> base,
            Specification<CentroCusto> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
