package com.example.igestamobile.data.model.Lider;

import com.google.gson.annotations.SerializedName;

public class LiderModel {
    @SerializedName("id")
    private Integer id;

    @SerializedName("idUnidade")
    private Integer idUnidade;

    @SerializedName("nome")
    private String nome;

    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    @SerializedName("area")
    private String area;

    public LiderModel(Integer idUnidade, String nome, String email, String senha, String area) {
        this.idUnidade = idUnidade;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.area = area;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Integer idUnidade) {
        this.idUnidade = idUnidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
