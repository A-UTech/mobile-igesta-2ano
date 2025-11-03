package com.example.igestamobile.data.model.Auth;

public class AuthRequest {
    private String emailCnpj;
    private String senha;

    public AuthRequest(String emailCnpj, String senha) {
        this.emailCnpj = emailCnpj;
        this.senha = senha;
    }
}
