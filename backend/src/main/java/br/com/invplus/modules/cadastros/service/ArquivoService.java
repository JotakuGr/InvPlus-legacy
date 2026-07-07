package br.com.invplus.modules.cadastros.service;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.invplus.modules.cadastros.dto.request.ArquivoRequest;
import br.com.invplus.modules.cadastros.dto.response.ArquivoResponse;
import br.com.invplus.modules.cadastros.entity.Arquivo;
import br.com.invplus.modules.cadastros.enums.StorageArquivo;
import br.com.invplus.modules.cadastros.mapper.ArquivoMapper;
import br.com.invplus.modules.cadastros.repository.ArquivoRepository;
import br.com.invplus.modules.cadastros.validator.ArquivoValidator;
import br.com.invplus.shared.domain.exception.BusinessException;
import br.com.invplus.shared.domain.service.BaseCrudService;
import br.com.invplus.shared.storage.StorageFacade;
import br.com.invplus.shared.storage.StoredFile;

/**
 * Regras de negocio para metadados de arquivos anexados.
 */
@Service
public class ArquivoService extends BaseCrudService<Arquivo, ArquivoRequest, ArquivoResponse> {

    private static final long TAMANHO_MAXIMO_UPLOAD_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> TIPOS_MIME_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final ArquivoRepository arquivoRepository;
    private final ArquivoValidator arquivoValidator;
    private final ArquivoMapper arquivoMapper;
    private final StorageFacade storageFacade;

    public ArquivoService(
            ArquivoRepository arquivoRepository,
            ArquivoMapper arquivoMapper,
            ArquivoValidator arquivoValidator,
            StorageFacade storageFacade
    ) {
        super(arquivoRepository, arquivoMapper, "Arquivo");
        this.arquivoRepository = arquivoRepository;
        this.arquivoMapper = arquivoMapper;
        this.arquivoValidator = arquivoValidator;
        this.storageFacade = storageFacade;
    }

    /**
     * Lista arquivos ativos em paginas e usa cache por pagina solicitada.
     */
    @Override
    @Cacheable(cacheNames = "cadastros:arquivos", key = "#pageable")
    @Transactional(readOnly = true)
    public Page<ArquivoResponse> listar(Pageable pageable) {
        return super.listar(pageable);
    }

    /**
     * Lista arquivos, com filtro opcional por entidade vinculada e paginacao.
     */
    @Cacheable(cacheNames = "cadastros:arquivos", key = "{#entidade, #entidadeId, #pageable}")
    @Transactional(readOnly = true)
    public Page<ArquivoResponse> listar(String entidade, UUID entidadeId, Pageable pageable) {
        if (entidade == null || entidadeId == null) {
            return listar(pageable);
        }
        return arquivoRepository.findByEntidadeAndEntidadeIdAndAtivoTrueAndDeletedAtIsNull(
                entidade.trim().toUpperCase(),
                entidadeId,
                pageable
        ).map(arquivoMapper::toResponse);
    }

    /**
     * Busca metadados de arquivo pelo identificador.
     */
    @Override
    @Transactional(readOnly = true)
    public ArquivoResponse buscar(UUID id) {
        return super.buscar(id);
    }

    /**
     * Registra um novo arquivo sem armazenar o binario no banco.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public ArquivoResponse criar(ArquivoRequest request) {
        arquivoValidator.validarCriacao(request);
        return super.criar(request);
    }

    /**
     * Recebe um binario, grava no storage configurado e persiste apenas os metadados.
     */
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public ArquivoResponse upload(MultipartFile file, String entidade, UUID entidadeId) {
        validarUpload(file, entidade, entidadeId);

        StoredFile storedFile = armazenar(file);
        try {
            ArquivoRequest request = new ArquivoRequest(
                    storedFile.originalName(),
                    storedFile.storedName(),
                    storedFile.contentType(),
                    storedFile.extension(),
                    storedFile.sizeBytes(),
                    storedFile.sha256(),
                    storedFile.url(),
                    StorageArquivo.valueOf(storedFile.provider().name()),
                    entidade,
                    entidadeId
            );
            arquivoValidator.validarCriacao(request);
            Arquivo arquivo = arquivoMapper.novaEntidade();
            arquivoMapper.aplicar(arquivo, request);
            return arquivoMapper.toResponse(arquivoRepository.save(arquivo));
        } catch (RuntimeException ex) {
            storageFacade.delete(storedFile.storedName());
            throw ex;
        }
    }

    /**
     * Atualiza metadados de arquivo existentes.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public ArquivoResponse atualizar(UUID id, ArquivoRequest request) {
        arquivoValidator.validarAtualizacao(id, request);
        return super.atualizar(id, request);
    }

    /**
     * Realiza exclusao logica dos metadados do arquivo.
     */
    @Override
    @CacheEvict(cacheNames = "cadastros:arquivos", allEntries = true)
    @Transactional
    public void excluir(UUID id) {
        super.excluir(id);
    }

    private void validarUpload(MultipartFile file, String entidade, UUID entidadeId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo enviado nao pode ser vazio");
        }
        if (file.getSize() > TAMANHO_MAXIMO_UPLOAD_BYTES) {
            throw new BusinessException("Arquivo excede o tamanho maximo de 10 MB");
        }
        if (file.getContentType() == null || !TIPOS_MIME_PERMITIDOS.contains(file.getContentType())) {
            throw new BusinessException("Tipo de arquivo nao permitido");
        }
        if (entidade == null || entidade.isBlank()) {
            throw new BusinessException("Entidade vinculada e obrigatoria");
        }
        if (entidadeId == null) {
            throw new BusinessException("Identificador da entidade vinculada e obrigatorio");
        }
    }

    private StoredFile armazenar(MultipartFile file) {
        try {
            return storageFacade.upload(file.getBytes(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException ex) {
            throw new BusinessException("Nao foi possivel ler o arquivo enviado", ex);
        }
    }
}
