package br.com.invplus.support.security;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.security.entity.UsuarioEmpresa;
import br.com.invplus.security.repository.UsuarioEmpresaRepository;
import br.com.invplus.security.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantem o administrador seed com um unico contexto para testes legados que exercitam o login real.
 */
@Component
@Profile("test")
@RequiredArgsConstructor
public class LegacyAdminEmpresaContextInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@invplus.com";
    private static final String EMPRESA_CNPJ = "99999999000191";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var admin = usuarioRepository.findByEmailAndAtivoTrueAndDeletedAtIsNull(ADMIN_EMAIL).orElse(null);
        if (admin == null) {
            return;
        }

        Empresa empresa = empresaRepository.findByCnpjAndAtivoTrueAndDeletedAtIsNull(EMPRESA_CNPJ)
                .orElseGet(() -> empresaRepository.saveAndFlush(new Empresa(
                        "Empresa Contexto Teste",
                        "Empresa Contexto Teste",
                        EMPRESA_CNPJ,
                        null,
                        null,
                        null)));

        List<UsuarioEmpresa> vinculos = usuarioEmpresaRepository.findAll().stream()
                .filter(vinculo -> vinculo.getUsuario().getId().equals(admin.getId()))
                .toList();

        UsuarioEmpresa vinculoPadrao = vinculos.stream()
                .filter(vinculo -> vinculo.getEmpresa().getId().equals(empresa.getId()))
                .findFirst()
                .orElseGet(UsuarioEmpresa::new);

        vinculos.stream()
                .filter(vinculo -> !vinculo.getEmpresa().getId().equals(empresa.getId()))
                .filter(vinculo -> Boolean.TRUE.equals(vinculo.getAtivo()))
                .forEach(UsuarioEmpresa::desativar);

        vinculoPadrao.configurar(admin, empresa, true, true);
        usuarioEmpresaRepository.saveAllAndFlush(vinculos);
        usuarioEmpresaRepository.saveAndFlush(vinculoPadrao);
    }
}
