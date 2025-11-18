package com.futime.labprog.futimeapi.model;

public enum TipoCompeticao {
    MATA_MATA("Mata-Mata"),
    PONTOS_CORRIDOS("Pontos Corridos");

    private final String descricao;

    TipoCompeticao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
