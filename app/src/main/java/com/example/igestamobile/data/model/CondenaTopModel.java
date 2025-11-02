package com.example.igestamobile.data.model;

public class CondenaTopModel {
    private int totalQuantidade;
    private String nome;

    public CondenaTopModel(int totalQuantidade, String nome) {
        this.totalQuantidade = totalQuantidade;
        this.nome = nome;
    }

    public int getTotalQuantidade() {
        return totalQuantidade;
    }

    public void setTotalQuantidade(int totalQuantidade) {
        this.totalQuantidade = totalQuantidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
