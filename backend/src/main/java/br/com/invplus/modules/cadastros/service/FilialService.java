package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.FilialRequest;
import br.com.invplus.modules.cadastros.dto.response.FilialResponse;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Endereco;
import br.com.invplus.modules.cadastros.entity.Filial;
import br.com.invplus.modules.cadastros.mapper.FilialMapper;
import br.com.invplus.modules.cadastros.repository.FilialRepository;
import br.com.invplus.modules.cadastros.validator.FilialValidator;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para cadastro de filiais.
 */
@Service
@RequiredArgsConstructor
public class FilialService {

    private final FilialRepository filialRepository;
    private final EmpresaService empresaService;
    private final EnderecoService enderecoService;
    private final FilialMapper filialMapper;
    private final FilialValidator filialValidator;

    /**
     * Lista filiais ativas, opcionalmente por empresa, sempre com paginacao.
     */
    @Transactional(readOnly = true)
    public Page<FilialResponse> listar(UUID empresaId, Pageable pageable) {
        Page<Filial> filiais = empresaId == null
                ? filialRepository.findByAtivoTrueAndDeletedAtIsNull(pageable)
                : filialRepository.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(empresaId, pageable);
        return filiais.map(filialMapper::toResponse);
    }

    /**
     * Busca filial ativa pelo identificador.
     */
    @Transactional(readOnly = true)
    public FilialResponse buscar(UUID id) {
        return filialMapper.toResponse(buscarEntidade(id));
    }

    /**
     * Cria uma filial vinculada a empresa ativa.
     */
    @Transactional
    public FilialResponse criar(FilialRequest request) {
        filialValidator.validarCriacao(request);
        Filial filial = new Filial();
        aplicar(filial, request);
        return filialMapper.toResponse(filialRepository.save(filial));
    }

    /**
     * Atualiza uma filial existente.
     */
    @Transactional
    public FilialResponse atualizar(UUID id, FilialRequest request) {
        Filial filial = buscarEntidade(id);
        aplicar(filial, request);
        return filialMapper.toResponse(filial);
    }

    /**
     * Exclui logicamente a filial.
     */
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    private void aplicar(Filial filial, FilialRequest request) {
        Empresa empresa = empresaService.buscarEntidade(request.empresaId());
        Endereco endereco = request.enderecoId() == null ? null : enderecoService.buscarEntidade(request.enderecoId());
        filial.atualizar(empresa, endereco, request.codigo(), request.nome(), request.cnpj(),
                request.inscricaoEstadual(), request.telefone(), request.email(), request.matriz());
    }

    private Filial buscarEntidade(UUID id) {
        return filialRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filial nao encontrada"));
    }
}
