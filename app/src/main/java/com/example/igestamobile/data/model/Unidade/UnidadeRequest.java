package com.example.igestamobile.data.model.Unidade;

public class UnidadeRequest {
    private String nome;
    private String cnpj;
    private String senha;

    public UnidadeRequest(String nome, String cnpj, String senha) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.senha = senha;
    }

    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public String getSenha() { return senha; }
}
