package br.com.invplus.support.context;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Base comum para fluxos de integracao que executam services empresariais diretamente. */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractContextoEmpresarialIntegrationTest {

    @Autowired
    protected TestEmpresaContext testEmpresaContext;

    protected UUID usuarioContextoId;
    protected UUID empresaContextoId;
    protected UUID filialContextoId;

    protected void ativarContexto(UUID usuarioId, UUID empresaId, UUID filialId) {
        this.usuarioContextoId = usuarioId;
        this.empresaContextoId = empresaId;
        this.filialContextoId = filialId;
        testEmpresaContext.ativar(usuarioId, empresaId, filialId);
    }

    protected void ativarEmpresa(UUID empresaId) {
        ativarContexto(UUID.randomUUID(), empresaId, null);
    }

    @AfterEach
    protected void limparContextoEmpresarial() {
        testEmpresaContext.limpar();
    }
}
