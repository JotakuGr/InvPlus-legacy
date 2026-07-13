package br.com.invplus.modules.estoque.validator;

import br.com.invplus.modules.estoque.dto.request.DepositoRequest;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.repository.DepositoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras de cadastro e operacao de Deposito.
 */
@Component
public class DepositoValidator {

    private final DepositoRepository repository;

    public DepositoValidator(DepositoRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(UUID empresaId, DepositoRequest request) {
        validarCampos(request);
        validarCodigoUnico(empresaId, request, null);
        validarPrincipalUnico(empresaId, request, null);
    }

    public void validarAtualizacao(UUID empresaId, Deposito atual, DepositoRequest request) {
        validarCampos(request);
        validarCodigoUnico(empresaId, request, atual.getId());
        validarPrincipalUnico(empresaId, request, atual.getId());
    }

    private void validarCampos(DepositoRequest request) {
        if (textoVazio(request.codigo())) {
            throw new BusinessException("Codigo e obrigatorio");
        }
        if (textoVazio(request.nome())) {
            throw new BusinessException("Nome e obrigatorio");
        }
        if (request.tipoDeposito() == null) {
            throw new BusinessException("Tipo de deposito e obrigatorio");
        }
    }

    private void validarCodigoUnico(UUID empresaId, DepositoRequest request, UUID idAtual) {
        repository.findByEmpresaIdAndCodigoAndAtivoTrueAndDeletedAtIsNull(empresaId, normalizarCodigo(request.codigo()))
                .map(Deposito::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Ja existe deposito ativo com este codigo para a empresa");
                });
    }

    private void validarPrincipalUnico(UUID empresaId, DepositoRequest request, UUID idAtual) {
        if (!Boolean.TRUE.equals(request.principal())) {
            return;
        }
        repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresaId)
                .map(Deposito::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Empresa ja possui deposito principal ativo");
                });
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase();
    }

    private boolean textoVazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
