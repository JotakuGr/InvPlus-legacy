package br.com.invplus.modules.financeiro.validator;

import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.StatusConciliacaoBancaria;
import br.com.invplus.modules.financeiro.enums.StatusExtratoBancario;
import br.com.invplus.modules.financeiro.enums.StatusMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.enums.TipoLancamentoBancario;
import br.com.invplus.modules.financeiro.enums.TipoMovimentoFinanceiro;
import br.com.invplus.modules.financeiro.repository.ConciliacaoBancariaRepository;
import br.com.invplus.shared.domain.exception.ConflictException;
import br.com.invplus.shared.domain.exception.InvalidOperationException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Reune as regras de compatibilidade e unicidade da conciliacao. */
@Component
@RequiredArgsConstructor
public class ConciliacaoBancariaValidator {

    private final ConciliacaoBancariaRepository repository;

    public void validar(ExtratoBancarioLancamento lancamento, MovimentoFinanceiro movimento) {
        if (!Boolean.TRUE.equals(lancamento.getAtivo()) || lancamento.getDeletedAt() != null) {
            throw new InvalidOperationException("Lancamento bancario deve estar ativo");
        }
        if (!Boolean.TRUE.equals(lancamento.getExtratoBancario().getAtivo())
                || lancamento.getExtratoBancario().getDeletedAt() != null) {
            throw new InvalidOperationException("Extrato bancario deve estar ativo");
        }
        if (lancamento.getExtratoBancario().getStatus() == StatusExtratoBancario.CANCELADO) {
            throw new InvalidOperationException("Extrato bancario cancelado nao pode ser conciliado");
        }
        if (!Boolean.TRUE.equals(lancamento.getExtratoBancario().getContaBancaria().getAtivo())
                || lancamento.getExtratoBancario().getContaBancaria().getDeletedAt() != null) {
            throw new InvalidOperationException("Conta bancaria deve estar ativa");
        }
        if (Boolean.TRUE.equals(lancamento.getConciliado())
                || repository.existsByExtratoBancarioLancamentoIdAndStatus(
                        lancamento.getId(), StatusConciliacaoBancaria.CONCILIADA)) {
            throw new ConflictException("Lancamento bancario ja esta conciliado");
        }
        if (!Boolean.TRUE.equals(movimento.getAtivo()) || movimento.getDeletedAt() != null
                || movimento.getStatus() != StatusMovimentoFinanceiro.ATIVO) {
            throw new InvalidOperationException("Movimento financeiro deve estar ativo");
        }
        if (repository.existsByMovimentoFinanceiroIdAndStatus(
                movimento.getId(), StatusConciliacaoBancaria.CONCILIADA)) {
            throw new ConflictException("Movimento financeiro ja esta conciliado");
        }
        validarTipo(lancamento, movimento);
        if (lancamento.getValor().compareTo(movimento.getValor()) != 0) {
            throw new InvalidOperationException("Valor do lancamento deve ser igual ao valor do movimento");
        }
    }

    private void validarTipo(ExtratoBancarioLancamento lancamento, MovimentoFinanceiro movimento) {
        boolean creditoEntrada = lancamento.getTipo() == TipoLancamentoBancario.CREDITO
                && movimento.getTipoMovimento() == TipoMovimentoFinanceiro.ENTRADA;
        boolean debitoSaida = lancamento.getTipo() == TipoLancamentoBancario.DEBITO
                && movimento.getTipoMovimento() == TipoMovimentoFinanceiro.SAIDA;
        if (!creditoEntrada && !debitoSaida) {
            throw new InvalidOperationException("Tipo do lancamento e incompativel com o movimento financeiro");
        }
    }
}
