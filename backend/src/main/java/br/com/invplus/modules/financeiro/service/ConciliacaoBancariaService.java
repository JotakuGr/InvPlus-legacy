package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaCancelamentoRequest;
import br.com.invplus.modules.financeiro.dto.request.ConciliacaoBancariaRequest;
import br.com.invplus.modules.financeiro.dto.response.ConciliacaoBancariaResponse;
import br.com.invplus.modules.financeiro.entity.ConciliacaoBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.TipoConciliacaoBancaria;
import br.com.invplus.modules.financeiro.mapper.ConciliacaoBancariaMapper;
import br.com.invplus.modules.financeiro.repository.ConciliacaoBancariaRepository;
import br.com.invplus.modules.financeiro.specification.ConciliacaoBancariaSpecification;
import br.com.invplus.modules.financeiro.validator.ConciliacaoBancariaValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.invplus.security.context.EmpresaContext;

/** Orquestra conciliacao e cancelamento como operacoes financeiras atomicas. */
@Service
public class ConciliacaoBancariaService {

    private final ConciliacaoBancariaRepository repository;
    private final ExtratoBancarioLancamentoService lancamentoService;
    private final ExtratoBancarioService extratoService;
    private final MovimentoFinanceiroService movimentoService;
    private final ConciliacaoBancariaMapper mapper;
    private final ConciliacaoBancariaValidator validator;
    private final AuditorAware<UUID> auditorAware;
    private final EmpresaContext empresaContext;

    public ConciliacaoBancariaService(ConciliacaoBancariaRepository repository,
            ExtratoBancarioLancamentoService lancamentoService, ExtratoBancarioService extratoService,
            MovimentoFinanceiroService movimentoService, ConciliacaoBancariaMapper mapper,
            ConciliacaoBancariaValidator validator, AuditorAware<UUID> auditorAware,
            EmpresaContext empresaContext) {
        this.repository = repository;
        this.lancamentoService = lancamentoService;
        this.extratoService = extratoService;
        this.movimentoService = movimentoService;
        this.mapper = mapper;
        this.validator = validator;
        this.auditorAware = auditorAware;
        this.empresaContext = empresaContext;
    }

    /** Concilia exatamente uma linha bancaria com um movimento ativo. */
    @Transactional
    public ConciliacaoBancariaResponse criar(ConciliacaoBancariaRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        ExtratoBancarioLancamento lancamento =
                lancamentoService.buscarEntidade(request.extratoBancarioLancamentoId(), empresaId);
        MovimentoFinanceiro movimento =
                movimentoService.buscarEntidade(request.movimentoFinanceiroId(), empresaId);
        validator.validar(lancamento, movimento);

        ConciliacaoBancaria entity = new ConciliacaoBancaria();
        entity.conciliar(lancamento, movimento, request.tipoConciliacao(), usuarioAtual(), request.observacao());
        repository.saveAndFlush(entity);
        lancamentoService.marcarComoConciliado(lancamento.getId());
        extratoService.recalcularStatusConciliacao(lancamento.getExtratoBancario().getId());
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ConciliacaoBancariaResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ConciliacaoBancariaResponse> listar(UUID lancamentoId, UUID movimentoId, UUID contaBancariaId,
            TipoConciliacaoBancaria tipo, StatusConciliacaoBancaria status,
            OffsetDateTime conciliadoEmDe, OffsetDateTime conciliadoEmAte, Pageable pageable) {
        Specification<ConciliacaoBancaria> specification = (root, query, cb) -> cb.conjunction();
        UUID empresaId = empresaContext.getEmpresaId();
        specification = and(specification, ConciliacaoBancariaSpecification.pertenceEmpresa(empresaId));
        specification = and(specification, ConciliacaoBancariaSpecification.lancamentoIgual(lancamentoId));
        specification = and(specification, ConciliacaoBancariaSpecification.movimentoIgual(movimentoId));
        specification = and(specification, ConciliacaoBancariaSpecification.contaIgual(contaBancariaId));
        specification = and(specification, ConciliacaoBancariaSpecification.tipoIgual(tipo));
        specification = and(specification, ConciliacaoBancariaSpecification.statusIgual(status));
        specification = and(specification, ConciliacaoBancariaSpecification.conciliadoEmDe(conciliadoEmDe));
        specification = and(specification, ConciliacaoBancariaSpecification.conciliadoEmAte(conciliadoEmAte));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    /** Cancela a conciliacao e restaura os estados derivados sem remover historico. */
    @Transactional
    public ConciliacaoBancariaResponse cancelar(UUID id, ConciliacaoBancariaCancelamentoRequest request) {
        UUID empresaId = empresaContext.getEmpresaId();
        ConciliacaoBancaria entity = repository.findByIdAndStatusAndContaBancariaEmpresaId(
                        id, StatusConciliacaoBancaria.CONCILIADA, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conciliacao bancaria ativa nao encontrada"));
        entity.cancelar(request.motivoCancelamento(), usuarioAtual());
        repository.saveAndFlush(entity);

        ExtratoBancarioLancamento lancamento = entity.getExtratoBancarioLancamento();
        lancamentoService.desmarcarComoConciliado(lancamento.getId());
        extratoService.recalcularStatusConciliacao(lancamento.getExtratoBancario().getId());
        return mapper.toResponse(entity);
    }

    private ConciliacaoBancaria buscarEntidade(UUID id) {
        UUID empresaId = empresaContext.getEmpresaId();
        return repository.findByIdAndContaBancariaEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conciliacao bancaria nao encontrada"));
    }

    private UUID usuarioAtual() {
        return auditorAware.getCurrentAuditor().orElse(null);
    }

    private Specification<ConciliacaoBancaria> and(Specification<ConciliacaoBancaria> base,
            Specification<ConciliacaoBancaria> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
