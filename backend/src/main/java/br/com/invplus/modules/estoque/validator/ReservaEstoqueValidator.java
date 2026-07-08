package br.com.invplus.modules.estoque.validator;

import br.com.invplus.modules.estoque.dto.request.ReservaEstoqueRequest;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.ReservaEstoque;
import br.com.invplus.modules.estoque.enums.StatusReservaEstoque;
import br.com.invplus.modules.estoque.repository.ReservaEstoqueRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Valida regras de criacao e transicao de reservas de estoque.
 */
@Component
public class ReservaEstoqueValidator {

    private final ReservaEstoqueRepository repository;

    public ReservaEstoqueValidator(ReservaEstoqueRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(ReservaEstoqueRequest request, Estoque estoque) {
        validarCamposObrigatorios(request);
        validarQuantidade(request.quantidade());
        validarEstoqueAtivo(estoque);
        validarDisponibilidade(request.quantidade(), estoque);
        validarDuplicidadeOrigem(request, estoque);
    }

    public void validarCancelamento(ReservaEstoque reserva) {
        if (reserva.getStatus() != StatusReservaEstoque.ATIVA) {
            throw new BusinessException("Somente reserva ativa pode ser cancelada");
        }
    }

    public void validarBaixa(ReservaEstoque reserva) {
        if (reserva.getStatus() == StatusReservaEstoque.CANCELADA) {
            throw new BusinessException("Reserva cancelada nao pode ser baixada");
        }
        if (reserva.getStatus() == StatusReservaEstoque.BAIXADA) {
            throw new BusinessException("Reserva ja baixada nao pode ser baixada novamente");
        }
        if (reserva.getStatus() == StatusReservaEstoque.EXPIRADA) {
            throw new BusinessException("Reserva expirada nao pode ser baixada");
        }
        if (reserva.getStatus() != StatusReservaEstoque.ATIVA) {
            throw new BusinessException("Somente reserva ativa pode ser baixada");
        }
    }

    private void validarCamposObrigatorios(ReservaEstoqueRequest request) {
        if (request.estoqueId() == null) {
            throw new BusinessException("Estoque e obrigatorio");
        }
        if (request.origem() == null) {
            throw new BusinessException("Origem e obrigatoria");
        }
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantidade da reserva deve ser maior que zero");
        }
    }

    private void validarEstoqueAtivo(Estoque estoque) {
        if (!Boolean.TRUE.equals(estoque.getAtivo()) || estoque.getDeletedAt() != null) {
            throw new BusinessException("Estoque deve estar ativo para reserva");
        }
    }

    private void validarDisponibilidade(BigDecimal quantidade, Estoque estoque) {
        if (estoque.getQuantidadeDisponivel().compareTo(quantidade) < 0) {
            throw new BusinessException("Quantidade da reserva nao pode ser maior que a quantidade disponivel");
        }
    }

    private void validarDuplicidadeOrigem(ReservaEstoqueRequest request, Estoque estoque) {
        if (request.origemId() == null) {
            return;
        }
        if (repository.existsByEstoqueIdAndOrigemAndOrigemIdAndStatusAndAtivoTrueAndDeletedAtIsNull(
                estoque.getId(),
                request.origem(),
                request.origemId(),
                StatusReservaEstoque.ATIVA
        )) {
            throw new ConflictException("Ja existe reserva ativa para esta origem");
        }
    }
}
