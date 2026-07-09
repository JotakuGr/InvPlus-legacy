package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.PlanoContaRequest;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.repository.PlanoContaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida unicidade e integridade hierarquica do plano de contas.
 */
@Component
@RequiredArgsConstructor
public class PlanoContaValidator {

    private final PlanoContaRepository repository;

    public void validarCriacao(PlanoContaRequest request, Empresa empresa, PlanoConta pai) {
        validarCodigoUnico(null, empresa.getId(), request.codigo());
        validarPaiDaMesmaEmpresa(empresa, pai);
        validarCiclo(null, pai);
    }

    public void validarAtualizacao(UUID id, PlanoContaRequest request, Empresa empresa, PlanoConta pai) {
        validarCodigoUnico(id, empresa.getId(), request.codigo());
        if (request.planoContaPaiId() != null && request.planoContaPaiId().equals(id)) {
            throw new InvalidOperationException("Plano de conta nao pode ser pai dele mesmo");
        }
        validarPaiDaMesmaEmpresa(empresa, pai);
        validarCiclo(id, pai);
    }

    private void validarCodigoUnico(UUID idAtual, UUID empresaId, String codigo) {
        String normalizado = codigo == null ? null : codigo.trim();
        repository.findByEmpresaIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, normalizado)
                .map(PlanoConta::getId)
                .filter(id -> !id.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe plano de conta ativo com este codigo na empresa");
                });
    }

    private void validarPaiDaMesmaEmpresa(Empresa empresa, PlanoConta pai) {
        if (pai != null && !pai.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Plano de conta pai deve pertencer a mesma empresa");
        }
    }

    private void validarCiclo(UUID idAtual, PlanoConta pai) {
        Set<UUID> visitados = new HashSet<>();
        PlanoConta atual = pai;
        while (atual != null) {
            if (!visitados.add(atual.getId())) {
                throw new InvalidOperationException("Ciclo hierarquico detectado no plano de contas");
            }
            if (idAtual != null && idAtual.equals(atual.getId())) {
                throw new InvalidOperationException("Plano de conta nao pode gerar ciclo hierarquico");
            }
            atual = atual.getPlanoContaPai();
        }
    }
}
