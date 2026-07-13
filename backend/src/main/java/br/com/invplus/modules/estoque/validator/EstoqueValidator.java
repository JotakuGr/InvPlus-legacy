package br.com.invplus.modules.estoque.validator;

import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.dto.request.EstoqueRequest;
import br.com.invplus.modules.estoque.entity.Deposito;
import br.com.invplus.modules.estoque.entity.Estoque;
import br.com.invplus.modules.estoque.entity.LocalizacaoEstoque;
import br.com.invplus.modules.estoque.repository.EstoqueRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Valida regras estruturais do saldo atual de estoque.
 */
@Component
public class EstoqueValidator {

    private final EstoqueRepository repository;

    public EstoqueValidator(EstoqueRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(UUID empresaId, EstoqueRequest request, Filial filial, Deposito deposito,
            LocalizacaoEstoque localizacao) {
        validarCamposObrigatorios(request);
        validarValores(request);
        validarVinculos(empresaId, request, filial, deposito, localizacao);
        validarDuplicidade(empresaId, request, null);
    }

    public void validarAtualizacao(UUID empresaId, Estoque atual, EstoqueRequest request, Filial filial,
            Deposito deposito, LocalizacaoEstoque localizacao) {
        validarCamposObrigatorios(request);
        validarValores(request);
        validarVinculos(empresaId, request, filial, deposito, localizacao);
        validarDuplicidade(empresaId, request, atual.getId());
    }

    private void validarCamposObrigatorios(EstoqueRequest request) {
        if (request.produtoId() == null) {
            throw new BusinessException("Produto e obrigatorio");
        }
        if (request.depositoId() == null) {
            throw new BusinessException("Deposito e obrigatorio");
        }
        if (request.localizacaoId() == null) {
            throw new BusinessException("Localizacao e obrigatoria");
        }
    }

    private void validarValores(EstoqueRequest request) {
        validarNaoNegativo(request.quantidade(), "Quantidade nao pode ser negativa");
        validarNaoNegativo(request.quantidadeReservada(), "Quantidade reservada nao pode ser negativa");
        validarNaoNegativo(request.quantidadeAvariada(), "Quantidade avariada nao pode ser negativa");
        validarNaoNegativo(request.quantidadeTransito(), "Quantidade em transito nao pode ser negativa");
        validarNaoNegativo(request.estoqueMinimo(), "Estoque minimo nao pode ser negativo");
        validarNaoNegativo(request.estoqueMaximo(), "Estoque maximo nao pode ser negativo");

        BigDecimal quantidade = valorOuZero(request.quantidade());
        BigDecimal reservada = valorOuZero(request.quantidadeReservada());
        if (reservada.compareTo(quantidade) > 0) {
            throw new BusinessException("Quantidade reservada nao pode ser maior que a quantidade fisica");
        }
        if (request.estoqueMaximo() != null && request.estoqueMinimo() != null
                && request.estoqueMaximo().compareTo(request.estoqueMinimo()) < 0) {
            throw new BusinessException("Estoque maximo deve ser maior ou igual ao estoque minimo");
        }
    }

    private void validarVinculos(UUID empresaId, EstoqueRequest request, Filial filial, Deposito deposito,
            LocalizacaoEstoque localizacao) {
        if (filial != null && !filial.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessException("Filial deve pertencer a empresa ativa");
        }
        if (!deposito.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessException("Deposito deve pertencer a empresa ativa");
        }
        if (filial != null && deposito.getFilial() != null && !deposito.getFilial().getId().equals(filial.getId())) {
            throw new BusinessException("Deposito deve pertencer a filial informada");
        }
        if (!localizacao.getDeposito().getId().equals(request.depositoId())) {
            throw new BusinessException("Localizacao deve pertencer ao deposito informado");
        }
    }

    private void validarDuplicidade(UUID empresaId, EstoqueRequest request, UUID idAtual) {
        repository.findByProdutoIdAndEmpresaIdAndDepositoIdAndLocalizacaoIdAndAtivoTrueAndDeletedAtIsNull(
                        request.produtoId(),
                        empresaId,
                        request.depositoId(),
                        request.localizacaoId()
                )
                .map(Estoque::getId)
                .filter(idEncontrado -> idAtual == null || !idEncontrado.equals(idAtual))
                .ifPresent(idEncontrado -> {
                    throw new ConflictException("Ja existe saldo ativo para este produto nesta empresa, deposito e localizacao");
                });
    }

    private void validarNaoNegativo(BigDecimal valor, String mensagem) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(mensagem);
        }
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
