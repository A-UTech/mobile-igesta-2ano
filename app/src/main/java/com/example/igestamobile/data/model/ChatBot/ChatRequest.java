package com.example.igestamobile.data.model.ChatBot;

public class ChatRequest {
    private String usuario;
    private String unidade;

    public ChatRequest(String usuario, String unidade) {
        this.usuario = usuario;
        this.unidade = unidade;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

}
