package br.com.invplus.security;
import static org.assertj.core.api.Assertions.*;
import br.com.invplus.security.service.*;import java.util.*;import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.test.context.ActiveProfiles;
@SpringBootTest @ActiveProfiles("test") class JwtContextoEmpresarialServiceTest {
 @Autowired JwtService jwt;
 @Test void deveGerarEExtrairContextoAccess(){UUID u=UUID.randomUUID(),e=UUID.randomUUID(),f=UUID.randomUUID();var p=new AuthenticatedUserDetails(u,"jwt@teste.com","x",List.of(),true,e,f);
  String token=jwt.gerarAccessToken(p,e,f);assertThat(jwt.tokenDoTipo(token,"ACCESS")).isTrue();assertThat(jwt.extrairUsuarioId(token)).isEqualTo(u);assertThat(jwt.extrairUuidClaim(token,"empresa_id")).isEqualTo(e);assertThat(jwt.extrairUuidClaimOpcional(token,"filial_id")).isEqualTo(f);}
 @Test void deveDiferenciarSelectionToken(){String token=jwt.gerarSelectionToken(UUID.randomUUID());assertThat(jwt.tokenDoTipo(token,"COMPANY_SELECTION")).isTrue();assertThat(jwt.tokenDoTipo(token,"ACCESS")).isFalse();}
 @Test void deveBloquearUuidInvalido(){String token=jwt.gerarToken(new org.springframework.security.core.userdetails.User("u","p",List.of()));assertThatThrownBy(()->jwt.extrairUsuarioId(token)).isInstanceOf(IllegalArgumentException.class);}
}
