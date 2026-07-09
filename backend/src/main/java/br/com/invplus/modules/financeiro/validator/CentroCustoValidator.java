package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.dto.request.CentroCustoRequest;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.repository.CentroCustoRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida unicidade por empresa e integridade da arvore de centros de custo.
 */
@Component
@RequiredArgsConstructor
public class CentroCustoValidator {

    private final CentroCustoRepository repository;

    public void validarCriacao(CentroCustoRequest request, Empresa empresa, CentroCusto pai) {
        validarCodigoUnico(null, empresa.getId(), request.codigo());
        validarPaiDaMesmaEmpresa(empresa, pai);
        validarCiclo(null, pai);
    }

    public void validarAtualizacao(UUID id, CentroCustoRequest request, Empresa empresa, CentroCusto pai) {
        validarCodigoUnico(id, empresa.getId(), request.codigo());
        if (request.centroCustoPaiId() != null && request.centroCustoPaiId().equals(id)) {
            throw new InvalidOperationException("Centro de custo nao pode ser pai dele mesmo");
        }
        validarPaiDaMesmaEmpresa(empresa, pai);
        validarCiclo(id, pai);
    }

    private void validarCodigoUnico(UUID idAtual, UUID empresaId, String codigo) {
        String normalizado = codigo == null ? null : codigo.trim();
        repository.findByEmpresaIdAndCodigoIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, normalizado)
                .map(CentroCusto::getId)
                .filter(id -> !id.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe centro de custo ativo com este codigo na empresa");
                });
    }

    private void validarPaiDaMesmaEmpresa(Empresa empresa, CentroCusto pai) {
        if (pai != null && !pai.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Centro de custo pai deve pertencer a mesma empresa");
        }
    }

    private void validarCiclo(UUID idAtual, CentroCusto pai) {
        Set<UUID> visitados = new HashSet<>();
        CentroCusto atual = pai;
        while (atual != null) {
            if (!visitados.add(atual.getId())) {
                throw new InvalidOperationException("Ciclo hierarquico detectado em centro de custo");
            }
            if (idAtual != null && idAtual.equals(atual.getId())) {
                throw new InvalidOperationException("Centro de custo nao pode gerar ciclo hierarquico");
            }
            atual = atual.getCentroCustoPai();
        }
    }
}
