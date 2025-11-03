package com.example.igestamobile.data.model.CondenaUnidade;

public class CondenaUnidadeResponse {
    private Long idCondena;
    private int quantidade;
    private String nome;
    private String tipo;

    public CondenaUnidadeResponse(Long idCondena) {
        this.idCondena = idCondena;
        this.quantidade = 0;
    }

    public Long getIdCondena() {
        return idCondena;
    }

    public void setIdCondena(Long idCondena) {
        this.idCondena = idCondena;
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
