package com.example.igestamobile.data.model;

public class AuthRequest {
    private String credencial;
    private String password;

    public AuthRequest(String credencial, String password) {
        this.credencial = credencial;
        this.password = password;
    }
}
