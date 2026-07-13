package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.ContaReceberRequest;
import br.com.invplus.modules.financeiro.entity.ContaReceber;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.repository.ContaReceberRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras financeiras antes de criar ou alterar contas a receber.
 */
@Component
@RequiredArgsConstructor
public class ContaReceberValidator {

    private final ContaReceberRepository repository;

    public void validarCriacao(ContaReceberRequest request, UUID empresaId) {
        validarNumeroUnico(null, empresaId, request.numero());
        validarValores(request);
    }

    public void validarAtualizacao(UUID id, ContaReceber conta, ContaReceberRequest request, UUID empresaId) {
        conta.validarEditavel();
        validarNumeroUnico(id, empresaId, request.numero());
        validarValores(request);
    }

    public void validarCancelamento(ContaReceber conta) {
        conta.validarEditavel();
    }

    /**
     * Garante que receitas usem um centro da mesma empresa e de tipo compativel.
     */
    public void validarCentroCusto(Empresa empresa, CentroCusto centroCusto) {
        if (centroCusto == null) {
            return;
        }
        if (!centroCusto.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Centro de custo deve pertencer a mesma empresa da conta");
        }
        if (centroCusto.getTipo() == TipoCentroCusto.DESPESA) {
            throw new InvalidOperationException("Conta a receber exige centro de custo RECEITA ou AMBOS");
        }
    }

    /**
     * Garante que contas a receber usem somente planos analiticos de receita.
     */
    public void validarPlanoConta(Empresa empresa, PlanoConta planoConta) {
        if (planoConta == null) {
            return;
        }
        if (!planoConta.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Plano de conta deve pertencer a mesma empresa da conta");
        }
        if (planoConta.getTipo() != TipoPlanoConta.ANALITICA) {
            throw new InvalidOperationException("Conta a receber exige plano de conta ANALITICA");
        }
        if (planoConta.getNatureza() != NaturezaPlanoConta.RECEITA) {
            throw new InvalidOperationException("Conta a receber exige plano de conta de natureza RECEITA");
        }
    }

    private void validarNumeroUnico(UUID idAtual, UUID empresaId, String numero) {
        if (empresaId == null || numero == null || numero.isBlank()) {
            return;
        }
        repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, numero.trim())
                .map(ContaReceber::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe conta a receber ativa com este numero para a empresa");
                });
    }

    private void validarValores(ContaReceberRequest request) {
        if (request.valorTotal() == null || request.valorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor total deve ser maior que zero");
        }
    }
}
