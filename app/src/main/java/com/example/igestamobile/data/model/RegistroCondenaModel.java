package com.example.igestamobile.data.model;

public class RegistroCondenaModel {
    private Long id;
    private int quantidade;
    private String nome;
    private String tipo;

    public RegistroCondenaModel(Long id) {
        this.id = id;
        this.quantidade = 0;
    }

    public RegistroCondenaModel(Long id, int quantidade, String nome, String tipo) {
        this.id = id;
        this.quantidade = quantidade;
        this.nome = nome;
        this.tipo = tipo;
    }

    public RegistroCondenaModel(CondenaUnidadeResponse response) {
        this.id = response.getIdCondena();
        this.quantidade = response.getQuantidade();
        this.nome = response.getNome();
        this.tipo = response.getTipo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantidade(int novaQuantidade) {
        this.quantidade = novaQuantidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
