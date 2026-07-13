package br.com.invplus.security;
import static org.assertj.core.api.Assertions.*;
import br.com.invplus.security.context.SecurityEmpresaContext;import br.com.invplus.security.exception.*;import br.com.invplus.security.service.AuthenticatedUserDetails;
import java.util.*;import org.junit.jupiter.api.*;import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;import org.springframework.security.core.context.SecurityContextHolder;
class SecurityEmpresaContextTest {
 @AfterEach void limpar(){SecurityContextHolder.clearContext();}
 @Test void deveRetornarEmpresaEFilial(){UUID u=UUID.randomUUID(),e=UUID.randomUUID(),f=UUID.randomUUID();autenticar(u,e,f);var c=new SecurityEmpresaContext();
  assertThat(c.getUsuarioId()).isEqualTo(u);assertThat(c.getEmpresaId()).isEqualTo(e);assertThat(c.requireFilialId()).isEqualTo(f);}
 @Test void deveBloquearEmpresaEFilialAusentes(){autenticar(UUID.randomUUID(),null,null);var c=new SecurityEmpresaContext();
  assertThatThrownBy(c::getEmpresaId).isInstanceOf(ContextoEmpresarialAusenteException.class);
  assertThatThrownBy(c::requireFilialId).isInstanceOf(ContextoFilialAusenteException.class);}
 private void autenticar(UUID u,UUID e,UUID f){var p=new AuthenticatedUserDetails(u,"a@b.com","x",List.of(),true,e,f);
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p,null,List.of()));}
}
