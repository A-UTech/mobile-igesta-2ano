package com.example.igestamobile.data.model;

import com.google.gson.annotations.SerializedName;

public class CondenaModel {
    @SerializedName("id")
    private int id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("tipo")
    private String tipo;

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }
}