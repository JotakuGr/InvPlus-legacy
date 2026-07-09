package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.dto.request.ContaPagarRequest;
import br.com.invplus.modules.financeiro.entity.ContaPagar;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.financeiro.repository.ContaPagarRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida unicidade, valores e transicoes das contas a pagar.
 */
@Component
@RequiredArgsConstructor
public class ContaPagarValidator {

    private final ContaPagarRepository repository;

    public void validarCriacao(ContaPagarRequest request) {
        validarNumeroUnico(null, request.empresaId(), request.numero());
        validarValores(request);
    }

    public void validarAtualizacao(UUID id, ContaPagar conta, ContaPagarRequest request) {
        conta.validarEditavel();
        validarNumeroUnico(id, request.empresaId(), request.numero());
        validarValores(request);
    }

    public void validarCancelamento(ContaPagar conta) {
        conta.validarEditavel();
    }

    /**
     * Garante que despesas usem um centro da mesma empresa e de tipo compativel.
     */
    public void validarCentroCusto(Empresa empresa, CentroCusto centroCusto) {
        if (centroCusto == null) {
            return;
        }
        if (!centroCusto.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Centro de custo deve pertencer a mesma empresa da conta");
        }
        if (centroCusto.getTipo() == TipoCentroCusto.RECEITA) {
            throw new InvalidOperationException("Conta a pagar exige centro de custo DESPESA ou AMBOS");
        }
    }

    /**
     * Garante que contas a pagar usem somente planos analiticos de despesa.
     */
    public void validarPlanoConta(Empresa empresa, PlanoConta planoConta) {
        if (planoConta == null) {
            return;
        }
        if (!planoConta.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Plano de conta deve pertencer a mesma empresa da conta");
        }
        if (planoConta.getTipo() != TipoPlanoConta.ANALITICA) {
            throw new InvalidOperationException("Conta a pagar exige plano de conta ANALITICA");
        }
        if (planoConta.getNatureza() != NaturezaPlanoConta.DESPESA) {
            throw new InvalidOperationException("Conta a pagar exige plano de conta de natureza DESPESA");
        }
    }

    private void validarNumeroUnico(UUID idAtual, UUID empresaId, String numero) {
        if (empresaId == null || numero == null || numero.isBlank()) {
            return;
        }
        repository.findByEmpresaIdAndNumeroIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(empresaId, numero.trim())
                .map(ContaPagar::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new ConflictException("Ja existe conta a pagar ativa com este numero para a empresa");
                });
    }

    private void validarValores(ContaPagarRequest request) {
        if (request.valorTotal() == null || request.valorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor total deve ser maior que zero");
        }
    }
}
