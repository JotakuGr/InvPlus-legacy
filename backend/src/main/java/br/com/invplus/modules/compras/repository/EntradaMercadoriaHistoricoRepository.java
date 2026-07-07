package br.com.invplus.modules.compras.repository;

import br.com.invplus.modules.compras.entity.EntradaMercadoriaHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consulta o historico de transicoes da entrada de mercadoria.
 */
public interface EntradaMercadoriaHistoricoRepository extends JpaRepository<EntradaMercadoriaHistorico, UUID> {

    List<EntradaMercadoriaHistorico> findByEntradaMercadoriaIdOrderByDataAlteracaoDesc(UUID entradaMercadoriaId);
}
