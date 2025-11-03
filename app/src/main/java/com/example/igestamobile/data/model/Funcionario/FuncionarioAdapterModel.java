package com.example.igestamobile.data.model.Funcionario;

public class FuncionarioAdapterModel {
    private String id;
    private String nome;
    private String cargo;
    private String identificador;
    private String email;
    private String urlImagem;

    public FuncionarioAdapterModel(String id, String nome, String cargo, String email, String urlImagem) {
        this.identificador = id;
        this.nome = nome;
        this.cargo = cargo;
        this.email = email;
        this.urlImagem = urlImagem;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }

    public String getEmail() {
        return email;
    }
    public String getUrlImagem() {
        return urlImagem;
    }
    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }
}