package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.MarcaRequest;
import br.com.invplus.modules.cadastros.repository.MarcaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras funcionais de Marca antes da persistencia.
 */
@Component
@RequiredArgsConstructor
public class MarcaValidator {

    private final MarcaRepository marcaRepository;

    /**
     * Garante que uma nova marca nao reutilize nome ativo.
     */
    public void validarCriacao(MarcaRequest request) {
        validarNomeUnico(null, request.nome());
    }

    /**
     * Garante que a atualizacao nao gere duplicidade de nome ativo.
     */
    public void validarAtualizacao(UUID id, MarcaRequest request) {
        validarNomeUnico(id, request.nome());
    }

    private void validarNomeUnico(UUID idAtual, String nome) {
        String nomeNormalizado = nome == null ? null : nome.trim();
        marcaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(nomeNormalizado)
                .filter(marca -> !Objects.equals(marca.getId(), idAtual))
                .ifPresent(marca -> {
                    throw new ConflictException("Ja existe marca ativa com este nome");
                });
    }
}
