package br.com.invplus.modules.cadastros.validator;

import br.com.invplus.modules.cadastros.dto.request.CategoriaRequest;
import br.com.invplus.modules.cadastros.entity.Categoria;
import br.com.invplus.modules.cadastros.repository.CategoriaRepository;
import br.com.invplus.shared.domain.exception.BusinessException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida regras de negocio de Categoria, incluindo unicidade e hierarquia.
 */
@Component
@RequiredArgsConstructor
public class CategoriaValidator {

    private final CategoriaRepository categoriaRepository;

    /**
     * Valida criacao com nome unico e pai opcional.
     */
    public void validarCriacao(CategoriaRequest request, Categoria categoriaPai) {
        validarNomeUnico(null, request.nome());
        validarCicloHierarquico(null, categoriaPai);
    }

    /**
     * Valida atualizacao impedindo duplicidade, auto-relacao e ciclos.
     */
    public void validarAtualizacao(UUID id, CategoriaRequest request, Categoria categoriaPai) {
        validarNomeUnico(id, request.nome());
        if (request.categoriaPaiId() != null && request.categoriaPaiId().equals(id)) {
            throw new BusinessException("Categoria nao pode ser pai dela mesma");
        }
        validarCicloHierarquico(id, categoriaPai);
    }

    private void validarNomeUnico(UUID idAtual, String nome) {
        String nomeNormalizado = nome == null ? null : nome.trim();
        categoriaRepository.findByNomeIgnoreCaseAndAtivoTrueAndDeletedAtIsNull(nomeNormalizado)
                .map(Categoria::getId)
                .filter(idEncontrado -> !idEncontrado.equals(idAtual))
                .ifPresent(id -> {
                    throw new BusinessException("Ja existe categoria ativa com este nome");
                });
    }

    private void validarCicloHierarquico(UUID idAtual, Categoria categoriaPai) {
        if (idAtual == null || categoriaPai == null) {
            return;
        }

        Set<UUID> visitados = new HashSet<>();
        Categoria atual = categoriaPai;
        while (atual != null) {
            if (!visitados.add(atual.getId())) {
                throw new BusinessException("Ciclo hierarquico detectado em categoria");
            }
            if (idAtual.equals(atual.getId())) {
                throw new BusinessException("Categoria nao pode gerar ciclo hierarquico");
            }
            atual = atual.getCategoriaPai();
        }
    }
}
