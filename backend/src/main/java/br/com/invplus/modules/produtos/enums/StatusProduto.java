package br.com.invplus.modules.produtos.enums;

/**
 * Estado operacional do produto, independente do soft delete do registro.
 */
public enum StatusProduto {
    EM_DESENVOLVIMENTO,
    ATIVO,
    DESCONTINUADO,
    BLOQUEADO,
    SUSPENSO
}
