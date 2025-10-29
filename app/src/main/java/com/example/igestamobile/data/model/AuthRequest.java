package com.example.igestamobile.data.model;

public class AuthRequest {
    private String emailCnpj;
    private String senha;

    public AuthRequest(String emailCnpj, String senha) {
        this.emailCnpj = emailCnpj;
        this.senha = senha;
    }
}
