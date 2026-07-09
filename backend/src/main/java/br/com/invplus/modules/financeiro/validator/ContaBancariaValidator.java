package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.repository.ContaBancariaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida unicidade, vinculos e regras operacionais de contas bancarias.
 */
@Component
@RequiredArgsConstructor
public class ContaBancariaValidator {

    private final ContaBancariaRepository repository;

    public void validarCriacao(ContaBancariaRequest request, Empresa empresa, Filial filial) {
        validarBasico(request, empresa, filial);
        validarContaUnica(null, empresa.getId(), request);
        validarPrincipalUnico(null, empresa.getId(), request.principal());
    }

    public void validarAtualizacao(UUID id, ContaBancariaRequest request, Empresa empresa, Filial filial) {
        validarBasico(request, empresa, filial);
        validarContaUnica(id, empresa.getId(), request);
        validarPrincipalUnico(id, empresa.getId(), request.principal());
    }

    private void validarBasico(ContaBancariaRequest request, Empresa empresa, Filial filial) {
        validarAtivo(empresa.getAtivo(), empresa.getDeletedAt(), "Empresa");
        if (filial != null) {
            validarAtivo(filial.getAtivo(), filial.getDeletedAt(), "Filial");
            if (!filial.getEmpresa().getId().equals(empresa.getId())) {
                throw new InvalidOperationException("Filial deve pertencer a empresa informada");
            }
        }
        BigDecimal saldo = request.saldoInicial() == null ? BigDecimal.ZERO : request.saldoInicial();
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Saldo inicial nao pode ser negativo");
        }
        validarDocumentoTitular(request.documentoTitular());
    }

    private void validarContaUnica(UUID idAtual, UUID empresaId, ContaBancariaRequest request) {
        repository.findByEmpresaIdAndBancoCodigoIgnoreCaseAndAgenciaIgnoreCaseAndNumeroContaIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(
                        empresaId, normalizar(request.bancoCodigo()), normalizar(request.agencia()),
                        normalizar(request.numeroConta()))
                .map(ContaBancaria::getId)
                .filter(id -> !id.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe conta bancaria ativa com estes dados na empresa");
                });
    }

    private void validarPrincipalUnico(UUID idAtual, UUID empresaId, Boolean principal) {
        if (!Boolean.TRUE.equals(principal)) {
            return;
        }
        repository.findByEmpresaIdAndPrincipalTrueAndAtivoTrueAndDeletedAtIsNull(empresaId)
                .map(ContaBancaria::getId)
                .filter(id -> !id.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe conta bancaria principal ativa para esta empresa");
                });
    }

    private void validarDocumentoTitular(String documento) {
        if (documento == null || documento.isBlank()) {
            return;
        }
        String numeros = documento.replaceAll("\\D", "");
        if (numeros.length() != 11 && numeros.length() != 14) {
            throw new InvalidOperationException("Documento do titular deve possuir 11 ou 14 digitos");
        }
    }

    private void validarAtivo(Boolean ativo, Object deletedAt, String recurso) {
        if (!Boolean.TRUE.equals(ativo) || deletedAt != null) {
            throw new InvalidOperationException(recurso + " inativa nao pode ser usada na conta bancaria");
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }
}
