package com.example.igestamobile.data.model.ChatBot;

public class MensagemModel {
    private String mensagem;
    private boolean isFuncionario;

    public MensagemModel(String mensagem, boolean isFuncionario) {
        this.mensagem = mensagem;
        this.isFuncionario = isFuncionario;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isFuncionario() {
        return isFuncionario;
    }

    public void setFuncionario(boolean funcionario) {
        isFuncionario = funcionario;
    }
}
