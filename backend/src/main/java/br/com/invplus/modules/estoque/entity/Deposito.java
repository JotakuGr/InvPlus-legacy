package br.com.invplus.modules.estoque.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.estoque.enums.TipoDeposito;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Local fisico, logico ou em transito onde o estoque fica armazenado.
 */
@Getter
@Setter
@Entity
@Table(name = "deposito")
@NoArgsConstructor
public class Deposito extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo", nullable = false, length = 30, columnDefinition = "citext")
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_deposito", nullable = false, length = 30)
    private TipoDeposito tipoDeposito;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Column(name = "permite_venda", nullable = false)
    private Boolean permiteVenda = true;

    @Column(name = "permite_compra", nullable = false)
    private Boolean permiteCompra = true;

    @Column(name = "permite_transferencia", nullable = false)
    private Boolean permiteTransferencia = true;

    @Column(name = "controla_estoque", nullable = false)
    private Boolean controlaEstoque = true;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado = false;

    /**
     * Atualiza os campos editaveis com normalizacao de codigo e defaults booleanos.
     */
    public void atualizar(
            String codigo,
            String nome,
            String descricao,
            TipoDeposito tipoDeposito,
            Boolean principal,
            Boolean permiteVenda,
            Boolean permiteCompra,
            Boolean permiteTransferencia,
            Boolean controlaEstoque,
            Boolean bloqueado
    ) {
        this.codigo = normalizarCodigo(codigo);
        this.nome = normalizarTexto(nome);
        this.descricao = normalizarTexto(descricao);
        this.tipoDeposito = tipoDeposito;
        this.principal = Boolean.TRUE.equals(principal);
        this.permiteVenda = permiteVenda == null || Boolean.TRUE.equals(permiteVenda);
        this.permiteCompra = permiteCompra == null || Boolean.TRUE.equals(permiteCompra);
        this.permiteTransferencia = permiteTransferencia == null || Boolean.TRUE.equals(permiteTransferencia);
        this.controlaEstoque = controlaEstoque == null || Boolean.TRUE.equals(controlaEstoque);
        this.bloqueado = Boolean.TRUE.equals(bloqueado);
    }

    private String normalizarCodigo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
