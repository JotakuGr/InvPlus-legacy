package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaBancariaResponse;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.enums.TipoContaBancaria;
import br.com.invplus.modules.financeiro.mapper.ContaBancariaMapper;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.modules.financeiro.specification.ContaBancariaSpecification;
import br.com.invplus.modules.financeiro.validator.ContaBancariaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.invplus.security.context.EmpresaContext;
import br.com.invplus.security.exception.AcessoEmpresaNegadoException;

/**
 * Orquestra o cadastro de contas bancarias e caixas internos por empresa.
 */
@Service
@RequiredArgsConstructor
public class ContaBancariaService {

    private final ContaBancariaRepository repository;
    private final EmpresaRepository empresaRepository;
    private final FilialRepository filialRepository;
    private final ContaBancariaMapper mapper;
    private final ContaBancariaValidator validator;

    private final EmpresaContext empresaContext;

    @Transactional
    public ContaBancariaResponse criar(ContaBancariaRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        Empresa empresa = buscarEmpresa(empresaId);
        Filial filial = buscarFilial(request.filialId());
        validator.validarCriacao(request, empresa, filial);

        ContaBancaria entity = new ContaBancaria();
        mapper.aplicar(entity, request, empresa, filial);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ContaBancariaResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ContaBancariaResponse> listar(UUID filialId, String bancoCodigo,
            String bancoNome, TipoContaBancaria tipoConta, Boolean principal, Boolean ativo, Pageable pageable) {
        UUID empresaId = empresaContext.getEmpresaId();
        Specification<ContaBancaria> specification = ContaBancariaSpecification.naoDeletado();
        specification = and(specification, ContaBancariaSpecification.pertenceEmpresa(empresaId));
        specification = and(specification, ContaBancariaSpecification.filialIgual(filialId));
        specification = and(specification, ContaBancariaSpecification.bancoCodigoContem(bancoCodigo));
        specification = and(specification, ContaBancariaSpecification.bancoNomeContem(bancoNome));
        specification = and(specification, ContaBancariaSpecification.tipoIgual(tipoConta));
        specification = and(specification, ContaBancariaSpecification.principalIgual(principal));
        specification = and(specification, ContaBancariaSpecification.ativo(ativo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }
    @Transactional
    public ContaBancariaResponse atualizar(UUID id, ContaBancariaRequest request) {
        ContaBancaria entity = buscarEntidade(id);
        UUID empresaId = empresaContext.getEmpresaId();
        Empresa empresa = buscarEmpresa(empresaId);
        Filial filial = buscarFilial(request.filialId());
        validator.validarAtualizacao(id, request, empresa, filial);
        mapper.aplicar(entity, request, empresa, filial);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    public ContaBancaria buscarEntidade(UUID id) {
        return repository.findByIdAndEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaContext.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancaria nao encontrada"));
    }

    private Empresa buscarEmpresa(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }

    private Filial buscarFilial(UUID id) {
        return id == null ? null : filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }

    private Specification<ContaBancaria> and(Specification<ContaBancaria> base,
            Specification<ContaBancaria> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
