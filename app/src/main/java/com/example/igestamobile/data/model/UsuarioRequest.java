package com.example.igestamobile.data.model;

public class UsuarioRequest {
    private String nome;
    private String email;
    private String senha;

    public UsuarioRequest(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
