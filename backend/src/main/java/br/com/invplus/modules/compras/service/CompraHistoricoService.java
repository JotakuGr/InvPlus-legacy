package br.com.invplus.modules.compras.service;

import br.com.invplus.modules.compras.entity.EntradaMercadoria;
import br.com.invplus.modules.compras.entity.EntradaMercadoriaHistorico;
import br.com.invplus.modules.compras.entity.PedidoCompra;
import br.com.invplus.modules.compras.entity.PedidoCompraHistorico;
import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import br.com.invplus.modules.compras.enums.StatusPedidoCompra;
import br.com.invplus.modules.compras.repository.EntradaMercadoriaHistoricoRepository;
import br.com.invplus.modules.compras.repository.PedidoCompraHistoricoRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centraliza a escrita de historico de status do modulo Compras.
 */
@Service
public class CompraHistoricoService {

    private final PedidoCompraHistoricoRepository pedidoHistoricoRepository;
    private final EntradaMercadoriaHistoricoRepository entradaHistoricoRepository;
    private final AuditorAware<UUID> auditorAware;

    public CompraHistoricoService(
            PedidoCompraHistoricoRepository pedidoHistoricoRepository,
            EntradaMercadoriaHistoricoRepository entradaHistoricoRepository,
            AuditorAware<UUID> auditorAware
    ) {
        this.pedidoHistoricoRepository = pedidoHistoricoRepository;
        this.entradaHistoricoRepository = entradaHistoricoRepository;
        this.auditorAware = auditorAware;
    }

    @Transactional
    public void registrarPedido(PedidoCompra pedido, StatusPedidoCompra anterior, StatusPedidoCompra novo, String observacao) {
        PedidoCompraHistorico historico = new PedidoCompraHistorico();
        historico.setPedidoCompra(pedido);
        historico.setStatusAnterior(anterior);
        historico.setNovoStatus(novo);
        historico.setUsuarioId(usuarioAtual().orElse(null));
        historico.setDataAlteracao(OffsetDateTime.now());
        historico.setObservacao(normalizar(observacao));
        pedidoHistoricoRepository.save(historico);
    }

    @Transactional
    public void registrarEntrada(EntradaMercadoria entrada, StatusEntradaMercadoria anterior,
                                 StatusEntradaMercadoria novo, String observacao) {
        EntradaMercadoriaHistorico historico = new EntradaMercadoriaHistorico();
        historico.setEntradaMercadoria(entrada);
        historico.setStatusAnterior(anterior);
        historico.setNovoStatus(novo);
        historico.setUsuarioId(usuarioAtual().orElse(null));
        historico.setDataAlteracao(OffsetDateTime.now());
        historico.setObservacao(normalizar(observacao));
        entradaHistoricoRepository.save(historico);
    }

    public Optional<UUID> usuarioAtual() {
        return auditorAware.getCurrentAuditor();
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
