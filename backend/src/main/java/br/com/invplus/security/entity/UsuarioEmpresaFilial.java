package br.com.invplus.security.entity;

import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.shared.domain.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "usuario_empresa_filial")
public class UsuarioEmpresaFilial extends BaseEntity {
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_empresa_id", nullable = false)
    private UsuarioEmpresa usuarioEmpresa;
    @NotNull @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filial_id", nullable = false)
    private Filial filial;
    @Column(nullable = false) private Boolean principal = false;
    @Column(nullable = false) private Boolean ativo = true;
    public void configurar(UsuarioEmpresa vinculo, Filial filial, Boolean principal) {
        this.usuarioEmpresa=vinculo; this.filial=filial; this.principal=Boolean.TRUE.equals(principal); this.ativo=true;
    }
    public void desativar() { this.ativo=false; this.principal=false; }
}
