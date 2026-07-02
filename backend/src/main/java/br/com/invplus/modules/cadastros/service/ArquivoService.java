package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.dto.response.ArquivoResponse;
import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para metadados de arquivos anexados.
 */
@Service
@RequiredArgsConstructor
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;

    /**
     * Lista arquivos, com filtro opcional por entidade vinculada e paginacao.
     */
    @Transactional(readOnly = true)
    public Page<ArquivoResponse> listar(String entidade, UUID entidadeId, Pageable pageable) {
        Page<Arquivo> arquivos = entidade == null || entidadeId == null
                ? arquivoRepository.findByAtivoTrueAndDeletedAtIsNull(pageable)
                : arquivoRepository.findByEntidadeAndEntidadeIdAndAtivoTrueAndDeletedAtIsNull(
                        entidade.trim().toUpperCase(),
                        entidadeId,
                        pageable
                );
        return arquivos.map(ArquivoResponse::from);
    }

    /**
     * Busca metadados de arquivo pelo identificador.
     */
    @Transactional(readOnly = true)
    public ArquivoResponse buscar(UUID id) {
        return ArquivoResponse.from(buscarEntidade(id));
    }

    /**
     * Registra um novo arquivo sem armazenar o binario no banco.
     */
    @Transactional
    public ArquivoResponse criar(ArquivoRequest request) {
        Arquivo arquivo = new Arquivo();
        aplicar(arquivo, request);
        return ArquivoResponse.from(arquivoRepository.save(arquivo));
    }

    /**
     * Atualiza metadados de arquivo existentes.
     */
    @Transactional
    public ArquivoResponse atualizar(UUID id, ArquivoRequest request) {
        Arquivo arquivo = buscarEntidade(id);
        aplicar(arquivo, request);
        return ArquivoResponse.from(arquivo);
    }

    /**
     * Realiza exclusao logica dos metadados do arquivo.
     */
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    private void aplicar(Arquivo arquivo, ArquivoRequest request) {
        arquivo.atualizar(request.nomeOriginal(), request.nomeArmazenado(), request.contentType(),
                request.tamanhoBytes(), request.caminho(), request.checksumSha256(),
                request.entidade(), request.entidadeId());
    }

    private Arquivo buscarEntidade(UUID id) {
        return arquivoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arquivo nao encontrado"));
    }
}
