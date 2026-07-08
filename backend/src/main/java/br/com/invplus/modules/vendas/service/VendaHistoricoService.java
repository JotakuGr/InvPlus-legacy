package br.com.invplus.modules.vendas.service;

import br.com.invplus.modules.vendas.entity.PedidoVenda;
import br.com.invplus.modules.vendas.entity.PedidoVendaHistorico;
import br.com.invplus.modules.vendas.enums.StatusPedidoVenda;
import br.com.invplus.modules.vendas.repository.PedidoVendaHistoricoRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centraliza a escrita do historico operacional do modulo Vendas.
 */
@Service
public class VendaHistoricoService {

    private final PedidoVendaHistoricoRepository historicoRepository;
    private final AuditorAware<UUID> auditorAware;

    public VendaHistoricoService(PedidoVendaHistoricoRepository historicoRepository, AuditorAware<UUID> auditorAware) {
        this.historicoRepository = historicoRepository;
        this.auditorAware = auditorAware;
    }

    @Transactional
    public void registrarPedido(PedidoVenda pedido, StatusPedidoVenda anterior, StatusPedidoVenda novo, String observacao) {
        PedidoVendaHistorico historico = new PedidoVendaHistorico();
        historico.setPedidoVenda(pedido);
        historico.setStatusAnterior(anterior);
        historico.setStatusNovo(novo);
        historico.setObservacao(normalizar(observacao));
        historicoRepository.save(historico);
    }

    public Optional<UUID> usuarioAtual() {
        return auditorAware.getCurrentAuditor();
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
