package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.CidadeRequest;
import br.com.invplus.modules.cadastros.repository.CidadeRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validacoes de regra de negocio para Cidade.
 */
@Component
@RequiredArgsConstructor
public class CidadeValidator {

    private final CidadeRepository cidadeRepository;

    /**
     * Valida duplicidade de nome dentro do estado.
     */
    public void validarCriacao(CidadeRequest request) {
        if (cidadeRepository.existsByEstadoIdAndNomeAndAtivoTrueAndDeletedAtIsNull(request.estadoId(), request.nome())) {
            throw new ConflictException("Ja existe cidade ativa com este nome para o estado informado");
        }
    }
}
