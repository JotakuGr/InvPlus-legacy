package br.com.invplus.support.security;

import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class EmpresaTestFactory {

    private final EmpresaRepository empresaRepository;

    public Empresa criarAtiva() {
        String sufixo = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String cnpj = String.format("%014d", Math.abs((long) UUID.randomUUID().hashCode()));
        return empresaRepository.saveAndFlush(new Empresa(
                "Empresa Auth " + sufixo,
                "Empresa Auth " + sufixo,
                cnpj,
                null,
                null,
                null));
    }
}
