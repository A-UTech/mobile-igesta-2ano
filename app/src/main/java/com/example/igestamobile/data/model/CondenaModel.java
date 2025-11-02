package com.example.igestamobile.data.model;

import com.google.gson.annotations.SerializedName;

public class CondenaModel {
    @SerializedName("id")
    private Long id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("tipo")
    private String tipo;

    private boolean isSelecionada = true;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isSelecionada() {
        return isSelecionada;
    }

    public void setSelecionada(boolean selecionada) {
        isSelecionada = selecionada;
    }
}