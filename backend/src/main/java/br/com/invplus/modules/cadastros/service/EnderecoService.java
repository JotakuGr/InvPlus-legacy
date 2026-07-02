package br.com.invplus.modules.cadastros.service;

import br.com.invplus.modules.cadastros.dto.request.EnderecoRequest;
import br.com.invplus.modules.cadastros.dto.response.EnderecoResponse;
import br.com.invplus.modules.cadastros.entity.Empresa;
import br.com.invplus.modules.cadastros.entity.Endereco;
import br.com.invplus.modules.cadastros.repository.EnderecoRepository;
import br.com.invplus.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negocio para enderecos de cadastros.
 */
@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final EmpresaService empresaService;
    private final CidadeService cidadeService;

    /**
     * Lista enderecos ativos ou filtra por empresa quando informado, sempre com paginacao.
     */
    @Transactional(readOnly = true)
    public Page<EnderecoResponse> listar(UUID empresaId, Pageable pageable) {
        Page<Endereco> enderecos = empresaId == null
                ? enderecoRepository.findByAtivoTrueAndDeletedAtIsNull(pageable)
                : enderecoRepository.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(empresaId, pageable);
        return enderecos.map(EnderecoResponse::from);
    }

    /**
     * Busca um endereco pelo identificador.
     */
    @Transactional(readOnly = true)
    public EnderecoResponse buscar(UUID id) {
        return EnderecoResponse.from(buscarEntidade(id));
    }

    /**
     * Cria um endereco e resolve o vinculo opcional com empresa.
     */
    @Transactional
    public EnderecoResponse criar(EnderecoRequest request) {
        Endereco endereco = new Endereco();
        aplicar(endereco, request);
        return EnderecoResponse.from(enderecoRepository.save(endereco));
    }

    /**
     * Atualiza os dados postais de um endereco existente.
     */
    @Transactional
    public EnderecoResponse atualizar(UUID id, EnderecoRequest request) {
        Endereco endereco = buscarEntidade(id);
        aplicar(endereco, request);
        return EnderecoResponse.from(endereco);
    }

    /**
     * Realiza exclusao logica mantendo historico do endereco.
     */
    @Transactional
    public void excluir(UUID id) {
        buscarEntidade(id).excluirLogicamente();
    }

    private void aplicar(Endereco endereco, EnderecoRequest request) {
        Empresa empresa = request.empresaId() == null ? null : empresaService.buscarEntidade(request.empresaId());
        var cidadeCadastro = request.cidadeId() == null ? null : cidadeService.buscarEntidade(request.cidadeId());
        endereco.atualizar(empresa, cidadeCadastro, request.tipo(), request.logradouro(), request.numero(), request.complemento(),
                request.bairro(), request.cidade(), request.uf(), request.cep(), request.principal());
    }

    /**
     * Recupera endereco ativo para uso por outros services.
     */
    @Transactional(readOnly = true)
    public Endereco buscarEntidade(UUID id) {
        return enderecoRepository.findByIdAndAtivoTrueAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereco nao encontrado"));
    }
}
