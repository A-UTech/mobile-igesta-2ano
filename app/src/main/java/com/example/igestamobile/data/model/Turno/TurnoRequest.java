package com.example.igestamobile.data.model.Turno;

public class TurnoRequest {
    private Integer id;
    private Integer idUnidade;
    private String nome;
    private String inicio;
    private String fim;

    public TurnoRequest(Integer idUnidade, String nome, String inicio, String fim) {
        this.idUnidade = idUnidade;
        this.nome = nome;
        this.inicio = inicio;
        this.fim = fim;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Integer idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFim() {
        return fim;
    }

    public void setFim(String fim) {
        this.fim = fim;
    }
}
