package br.com.invplus.modules.pagamentos.mapper;

import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoParcelaRequest;
import br.com.invplus.modules.pagamentos.dto.request.CondicaoPagamentoRequest;
import br.com.invplus.modules.pagamentos.dto.response.CondicaoPagamentoParcelaResponse;
import br.com.invplus.modules.pagamentos.dto.response.CondicaoPagamentoResponse;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamento;
import br.com.invplus.modules.pagamentos.entity.CondicaoPagamentoParcela;
import br.com.invplus.shared.domain.mapper.CrudMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Converte CondicaoPagamento entre entidade JPA, payload de entrada e resposta da API.
 */
@Component
public class CondicaoPagamentoMapper implements CrudMapper<CondicaoPagamento, CondicaoPagamentoRequest, CondicaoPagamentoResponse> {

    @Override
    public CondicaoPagamento novaEntidade() {
        return new CondicaoPagamento();
    }

    @Override
    public void aplicar(CondicaoPagamento entity, CondicaoPagamentoRequest request) {
        entity.atualizar(
                request.codigo(),
                request.nome(),
                request.jurosPercentual(),
                request.multaPercentual(),
                request.descontoPercentual(),
                request.regraVencimento(),
                request.consideraFeriados()
        );
        entity.substituirParcelas(toParcelas(entity, request.parcelas()));
    }

    @Override
    public CondicaoPagamentoResponse toResponse(CondicaoPagamento entity) {
        return new CondicaoPagamentoResponse(
                entity.getId(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getRegraVencimento(),
                entity.getConsideraFeriados(),
                entity.getJurosPercentual(),
                entity.getMultaPercentual(),
                entity.getDescontoPercentual(),
                entity.getParcelas().stream()
                        .filter(parcela -> Boolean.TRUE.equals(parcela.getAtivo()) && parcela.getDeletedAt() == null)
                        .sorted(Comparator.comparing((CondicaoPagamentoParcela parcela) -> Objects.requireNonNull(
                                parcela.getNumeroParcela(), "Numero da parcela nao pode ser nulo")))
                        .map(this::toParcelaResponse)
                        .toList(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    /**
     * Monta as parcelas recebidas no request e vincula cada uma a condicao proprietaria.
     */
    private List<CondicaoPagamentoParcela> toParcelas(CondicaoPagamento condicao, List<CondicaoPagamentoParcelaRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> {
                    CondicaoPagamentoParcela parcela = new CondicaoPagamentoParcela();
                    parcela.atualizar(condicao, request.numeroParcela(), request.dias(), request.percentual());
                    return parcela;
                })
                .toList();
    }

    /**
     * Converte uma parcela ativa da entidade para o DTO de resposta.
     */
    private CondicaoPagamentoParcelaResponse toParcelaResponse(CondicaoPagamentoParcela parcela) {
        return new CondicaoPagamentoParcelaResponse(
                parcela.getId(),
                parcela.getNumeroParcela(),
                parcela.getDias(),
                parcela.getPercentual(),
                parcela.getAtivo()
        );
    }
}
