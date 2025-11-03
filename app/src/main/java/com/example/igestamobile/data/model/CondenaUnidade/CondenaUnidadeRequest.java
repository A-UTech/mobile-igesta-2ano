package com.example.igestamobile.data.model.CondenaUnidade;

public class CondenaUnidadeRequest {
    private Long idCondena;
    private Long idUnidade;

    public CondenaUnidadeRequest(Long idCondena, Long idUnidade) {
        this.idCondena = idCondena;
        this.idUnidade = idUnidade;
    }

    public Long getIdCondena() {
        return idCondena;
    }

    public void setIdCondena(Long idCondena) {
        this.idCondena = idCondena;
    }

    public Long getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Long idUnidade) {
        this.idUnidade = idUnidade;
    }
}
