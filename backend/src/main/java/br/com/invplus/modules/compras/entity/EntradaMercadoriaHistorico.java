package br.com.invplus.modules.compras.entity;

import br.com.invplus.modules.compras.enums.StatusEntradaMercadoria;
import br.com.invplus.shared.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historico imutavel das transicoes de status de uma entrada de mercadoria.
 */
@Getter
@Setter
@Entity
@Table(
        name = "entrada_mercadoria_historico",
        indexes = @Index(name = "idx_entrada_mercadoria_historico_entrada", columnList = "entrada_mercadoria_id,data_alteracao")
)
@NoArgsConstructor
public class EntradaMercadoriaHistorico extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrada_mercadoria_id", nullable = false)
    private EntradaMercadoria entradaMercadoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusEntradaMercadoria statusAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "novo_status", nullable = false, length = 30)
    private StatusEntradaMercadoria novoStatus;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @NotNull
    @Column(name = "data_alteracao", nullable = false)
    private OffsetDateTime dataAlteracao = OffsetDateTime.now();

    @Column(name = "observacao")
    private String observacao;
}
