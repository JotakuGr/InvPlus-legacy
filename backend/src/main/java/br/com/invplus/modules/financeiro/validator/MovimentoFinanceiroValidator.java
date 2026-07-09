package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.dto.request.MovimentoFinanceiroRequest;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.entity.CentroCusto;
import br.com.invplus.modules.financeiro.entity.PlanoConta;
import br.com.invplus.modules.financeiro.enums.NaturezaPlanoConta;
import br.com.invplus.modules.financeiro.enums.OrigemMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoCentroCusto;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoPlanoConta;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.pagamentos.entity.FormaPagamento;
import br.com.invplus.modules.pessoas.entity.Cliente;
import br.com.invplus.modules.pessoas.entity.Fornecedor;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Protege a coerencia de movimentos manuais e seu fluxo de cancelamento.
 */
@Component
public class MovimentoFinanceiroValidator {

    public void validarManual(MovimentoFinanceiroRequest request, Empresa empresa, Filial filial,
            FormaPagamento formaPagamento, Cliente cliente, Fornecedor fornecedor,
            CentroCusto centroCusto, PlanoConta planoConta) {
        validarAtivo(empresa.getAtivo(), empresa.getDeletedAt(), "Empresa");
        validarAtivo(formaPagamento.getAtivo(), formaPagamento.getDeletedAt(), "Forma de pagamento");
        if (filial != null) {
            validarAtivo(filial.getAtivo(), filial.getDeletedAt(), "Filial");
            if (!filial.getEmpresa().getId().equals(empresa.getId())) {
                throw new InvalidOperationException("Filial deve pertencer a empresa informada");
            }
        }
        if (request.origem() == OrigemMovimentoFinanceiro.RECEBIMENTO
                || request.origem() == OrigemMovimentoFinanceiro.PAGAMENTO) {
            throw new InvalidOperationException("Origem RECEBIMENTO ou PAGAMENTO deve ser registrada pelo service");
        }
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Valor do movimento deve ser maior que zero");
        }
        if (request.dataMovimento() == null) {
            throw new InvalidOperationException("Data do movimento e obrigatoria");
        }
        validarContraparte(request.tipoMovimento(), cliente, fornecedor);
        validarCentroCusto(empresa, request.tipoMovimento(), centroCusto);
        validarPlanoConta(empresa, request.tipoMovimento(), planoConta);
    }

    public void validarCancelamento(MovimentoFinanceiro movimento) {
        if (movimento.getStatus() == StatusMovimentoFinanceiro.CANCELADO) {
            throw new InvalidOperationException("Movimento financeiro ja esta cancelado");
        }
    }

    /**
     * Valida empresa e natureza contabil do centro usado no movimento.
     */
    public void validarCentroCusto(Empresa empresa, TipoMovimentoFinanceiro tipo, CentroCusto centroCusto) {
        if (centroCusto == null) {
            return;
        }
        if (!centroCusto.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Centro de custo deve pertencer a mesma empresa do movimento");
        }
        if (tipo == TipoMovimentoFinanceiro.ENTRADA && centroCusto.getTipo() == TipoCentroCusto.DESPESA) {
            throw new InvalidOperationException("Movimento de entrada exige centro de custo RECEITA ou AMBOS");
        }
        if (tipo == TipoMovimentoFinanceiro.SAIDA && centroCusto.getTipo() == TipoCentroCusto.RECEITA) {
            throw new InvalidOperationException("Movimento de saida exige centro de custo DESPESA ou AMBOS");
        }
    }

    /**
     * Valida empresa, tipo analitico e natureza contabil do plano usado no movimento.
     */
    public void validarPlanoConta(Empresa empresa, TipoMovimentoFinanceiro tipo, PlanoConta planoConta) {
        if (planoConta == null) {
            return;
        }
        if (!planoConta.getEmpresa().getId().equals(empresa.getId())) {
            throw new InvalidOperationException("Plano de conta deve pertencer a mesma empresa do movimento");
        }
        if (planoConta.getTipo() != TipoPlanoConta.ANALITICA) {
            throw new InvalidOperationException("Movimento financeiro exige plano de conta ANALITICA");
        }
        if (tipo == TipoMovimentoFinanceiro.ENTRADA && planoConta.getNatureza() != NaturezaPlanoConta.RECEITA) {
            throw new InvalidOperationException("Movimento de entrada exige plano de conta de natureza RECEITA");
        }
        if (tipo == TipoMovimentoFinanceiro.SAIDA && planoConta.getNatureza() != NaturezaPlanoConta.DESPESA) {
            throw new InvalidOperationException("Movimento de saida exige plano de conta de natureza DESPESA");
        }
    }

    private void validarContraparte(TipoMovimentoFinanceiro tipo, Cliente cliente, Fornecedor fornecedor) {
        if (tipo == TipoMovimentoFinanceiro.ENTRADA && fornecedor != null) {
            throw new InvalidOperationException("Movimento de entrada nao permite fornecedor");
        }
        if (tipo == TipoMovimentoFinanceiro.SAIDA && cliente != null) {
            throw new InvalidOperationException("Movimento de saida nao permite cliente");
        }
        if (cliente != null) {
            validarAtivo(cliente.getAtivo(), cliente.getDeletedAt(), "Cliente");
        }
        if (fornecedor != null) {
            validarAtivo(fornecedor.getAtivo(), fornecedor.getDeletedAt(), "Fornecedor");
        }
    }

    private void validarAtivo(Boolean ativo, Object deletedAt, String recurso) {
        if (!Boolean.TRUE.equals(ativo) || deletedAt != null) {
            throw new InvalidOperationException(recurso + " inativo nao pode ser usado no movimento");
        }
    }
}
