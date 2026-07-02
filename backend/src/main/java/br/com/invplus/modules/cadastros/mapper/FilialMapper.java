package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.response.FilialResponse;
import br.com.invplus.modules.cadastros.entity.Filial;
import org.springframework.stereotype.Component;

/**
 * Conversoes entre entidade Filial e DTOs da API.
 */
@Component
public class FilialMapper {

    /**
     * Monta a resposta publica da filial.
     */
    public FilialResponse toResponse(Filial filial) {
        return new FilialResponse(filial.getId(), filial.getEmpresa().getId(),
                filial.getEndereco() == null ? null : filial.getEndereco().getId(), filial.getCodigo(),
                filial.getNome(), filial.getCnpj(), filial.getInscricaoEstadual(), filial.getTelefone(),
                filial.getEmail(), filial.getMatriz(), filial.getAtivo(), filial.getVersion(),
                filial.getCriadoEm(), filial.getAtualizadoEm());
    }
}
