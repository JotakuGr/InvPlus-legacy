package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.ExtratoBancarioRequest;
import br.com.invplus.modules.financeiro.repository.ExtratoBancarioRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centraliza regras de importacao, hash e cancelamento de extratos.
 */
@Component
@RequiredArgsConstructor
public class ExtratoBancarioValidator {

    private static final String SHA_256_REGEX = "^[A-Fa-f0-9]{64}$";

    private final ExtratoBancarioRepository repository;

    /**
     * Valida campos de negocio antes da importacao do extrato.
     */
    public void validarImportacao(ExtratoBancarioRequest request) {
        if (request.dataInicio().isAfter(request.dataFim())) {
            throw new InvalidOperationException("Data inicial do extrato nao pode ser maior que a data final");
        }
        validarHash(request.arquivoHash());
        validarHashUnico(request.arquivoHash());
    }

    private void validarHash(String arquivoHash) {
        if (arquivoHash == null || arquivoHash.isBlank()) {
            return;
        }
        if (!arquivoHash.trim().matches(SHA_256_REGEX)) {
            throw new InvalidOperationException("Hash do arquivo deve ser um SHA-256 valido");
        }
    }

    private void validarHashUnico(String arquivoHash) {
        if (arquivoHash == null || arquivoHash.isBlank()) {
            return;
        }
        repository.findByArquivoHashIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(arquivoHash.trim())
                .ifPresent(extrato -> {
                    throw new ConflictException("Ja existe extrato bancario ativo com este hash de arquivo");
                });
    }
}
