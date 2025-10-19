package com.example.igestamobile.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginModelRequest {

    @SerializedName("emailCnpj")
    private String emailCnpj;

    private String senha;

    public LoginModelRequest(String emailCnpj, String senha) {
        this.emailCnpj = emailCnpj;
        this.senha = senha;
    }

    public String getEmailCnpj() {
        return emailCnpj;
    }

    public String getSenha() {
        return senha;
    }
}