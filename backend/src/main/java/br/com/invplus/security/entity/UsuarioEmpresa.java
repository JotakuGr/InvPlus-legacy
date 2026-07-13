package br.com.invplus.security.entity;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.shared.domain.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "usuario_empresa")
public class UsuarioEmpresa extends BaseEntity {
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false) private Boolean principal = false;
    @Column(name = "acesso_todas_filiais", nullable = false) private Boolean acessoTodasFiliais = false;
    @Column(nullable = false) private Boolean ativo = true;

    public void configurar(Usuario usuario, Empresa empresa, Boolean principal, Boolean acessoTodasFiliais) {
        this.usuario = usuario; this.empresa = empresa;
        this.principal = Boolean.TRUE.equals(principal);
        this.acessoTodasFiliais = Boolean.TRUE.equals(acessoTodasFiliais);
        this.ativo = true;
    }
    public void desativar() { this.ativo = false; this.principal = false; }
}
