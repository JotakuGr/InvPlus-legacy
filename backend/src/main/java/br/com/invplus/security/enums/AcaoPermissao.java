package br.com.invplus.security.enums;

public enum AcaoPermissao {
    CRIAR,
    VISUALIZAR,
    EDITAR,
    EXCLUIR,
    EXPORTAR,
    APROVAR,
    TODOS("*");

    private final String valorBanco;

    AcaoPermissao() {
        this.valorBanco = name();
    }

    AcaoPermissao(String valorBanco) {
        this.valorBanco = valorBanco;
    }

    public String getValorBanco() {
        return valorBanco;
    }
}
