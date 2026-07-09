package br.com.invplus.modules.financeiro.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.financeiro.dto.request.ContaBancariaRequest;
import br.com.invplus.modules.financeiro.dto.response.ContaBancariaResponse;
import br.com.invplus.modules.financeiro.entity.ContaBancaria;
import org.springframework.stereotype.Component;

/**
 * Converte ContaBancaria sem acessar repositories.
 */
@Component
public class ContaBancariaMapper {

    public void aplicar(ContaBancaria entity, ContaBancariaRequest request, Empresa empresa, Filial filial) {
        entity.atualizar(empresa, filial, request.bancoCodigo(), request.bancoNome(),
                request.agencia(), request.agenciaDigito(), request.numeroConta(), request.contaDigito(),
                request.tipoConta(), request.titular(), request.documentoTitular(), request.pixChave(),
                request.saldoInicial(), request.dataSaldoInicial(), request.principal());
    }

    public ContaBancariaResponse toResponse(ContaBancaria entity) {
        Filial filial = entity.getFilial();
        return new ContaBancariaResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpresa().getNomeFantasia(),
                filial == null ? null : filial.getId(),
                filial == null ? null : filial.getNome(),
                entity.getBancoCodigo(),
                entity.getBancoNome(),
                entity.getAgencia(),
                entity.getAgenciaDigito(),
                entity.getNumeroConta(),
                entity.getContaDigito(),
                entity.getTipoConta(),
                entity.getTitular(),
                entity.getDocumentoTitular(),
                entity.getPixChave(),
                entity.getSaldoInicial(),
                entity.getSaldoAtual(),
                entity.getDataSaldoInicial(),
                entity.getPrincipal(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
