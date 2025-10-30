package com.example.igestamobile.data.model;

public class UnidadeModel {
    private Integer id;
    private String nome;
    private String estado;
    private String cidade;
    private String cnpj;
    private Integer idPlano;
    private Integer idEmpresa;
    private String senha;

    public UnidadeModel(Integer id, String nome, String estado, String cidade, String cnpj, Integer idPlano, Integer idEmpresa, String senha) {
        this.id = id;
        this.nome = nome;
        this.estado = estado;
        this.cidade = cidade;
        this.cnpj = cnpj;
        this.idPlano = idPlano;
        this.idEmpresa = idEmpresa;
        this.senha = senha;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public Integer getIdPlano() {
        return idPlano;
    }

    public void setIdPlano(Integer idPlano) {
        this.idPlano = idPlano;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
