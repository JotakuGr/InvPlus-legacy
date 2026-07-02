package br.com.invplus.modules.cadastros.mapper;

import br.com.invplus.modules.cadastros.dto.request.PaisRequest;
import br.com.invplus.modules.cadastros.dto.response.PaisResponse;
import br.com.invplus.modules.cadastros.entity.Pais;
import org.springframework.stereotype.Component;

/**
 * Conversoes entre entidade Pais e DTOs da API.
 */
@Component
public class PaisMapper {

    /**
     * Copia os dados do request para a entidade.
     */
    public void aplicar(Pais pais, PaisRequest request) {
        pais.atualizar(request.nome(), request.codigoIso2(), request.codigoIso3(), request.codigoBacen());
    }

    /**
     * Monta a resposta publica do pais.
     */
    public PaisResponse toResponse(Pais pais) {
        return new PaisResponse(pais.getId(), pais.getNome(), pais.getCodigoIso2(), pais.getCodigoIso3(),
                pais.getCodigoBacen(), pais.getAtivo(), pais.getVersion(), pais.getCriadoEm(), pais.getAtualizadoEm());
    }
}
