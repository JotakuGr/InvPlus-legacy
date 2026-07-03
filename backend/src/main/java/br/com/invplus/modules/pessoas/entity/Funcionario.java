package br.com.invplus.modules.pessoas.entity;

import br.com.invplus.security.entity.Usuario;
import br.com.invplus.shared.domain.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Papel de funcionario associado a uma Pessoa e opcionalmente a um Usuario do sistema.
 */
@Getter
@Setter
@Entity
@Table(
        name = "funcionario",
        indexes = {
                @Index(name = "idx_funcionario_pessoa", columnList = "pessoa_id"),
                @Index(name = "idx_funcionario_usuario", columnList = "usuario_id"),
                @Index(name = "idx_funcionario_ativo", columnList = "ativo")
        }
)
@NoArgsConstructor
public class Funcionario extends SoftDeleteEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotBlank
    @Size(max = 120)
    @Column(name = "cargo", nullable = false, length = 120)
    private String cargo;

    @NotNull
    @PositiveOrZero
    @Column(name = "salario", nullable = false, precision = 15, scale = 2)
    private BigDecimal salario = BigDecimal.ZERO;

    @NotNull
    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "data_demissao")
    private LocalDate dataDemissao;

    @Column(name = "observacao")
    private String observacao;

    /**
     * Atualiza dados trabalhistas do funcionario sem alterar dados cadastrais da Pessoa ou do Usuario.
     */
    public void atualizar(
            String cargo,
            BigDecimal salario,
            LocalDate dataAdmissao,
            LocalDate dataDemissao,
            String observacao
    ) {
        this.cargo = cargo == null ? null : cargo.trim();
        this.salario = salario == null ? BigDecimal.ZERO : salario;
        this.dataAdmissao = dataAdmissao;
        this.dataDemissao = dataDemissao;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }
}
