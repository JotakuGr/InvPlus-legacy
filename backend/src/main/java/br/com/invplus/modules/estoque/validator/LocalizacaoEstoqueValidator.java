package br.com.invplus.modules.estoque.validator;

import br.com.invplus.modules.estoque.dto.request.LocalizacaoEstoqueRequest;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.repository.LocalizacaoEstoqueRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras de localizacao interna do deposito.
 */
@Component
public class LocalizacaoEstoqueValidator {

    private final LocalizacaoEstoqueRepository repository;

    public LocalizacaoEstoqueValidator(LocalizacaoEstoqueRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(LocalizacaoEstoqueRequest request) {
        validarCampos(request);
        validarCodigoUnico(request, null);
        validarPrincipalUnico(request, null);
    }

    public void validarAtualizacao(LocalizacaoEstoque atual, LocalizacaoEstoqueRequest request) {
        validarCampos(request);
        validarCodigoUnico(request, atual.getId());
        validarPrincipalUnico(request, atual.getId());
    }

    private void validarCampos(LocalizacaoEstoqueRequest request) {
        if (request.depositoId() == null) {
            throw new BusinessException("Deposito e obrigatorio");
        }
        if (request.codigo() == null || request.codigo().isBlank()) {
            throw new BusinessException("Codigo e obrigatorio");
        }
    }

    private void validarCodigoUnico(LocalizacaoEstoqueRequest request, UUID idAtual) {
        repository.findByDepositoIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(request.depositoId(), normalizarCodigo(request.codigo()))
                .map(LocalizacaoEstoque::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Ja existe localizacao ativa com este codigo para o deposito");
                });
    }

    private void validarPrincipalUnico(LocalizacaoEstoqueRequest request, UUID idAtual) {
        if (!Boolean.TRUE.equals(request.principal())) {
            return;
        }
        repository.findByDepositoIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(request.depositoId())
                .map(LocalizacaoEstoque::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Deposito ja possui localizacao principal ativa");
                });
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase();
    }
}
