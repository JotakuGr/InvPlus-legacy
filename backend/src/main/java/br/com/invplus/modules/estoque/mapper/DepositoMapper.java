package br.com.invplus.modules.estoque.mapper;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.dto.request.DepositoRequest;
import br.com.invplus.modules.estoque.dto.response.DepositoResponse;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import org.springframework.stereotype.Component;

/**
 * Converte Deposito entre entidade JPA e DTOs REST.
 */
@Component
public class DepositoMapper implements CrudMapper<Deposito, DepositoRequest, DepositoResponse> {

    @Override
    public Deposito novaEntidade() {
        return new Deposito();
    }

    public Deposito toEntity(DepositoRequest request) {
        Deposito deposito = novaEntidade();
        updateEntity(deposito, request);
        return deposito;
    }

    @Override
    public DepositoResponse toResponse(Deposito deposito) {
        Empresa empresa = deposito.getEmpresa();
        Filial filial = deposito.getFilial();
        return new DepositoResponse(
                deposito.getId(),
                empresa.getId(),
                nomeEmpresa(empresa),
                filial == null ? null : filial.getId(),
                filial == null ? null : filial.getNome(),
                deposito.getCodigo(),
                deposito.getNome(),
                deposito.getDescricao(),
                deposito.getTipoDeposito(),
                deposito.getPrincipal(),
                deposito.getPermiteVenda(),
                deposito.getPermiteCompra(),
                deposito.getPermiteTransferencia(),
                deposito.getControlaEstoque(),
                deposito.getBloqueado(),
                deposito.getAtivo(),
                deposito.getCriadoEm(),
                deposito.getAtualizadoEm()
        );
    }

    @Override
    public void aplicar(Deposito deposito, DepositoRequest request) {
        updateEntity(deposito, request);
    }

    /**
     * Atualiza campos simples; relacionamentos sao resolvidos no service.
     */
    public void updateEntity(Deposito deposito, DepositoRequest request) {
        deposito.atualizar(
                request.codigo(),
                request.nome(),
                request.descricao(),
                request.tipoDeposito(),
                request.principal(),
                request.permiteVenda(),
                request.permiteCompra(),
                request.permiteTransferencia(),
                request.controlaEstoque(),
                request.bloqueado()
        );
    }

    private String nomeEmpresa(Empresa empresa) {
        return empresa.getNomeFantasia() == null ? empresa.getRazaoSocial() : empresa.getNomeFantasia();
    }
}
