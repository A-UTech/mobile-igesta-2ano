package com.example.igestamobile.data.model;

public class CondenaDetalhe {
    private String nome;
    private String tipo;
    private int quantidade;
    private double porcentagem;

    public CondenaDetalhe(String nome, String tipo, int quantidade, double porcentagem) {
        this.nome = nome;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.porcentagem = porcentagem;
    }

    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public double getPorcentagem() { return porcentagem; }
}