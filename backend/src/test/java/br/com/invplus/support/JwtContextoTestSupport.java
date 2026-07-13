package br.com.invplus.support;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.entity.UsuarioEmpresa;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.service.AuthenticatedUserDetails;
import br.com.invplus.security.service.JwtService;
import java.util.List;

/** Utilitario para gerar access tokens com contexto empresarial nos testes MVC. */
public final class JwtContextoTestSupport {

    private JwtContextoTestSupport() {
    }

    public static String gerarAccessToken(
            JwtService jwtService,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            Usuario usuario,
            Empresa empresa
    ) {
        vincular(usuarioEmpresaRepository, usuario, empresa);
        var principal = new AuthenticatedUserDetails(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getSenhaHash(),
                List.of(),
                Boolean.TRUE.equals(usuario.getAtivo()),
                empresa.getId(),
                null
        );
        return jwtService.gerarAccessToken(principal, empresa.getId(), null);
    }

    private static void vincular(
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            Usuario usuario,
            Empresa empresa
    ) {
        if (usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaIdAndAtivoTrue(usuario.getId(), empresa.getId())
                .isPresent()) {
            return;
        }

        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.configurar(usuario, empresa, false, true);
        usuarioEmpresaRepository.saveAndFlush(vinculo);
    }
}
