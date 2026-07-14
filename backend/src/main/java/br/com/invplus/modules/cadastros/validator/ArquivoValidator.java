package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Arquivo antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class ArquivoValidator {

    private static final Pattern HASH_SHA256 = Pattern.compile("^[A-Fa-f0-9]{64}$");

    private final ArquivoRepository arquivoRepository;

    /**
     * Garante que um novo arquivo tenha hash valido e nao reutilize hash ativo.
     */
    public void validarCriacao(ArquivoRequest request) {
        validarCampos(request);
        validarHashUnico(null, request.hashSha256());
    }

    /**
     * Garante que uma atualizacao preserve a unicidade do hash ativo.
     */
    public void validarAtualizacao(UUID id, ArquivoRequest request) {
        validarCampos(request);
        validarHashUnico(id, request.hashSha256());
    }

    private void validarCampos(ArquivoRequest request) {
        if (request.tamanhoBytes() == null || request.tamanhoBytes() <= 0) {
            throw new BusinessException("Tamanho do arquivo deve ser maior que zero");
        }
        if (request.hashSha256() == null || !HASH_SHA256.matcher(request.hashSha256().trim()).matches()) {
            throw new BusinessException("Hash SHA-256 invalido");
        }
        if (request.storage() == null) {
            throw new BusinessException("Storage do arquivo e obrigatorio");
        }
        if (request.entidadeId() == null) {
            throw new BusinessException("Identificador da entidade vinculada e obrigatorio");
        }
    }

    private void validarHashUnico(UUID idAtual, String hashSha256) {
        String hashNormalizado = hashSha256.trim().toLowerCase();
        arquivoRepository.findByHashSha256AndAtivoTrueAndDeletedAtIsNull(hashNormalizado)
                .filter(arquivo -> !Objects.equals(arquivo.getId(), idAtual))
                .ifPresent(arquivo -> {
                    throw new ConflictException("Ja existe arquivo ativo com este hash SHA-256");
                });
    }
}
