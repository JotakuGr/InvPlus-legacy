package br.com.invplus.security.validator;

import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import br.com.invplus.modules.financeiro.entity.ExtratoBancario;
import br.com.invplus.modules.financeiro.entity.ExtratoBancarioLancamento;
import br.com.invplus.modules.financeiro.entity.MovimentoFinanceiro;
import br.com.invplus.security.exception.AcessoEmpresaNegadoException;
import br.com.invplus.security.exception.AcessoFilialNegadoException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Defesa adicional para recursos empresariais ja carregados pelo dominio. */
@Component
public class EmpresaResourceAccessValidator {

    public void validarEmpresa(UUID empresaEsperada, UUID empresaRecurso) {
        if (empresaEsperada == null || empresaRecurso == null || !empresaEsperada.equals(empresaRecurso)) {
            throw new AcessoEmpresaNegadoException();
        }
    }

    public void validarFilialOpcional(UUID filialContexto, UUID filialRecurso) {
        if (filialContexto != null && filialRecurso != null && !filialContexto.equals(filialRecurso)) {
            throw new AcessoFilialNegadoException();
        }
    }

    public void validarContaBancaria(UUID empresaId, ContaBancaria conta) {
        validarEmpresa(empresaId, conta.getEmpresa().getId());
    }

    public void validarExtrato(UUID empresaId, ExtratoBancario extrato) {
        validarContaBancaria(empresaId, extrato.getContaBancaria());
    }

    public void validarLancamento(UUID empresaId, ExtratoBancarioLancamento lancamento) {
        validarExtrato(empresaId, lancamento.getExtratoBancario());
    }

    public void validarMovimento(UUID empresaId, MovimentoFinanceiro movimento) {
        validarEmpresa(empresaId, movimento.getEmpresa().getId());
    }
}
