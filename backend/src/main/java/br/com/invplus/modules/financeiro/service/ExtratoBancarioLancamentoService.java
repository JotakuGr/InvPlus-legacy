package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioLancamentoRequest;
import br.com.invplus.modules.financeiro.dto.response.ExtratoBancarioLancamentoResponse;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.enums.CategoriaLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.mapper.ExtratoBancarioLancamentoMapper;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioLancamentoRepository;
import br.com.invplus.modules.financeiro.specification.ExtratoBancarioLancamentoSpecification;
import br.com.invplus.modules.financeiro.validator.ExtratoBancarioLancamentoValidator;
import br.com.invplus.modules.financeiro.importacao.parser.dto.LancamentoImportado;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.invplus.security.context.EmpresaContext;

/**
 * Orquestra CRUD e marcacao de conciliacao dos lancamentos de extrato.
 */
@Service
@RequiredArgsConstructor
public class ExtratoBancarioLancamentoService {

    private final ExtratoBancarioLancamentoRepository repository;
    private final ExtratoBancarioService extratoBancarioService;
    private final ExtratoBancarioLancamentoMapper mapper;
    private final ExtratoBancarioLancamentoValidator validator;
    private final EmpresaContext empresaContext;

    @Transactional
    public ExtratoBancarioLancamentoResponse criar(ExtratoBancarioLancamentoRequest request) {
        ExtratoBancario extrato = extratoBancarioService.buscarEntidade(request.extratoBancarioId());
        validator.validarCriacao(request, extrato);

        ExtratoBancarioLancamento entity = new ExtratoBancarioLancamento();
        mapper.aplicar(entity, request, extrato);
        return mapper.toResponse(repository.save(entity));
    }

    /** Converte uma linha intermediaria OFX para o dominio bancario existente. */
    @Transactional
    public ExtratoBancarioLancamento criarPorImportacao(ExtratoBancario extrato, LancamentoImportado dados) {
        TipoLancamentoBancario tipo = dados.valor().signum() >= 0
                ? TipoLancamentoBancario.CREDITO : TipoLancamentoBancario.DEBITO;
        ExtratoBancarioLancamentoRequest request = new ExtratoBancarioLancamentoRequest(
                extrato.getId(), dados.dataLancamento(), dados.descricao(), dados.documento(), tipo,
                dados.valor().abs(), dados.saldoAposLancamento(), dados.identificadorBancario(),
                categoria(dados.tipoOriginal()), null);
        validator.validarCriacao(request, extrato);
        ExtratoBancarioLancamento entity = new ExtratoBancarioLancamento();
        mapper.aplicar(entity, request, extrato);
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public ExtratoBancarioLancamentoResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ExtratoBancarioLancamentoResponse> listar(UUID extratoBancarioId, UUID contaBancariaId,
            TipoLancamentoBancario tipo, CategoriaLancamentoBancario categoriaBancaria, Boolean conciliado,
            LocalDate dataLancamentoDe, LocalDate dataLancamentoAte, Boolean ativo, Pageable pageable) {
        Specification<ExtratoBancarioLancamento> specification = ExtratoBancarioLancamentoSpecification.naoDeletado();
        UUID empresaId = empresaAtualOuNull();
        if (empresaId != null) specification = and(specification,
                ExtratoBancarioLancamentoSpecification.pertenceEmpresa(empresaId));
        specification = and(specification, ExtratoBancarioLancamentoSpecification.extratoIgual(extratoBancarioId));
        specification = and(specification, ExtratoBancarioLancamentoSpecification.contaBancariaIgual(contaBancariaId));
        specification = and(specification, ExtratoBancarioLancamentoSpecification.tipoIgual(tipo));
        specification = and(specification, ExtratoBancarioLancamentoSpecification.categoriaIgual(categoriaBancaria));
        specification = and(specification, ExtratoBancarioLancamentoSpecification.conciliadoIgual(conciliado));
        specification = and(specification,
                ExtratoBancarioLancamentoSpecification.dataLancamentoMaiorOuIgual(dataLancamentoDe));
        specification = and(specification,
                ExtratoBancarioLancamentoSpecification.dataLancamentoMenorOuIgual(dataLancamentoAte));
        specification = and(specification, ExtratoBancarioLancamentoSpecification.ativo(ativo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public ExtratoBancarioLancamentoResponse atualizar(UUID id, ExtratoBancarioLancamentoRequest request) {
        ExtratoBancarioLancamento entity = buscarEntidade(id);
        ExtratoBancario extrato = extratoBancarioService.buscarEntidade(request.extratoBancarioId());
        validator.validarAtualizacao(id, request, extrato, entity);
        mapper.aplicar(entity, request, extrato);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void excluir(UUID id) {
        ExtratoBancarioLancamento entity = buscarEntidade(id);
        validator.validarExclusao(entity);
        entity.excluirLogicamente();
    }

    @Transactional
    public ExtratoBancarioLancamentoResponse marcarComoConciliado(UUID id) {
        ExtratoBancarioLancamento entity = buscarEntidade(id);
        entity.marcarComoConciliado();
        return mapper.toResponse(entity);
    }

    @Transactional
    public ExtratoBancarioLancamentoResponse desmarcarComoConciliado(UUID id) {
        ExtratoBancarioLancamento entity = buscarEntidade(id);
        entity.desmarcarComoConciliado();
        return mapper.toResponse(entity);
    }

    public ExtratoBancarioLancamento buscarEntidade(UUID id) {
        if (empresaContext != null) {
            UUID empresaId = empresaAtualOuNull();
            if (empresaId != null) return repository
                    .findByIdAndExtratoBancarioContaBancariaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lancamento de extrato bancario nao encontrado"));
        }
        return repository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lancamento de extrato bancario nao encontrado"));
    }

    private UUID empresaAtualOuNull() {
        if (empresaContext == null) return null;
        try { return empresaContext.getEmpresaId(); }
        catch (br.com.invplus.security.exception.ContextoEmpresarialAusenteException
                | org.springframework.security.authentication.AuthenticationCredentialsNotFoundException ex) { return null; }
    }

    public ExtratoBancarioLancamento buscarEntidade(UUID id, UUID empresaId) {
        return repository.findByIdAndExtratoBancarioContaBancariaEmpresaIdAndAtivoTrueAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lancamento de extrato bancario nao encontrado"));
    }

    private Specification<ExtratoBancarioLancamento> and(Specification<ExtratoBancarioLancamento> base,
            Specification<ExtratoBancarioLancamento> filtro) {
        return filtro == null ? base : base.and(filtro);
    }

    private CategoriaLancamentoBancario categoria(String tipoOriginal) {
        if (tipoOriginal == null) {
            return CategoriaLancamentoBancario.OUTRO;
        }
        return switch (tipoOriginal.trim().toUpperCase()) {
            case "FEE", "SRVCHG" -> CategoriaLancamentoBancario.TARIFA;
            case "INT" -> CategoriaLancamentoBancario.JUROS;
            case "XFER" -> CategoriaLancamentoBancario.TRANSFERENCIA;
            default -> CategoriaLancamentoBancario.OUTRO;
        };
    }
}
