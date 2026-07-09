package br.com.invplus.modules.financeiro.repository;

import br.com.invplus.modules.financeiro.entity.Pagamento;
import br.com.invplus.shared.domain.repository.BaseRepository;
import java.util.List;
import java.util.UUID;

/**
 * Persistencia dos pagamentos financeiros registrados para parcelas.
 */
public interface PagamentoRepository extends BaseRepository<Pagamento> {

    List<Pagamento> findByParcelaPagarIdAndAtivoTrueAndDeletedAtIsNull(UUID parcelaId);

    List<Pagamento> findByContaPagarIdAndAtivoTrueAndDeletedAtIsNull(UUID contaId);
}
