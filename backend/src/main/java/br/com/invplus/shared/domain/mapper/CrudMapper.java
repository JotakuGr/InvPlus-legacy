package br.com.invplus.shared.domain.mapper;

import br.com.invplus.shared.domain.entity.SoftDeleteEntity;

/**
 * Contrato minimo para mappers usados pelo CRUD generico.
 */
public interface CrudMapper<T extends SoftDeleteEntity, REQ, RES> {

    /**
     * Cria uma entidade vazia para receber os dados do request.
     */
    T novaEntidade();

    /**
     * Aplica os dados de entrada em uma entidade nova ou existente.
     */
    void aplicar(T entity, REQ request);

    /**
     * Converte a entidade persistida para o DTO de saida da API.
     */
    RES toResponse(T entity);
}
