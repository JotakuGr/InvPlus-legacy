package br.com.invplus.modules.financeiro.service;

import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.dto.response.ExtratoBancarioResponse;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.enums.OrigemExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.mapper.ExtratoBancarioMapper;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioRepository;
import br.com.invplus.modules.financeiro.specification.ExtratoBancarioSpecification;
import br.com.invplus.modules.financeiro.validator.ExtratoBancarioValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra importacao, consulta e cancelamento historico de extratos bancarios.
 */
@Service
@RequiredArgsConstructor
public class ExtratoBancarioService {

    private final ExtratoBancarioRepository repository;
    private final ContaBancariaService contaBancariaService;
    private final ExtratoBancarioMapper mapper;
    private final ExtratoBancarioValidator validator;

    @Transactional
    public ExtratoBancarioResponse importar(ExtratoBancarioRequest request) {
        ContaBancaria contaBancaria = contaBancariaService.buscarEntidade(request.contaBancariaId());
        validator.validarImportacao(request);

        ExtratoBancario entity = new ExtratoBancario();
        mapper.aplicarImportacao(entity, request, contaBancaria);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExtratoBancarioResponse buscar(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ExtratoBancarioResponse> listar(UUID contaBancariaId, OrigemExtratoBancario origem,
            StatusExtratoBancario status, LocalDate dataInicioDe, LocalDate dataInicioAte,
            LocalDate dataFimDe, LocalDate dataFimAte, Boolean ativo, Pageable pageable) {
        Specification<ExtratoBancario> specification = ExtratoBancarioSpecification.naoDeletado();
        specification = and(specification, ExtratoBancarioSpecification.contaBancariaIgual(contaBancariaId));
        specification = and(specification, ExtratoBancarioSpecification.origemIgual(origem));
        specification = and(specification, ExtratoBancarioSpecification.statusIgual(status));
        specification = and(specification, ExtratoBancarioSpecification.dataInicioMaiorOuIgual(dataInicioDe));
        specification = and(specification, ExtratoBancarioSpecification.dataInicioMenorOuIgual(dataInicioAte));
        specification = and(specification, ExtratoBancarioSpecification.dataFimMaiorOuIgual(dataFimDe));
        specification = and(specification, ExtratoBancarioSpecification.dataFimMenorOuIgual(dataFimAte));
        specification = and(specification, ExtratoBancarioSpecification.ativo(ativo));
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    @Transactional
    public ExtratoBancarioResponse cancelar(UUID id) {
        ExtratoBancario entity = buscarEntidade(id);
        entity.cancelar();
        return mapper.toResponse(entity);
    }

    public ExtratoBancario buscarEntidade(UUID id) {
        return repository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Extrato bancario nao encontrado"));
    }

    private Specification<ExtratoBancario> and(Specification<ExtratoBancario> base,
            Specification<ExtratoBancario> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
