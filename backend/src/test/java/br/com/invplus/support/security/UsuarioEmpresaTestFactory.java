package br.com.invplus.support.security;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.entity.UsuarioEmpresa;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class UsuarioEmpresaTestFactory {

    private final UsuarioEmpresaRepository repository;

    public UsuarioEmpresa criar(Usuario usuario, Empresa empresa, boolean principal, boolean todasFiliais) {
        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.configurar(usuario, empresa, principal, todasFiliais);
        return repository.saveAndFlush(vinculo);
    }
}
