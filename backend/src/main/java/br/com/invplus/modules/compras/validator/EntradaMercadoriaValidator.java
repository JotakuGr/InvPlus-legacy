package br.com.invplus.modules.compras.validator;

import br.com.invplus.modules.compras.dto.request.EntradaMercadoriaRequest;
import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Regras da entrada de mercadoria.
 */
@Component
public class EntradaMercadoriaValidator {

    private final EntradaMercadoriaRepository repository;

    public EntradaMercadoriaValidator(EntradaMercadoriaRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(EntradaMercadoriaRequest request, UUID empresaId) {
        validarNumeroUnico(empresaId, request.numero(), null);
    }

    public void validarAtualizacao(EntradaMercadoria entity, EntradaMercadoriaRequest request, UUID empresaId) {
        entity.validarEditavel();
        validarNumeroUnico(empresaId, request.numero(), entity.getId());
    }

    private void validarNumeroUnico(UUID empresaId, String numero, UUID idAtual) {
        repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, numero)
                .filter(existente -> idAtual == null || !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ConflictException("Ja existe entrada de mercadoria ativa com este numero para a empresa");
                });
    }
}
