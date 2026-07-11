package br.com.invplus.security.service;

import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.security.exception.*;
import br.com.invplus.security.repository.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor
public class UsuarioEmpresaAccessValidator {
    private final UsuarioEmpresaRepository vinculoRepository;
    private final UsuarioEmpresaFilialRepository filialVinculoRepository;
    private final FilialRepository filialRepository;

    @Transactional(readOnly = true)
    public void validarContexto(UUID usuarioId, UUID empresaId, UUID filialId) {
        var vinculo = vinculoRepository.findByUsuarioIdAndEmpresaIdAndAtivoTrue(usuarioId, empresaId)
                .orElseThrow(AcessoEmpresaNegadoException::new);
        if (!Boolean.TRUE.equals(vinculo.getEmpresa().getAtivo()) || vinculo.getEmpresa().getDeletedAt()!=null)
            throw new AcessoEmpresaNegadoException();
        if (filialId == null) return;
        var filial = filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(filialId)
                .orElseThrow(AcessoFilialNegadoException::new);
        if (!filial.getEmpresa().getId().equals(empresaId)) throw new AcessoFilialNegadoException();
        if (!Boolean.TRUE.equals(vinculo.getAcessoTodasFiliais())
                && !filialVinculoRepository.existsByUsuarioEmpresaIdAndFilialIdAndAtivoTrue(vinculo.getId(), filialId))
            throw new AcessoFilialNegadoException();
    }
}
