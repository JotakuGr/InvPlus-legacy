package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.EmpresaRequest;
import br.com.invplus.modules.cadastros.dto.response.EmpresaResponse;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.repository.EmpresaRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para cadastro de empresas.
 */
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    /**
     * Lista empresas ativas em paginas para evitar consultas sem limite.
     */
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(Pageable pageable) {
        return empresaRepository.findByAtivoTrueAndDeletedAtIsNull(pageable).map(EmpresaResponse::from);
    }

    /**
     * Busca uma empresa pelo identificador.
     */
    @Transactional(readOnly = true)
    public EmpresaResponse buscar(UUID id) {
        return EmpresaResponse.from(buscarEntidade(id));
    }

    /**
     * Cria uma nova empresa com dados normalizados.
     */
    @Transactional
    public EmpresaResponse criar(EmpresaRequest request) {
        Empresa empresa = new Empresa(request.razaoSocial(), request.nomeFantasia(), request.cnpj(),
                request.inscricaoEstadual(), request.telefone(), request.email());
        return EmpresaResponse.from(empresaRepository.save(empresa));
    }

    /**
     * Atualiza uma empresa existente.
     */
    @Transactional
    public EmpresaResponse atualizar(UUID id, EmpresaRequest request) {
        Empresa empresa = buscarEntidade(id);
        empresa.atualizar(request.razaoSocial(), request.nomeFantasia(), request.cnpj(),
                request.inscricaoEstadual(), request.telefone(), request.email());
        return EmpresaResponse.from(empresa);
    }

    /**
     * Realiza exclusao logica mantendo historico do cadastro.
     */
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    /**
     * Recupera a entidade ou dispara erro padronizado de nao encontrado.
     */
    @Transactional(readOnly = true)
    public Empresa buscarEntidade(UUID id) {
        return empresaRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
    }
}
