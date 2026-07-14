package br.com.invplus.security;

import br.com.invplus.security.entity.Perfil;
import br.com.invplus.security.entity.Permissao;
import br.com.invplus.security.entity.Usuario;
import br.com.invplus.security.repository.PerfilRepository;
import br.com.invplus.security.repository.PermissaoRepository;
import br.com.invplus.security.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SecuritySeedRepositoryTests {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Test
    void deveConsultarSeedInicialDeSegurancaViaRepository() {
        Usuario administrador = usuarioRepository.findByEmailAndAtivoTrueAndDeletedAtIsNull("admin@invplus.com").orElseThrow();
        Perfil admin = perfilRepository.findByNome("ADMIN").orElseThrow();
        Permissao acessoTotalSeguranca = permissaoRepository
                .findByRecursoAndAcao("SEGURANCA", "*")
                .orElseThrow();

        assertThat(administrador.getNome()).isEqualTo("Administrador");
        assertThat(administrador.getAtivo()).isTrue();
        assertThat(administrador.getDeletedAt()).isNull();
        assertThat(administrador.getSenhaHash()).isNotBlank();
        assertThat(administrador.getSenhaHash()).isNotEqualTo("admin123");
        assertThat(administrador.getPerfis())
                .extracting(perfil -> perfil.getNome())
                .contains("ADMIN");

        assertThat(admin.getAtivo()).isTrue();
        assertThat(admin.getPermissoes())
                .extracting(permissao -> permissao.getRecurso())
                .contains("SEGURANCA", "USUARIO", "PERFIL", "PERMISSAO");

        assertThat(acessoTotalSeguranca.getAtivo()).isTrue();
        assertThat(acessoTotalSeguranca.getNome()).isEqualTo("Acesso total de seguranca");
    }
}
